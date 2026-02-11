package com.tech.novagraphbackenduserservice.infrastructure.manager.handler;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tech.novagraphbackendcommon.cache.valueobject.UserActionEnum;
import com.tech.novagraphbackendcommon.handler.IUserActionSyncHandler;
import com.tech.novagraphbackendmodel.user.constant.UserCacheConstant;
import com.tech.novagraphbackendmodel.user.entity.UserFavorite;
import com.tech.novagraphbackendmodel.user.entity.UserPostThumb;
import com.tech.novagraphbackendserviceclient.GraphFeignClient;
import com.tech.novagraphbackenduserservice.infrastructure.mapper.UserFavoriteMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;

@Slf4j
@Component
public class UserFavoriteHandler {

    @Resource
    private UserFavoriteMapper userFavoriteMapper;

    @Resource
    private GraphFeignClient graphFeignClient;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Transactional(rollbackFor = Exception.class)
    public void executeSync(String tail) {
        String redisKey = UserCacheConstant.buildRedisKey(UserCacheConstant.getTempUserFavoriteKey(tail));
        Map<Object, Object> allTempMap = redisTemplate.opsForHash().entries(redisKey);

        if (CollectionUtils.isEmpty(allTempMap)) {
            return;
        }

        // 1. 数据分类容器
        // 统计增量
        Map<Long, Long> targetCountMap = new HashMap<>();
        // 待插入实体
        List<UserFavorite> insertList = new ArrayList<>();
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
            if (ids.length < 3) continue;

            Long userId = Long.valueOf(ids[0]);
            Long targetId = Long.valueOf(ids[1]);
            Long folderId = Long.valueOf(ids[2]);
            Integer type = Integer.valueOf(allTempMap.get(keyObj).toString());

            // 计算目标的增量
            targetCountMap.put(targetId, targetCountMap.getOrDefault(targetId, 0L) + type);

            if (type == UserActionEnum.INCR.getValue()) {
                // 增加的逻辑
                UserFavorite entity = new UserFavorite();
                entity.setUserId(userId);
                entity.setFolderId(folderId);
                entity.setScreenplayId(targetId);
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
            userFavoriteMapper.batchIgnoreInsert(insertList);
        } else if (insertList.size() == 1) {
            userFavoriteMapper.insert(insertList.getFirst());
        }
        if (!CollectionUtils.isEmpty(deleteUserIds)) {
            LambdaQueryWrapper<UserFavorite> wrapper = new LambdaQueryWrapper<>();
            for (int i = 0; i< deleteUserIds.size(); i++) {
                wrapper.or().eq(UserFavorite::getUserId, deleteUserIds.get(i))
                        .eq(UserFavorite::getScreenplayId, deleteTargetIds.get(i));
            }
            userFavoriteMapper.delete(wrapper);
        }
        if (!CollectionUtils.isEmpty(targetCountMap)) {
            graphFeignClient.batchUpdateFavourites(targetCountMap);
        }

//        // 4. 处理副作用 (积分 + 消息)
//        if (!CollectionUtils.isEmpty(insertedTargetIds)) {
//
//        }

        // 5. 清理 Redis 中的临时缓存
        Thread.startVirtualThread(() -> redisTemplate.delete(redisKey));
    }

}
