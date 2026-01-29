package com.tech.novagraphbackendcommon.cache;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tech.novagraphbackendcommon.exception.BusinessException;
import com.tech.novagraphbackendcommon.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

@Slf4j
@Component
@ConditionalOnClass(RedisTemplate.class)
public class PageCacheTemplate {

    /**
     * 全局锁映射，用于防止缓存击穿时的并发重建
     * 这里为了线程安全和高性能，使用 ConcurrentHashMap
     */
    protected final Map<String, Object> lockMap = new ConcurrentHashMap<>();

    /**
     * 通用分页查询缓存模板方法
     *
     * @param request          请求参数对象 (用于生成锁的 Key)
     * @param lockKeyGenerator 锁 Key 的生成器 (例如: req -> CacheUtils.getHexLockString(req))
     * @param cacheLoader      缓存加载函数 (调用查缓存的方法，返回 null 表示未命中)
     * @param dbLoader         查询数据库
     * @param converter        实体转 VO 的转换函数
     * @param cacheSaver       缓存回写函数
     * @param <R>              Request 请求类型
     * @param <E>              Entity 数据库实体类型
     * @param <V>              VO 视图对象类型
     * @return Page<V>
     */
    public <R, E, V> Page<V> baseQuery(
            R request,
            Function<R, String> lockKeyGenerator,
            Supplier<Page<V>> cacheLoader,
            Supplier<Page<E>> dbLoader,
            Function<List<E>, List<V>> converter,
            Consumer<Page<V>> cacheSaver) {

        // 1. 先查缓存
        Page<V> entityVOPage = cacheLoader.get();
        if (entityVOPage != null) {
            return entityVOPage;
        }

        // 2. 缓存未命中，开始准备加锁
        // 使用函数式接口生成锁字符串，解耦具体的生成逻辑
        String lockStr = lockKeyGenerator.apply(request);

        // computeIfAbsent 保证同一个 lockStr 获取的是同一个锁对象
        Object lock = lockMap.computeIfAbsent(lockStr, k -> new Object());

        synchronized (lock) {
            // 3. 双重检查锁定 (Double-Check Locking)
            // 获取锁后，再次检查缓存，防止等待锁的期间其他线程已经重建了缓存
            entityVOPage = cacheLoader.get();
            if (entityVOPage != null) {
                // 防止内存泄漏：获取锁后发现已有缓存，需移除当前锁引用
                lockMap.remove(lockStr);
                return entityVOPage;
            }

            try {
                // 4. 查数据库
                Page<E> entityPage = dbLoader.get();
                List<E> entityList = entityPage.getRecords();

                // 5. 转换 Entity -> VO
                List<V> entityVoList = converter.apply(entityList);

                // 6. 组装返回结果
                entityVOPage = new Page<>();
                entityVOPage.setRecords(entityVoList);
                entityVOPage.setCurrent(entityPage.getCurrent());
                entityVOPage.setSize(entityPage.getSize());
                entityVOPage.setTotal(entityPage.getTotal());

                // 7. 写入缓存
                cacheSaver.accept(entityVOPage);

                return entityVOPage;
            } catch (Exception e) {
                log.error(ErrorCode.SYSTEM_ERROR.getMessage(), e);
                 throw new BusinessException(ErrorCode.SYSTEM_ERROR);
            } finally {
                // 8. 防止内存泄漏：无论成功失败，都要移除锁对象
                lockMap.remove(lockStr);
            }
        }
    }
}
