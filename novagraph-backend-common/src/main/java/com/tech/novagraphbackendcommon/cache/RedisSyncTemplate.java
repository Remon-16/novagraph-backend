package com.tech.novagraphbackendcommon.cache;

import com.tech.novagraphbackendcommon.cache.valueobject.UserActionEnum;
import com.tech.novagraphbackendcommon.handler.IUserActionSyncHandler;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;

@Slf4j
@Component
@ConditionalOnClass(RedisTemplate.class)
public class RedisSyncTemplate {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 执行缓存同步到数据库
     * @param handler 具体实现的处理实例
     * @param tail MQ方式、定时器同步方式（服务降级机制）或者补偿机制 tail可能不同。
     * @param <T> 具体业务处理的实现类
     */
    @Transactional(rollbackFor = Exception.class)
    public <T> void executeSync(IUserActionSyncHandler<T> handler, String tail) {
        String redisKey = handler.getRedisKeyPrefix() + tail;
        Map<Object, Object> allTempMap = redisTemplate.opsForHash().entries(redisKey);

        if (CollectionUtils.isEmpty(allTempMap)) {
            return;
        }

        // 1. 数据分类容器
        // 统计增量
        Map<Long, Long> targetCountMap = new HashMap<>();
        // 待插入实体
        List<T> insertList = new ArrayList<>();
        // 待删除记录的用户ID
        List<Long> deleteUserIds = new ArrayList<>();
        // 待删除记录的目标ID
        List<Long> deleteTargetIds = new ArrayList<>();
        // 记录插入操作对应的 targetId，用于后续处理副作用
        Set<Long> insertedTargetIds = new HashSet<>();

        // 2. 解析 Redis 数据
        for (Object keyObj : allTempMap.keySet()) {
            String compositeKey = (String) keyObj;
            // 假设 Key 格式为 "userId:targetId"
            String[] ids = compositeKey.split(":");
            if (ids.length < 2) continue;

            Long userId = Long.valueOf(ids[0]);
            Long targetId = Long.valueOf(ids[1]);
            Integer type = Integer.valueOf(allTempMap.get(keyObj).toString());

            // 计算目标的增量
            targetCountMap.put(targetId, targetCountMap.getOrDefault(targetId, 0L) + type);

            if (type == UserActionEnum.INCR.getValue()) {
                // 增加的逻辑
                T entity = handler.buildEntity(userId, targetId);
                insertList.add(entity);
                insertedTargetIds.add(targetId);
            } else if (type == UserActionEnum.DECR.getValue()) {
                // 减少的逻辑
                deleteUserIds.add(userId);
                deleteTargetIds.add(targetId);
            } else {
                // 不操作的逻辑 比如某个用户点完赞可能立刻取消了，这就不需要同步。
                if (type != UserActionEnum.NON.getValue()) {
                    log.warn("数据异常：{}", redisKey + "," + userId + "," + targetId + "," + type);
                }
            }
        }

        // 3. 执行数据库操作 由于缓存中可能存在违反唯一约束的数据，所以批量操作和单独操作在业务上做一个区分。
        if (!CollectionUtils.isEmpty(insertList) && insertList.size() > 1) {
            handler.batchInsert(insertList);
        } else if (insertList.size() == 1) {
            handler.singleInsert(insertList.getFirst());
        }
        if (!CollectionUtils.isEmpty(deleteUserIds)) {
            handler.batchRemove(deleteUserIds, deleteTargetIds);
        }
        if (!CollectionUtils.isEmpty(targetCountMap)) {
            handler.updateStatistics(targetCountMap);
        }

        // 4. 处理副作用 (积分 + 消息)
        if (!CollectionUtils.isEmpty(insertedTargetIds)) {
            handleGenericSideEffects(handler, insertedTargetIds, insertList);
        }

        // 5. 清理 Redis 中的临时缓存
        Thread.startVirtualThread(() -> redisTemplate.delete(redisKey));
    }

    /**
     * 通用的副作用处理逻辑
     */
    private <T> void handleGenericSideEffects(IUserActionSyncHandler<T> handler, Set<Long> targetIds, List<T> insertedList) {
        handler.handleSideEffects(insertedList, targetIds);
    }
}