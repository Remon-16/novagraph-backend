package com.tech.novagraphbackendcommon.cache;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tech.novagraphbackendcommon.cache.bean.ValueQueryBean;
import com.tech.novagraphbackendcommon.exception.ErrorCode;
import com.tech.novagraphbackendcommon.exception.ThrowUtils;
import com.tech.novagraphbackendcommon.utils.CacheUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.function.Function;
import java.util.function.Supplier;

@Slf4j
@Component
@ConditionalOnClass(RedisTemplate.class)
public class SingleValueCacheTemplate extends SingleCacheTemplate{
    @Resource
    private CacheManager cacheManager;

    public <R, E, V> V valueQuery(R request, ValueQueryBean valueQueryBean,
                                        Supplier<E> dbLoader, Function<E, V> converter) {
        return super.baseQuery(
                // 参数1: Request 对象
                request,
                // 参数2: 锁 Key 生成器
                CacheUtils::getHexLockString,
                // 参数3: 查缓存逻辑
                () -> queryCache(valueQueryBean),
                // 参数4: 查数据库逻辑
                dbLoader,
                // 参数5: 转换逻辑
                converter,
                // 参数6: 写缓存逻辑
                vo -> this.putEntityVOPage2Cache(vo, valueQueryBean.getCacheKey())
        );
    }

    private <V> V queryCache(ValueQueryBean valueQueryBean){
        String cacheKey = valueQueryBean.getCacheKey();
        Class VOClass = valueQueryBean.getVOClass();
        ThrowUtils.throwIf(StringUtils.isEmpty(cacheKey), ErrorCode.PARAMS_ERROR);

        Object value = cacheManager.getValueCache(cacheKey);
        if(value == null){
            return null;
        }
        return (V) JSONUtil.toBean((String)value, VOClass);
    }

    private <V> void putEntityVOPage2Cache(V vo, String cacheKey){
        cacheManager.putValueToCache(cacheKey, vo);
    }

}
