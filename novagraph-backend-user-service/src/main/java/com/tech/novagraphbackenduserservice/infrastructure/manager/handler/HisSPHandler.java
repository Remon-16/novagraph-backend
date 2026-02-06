package com.tech.novagraphbackenduserservice.infrastructure.manager.handler;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tech.novagraphbackendcommon.handler.IUserActionSyncHandler;
import com.tech.novagraphbackendmodel.user.constant.UserCacheConstant;
import com.tech.novagraphbackendmodel.user.entity.UserPlayHistory;
import com.tech.novagraphbackendserviceclient.GraphFeignClient;
import com.tech.novagraphbackenduserservice.infrastructure.mapper.UserPlayHistoryMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class HisSPHandler implements IUserActionSyncHandler<UserPlayHistory> {

    @Resource
    private UserPlayHistoryMapper userPlayHistoryMapper;

    @Resource
    private GraphFeignClient graphFeignClient;

    @Override
    public String getRedisKeyPrefix() {
        return UserCacheConstant.buildRedisKey(UserCacheConstant.TEMP_HIS_KEY_PREFIX);
    }

    @Override
    public UserPlayHistory buildEntity(Long userId, Long targetId) {
        UserPlayHistory userPlayHistory = new UserPlayHistory();
        userPlayHistory.setUserId(userId);
        userPlayHistory.setScreenplayId(targetId);
        return userPlayHistory;
    }

    @Override
    public void batchInsert(List<UserPlayHistory> entityList) {
        userPlayHistoryMapper.batchIgnoreInsert(entityList);
    }

    @Override
    public void singleInsert(UserPlayHistory entity) {
        userPlayHistoryMapper.insert(entity);
    }

    @Override
    public void batchRemove(List<Long> userIds, List<Long> targetIds) {
        LambdaQueryWrapper<UserPlayHistory> wrapper = new LambdaQueryWrapper<>();
        for (int i = 0; i<userIds.size(); i++) {
            wrapper.or().eq(UserPlayHistory::getUserId, userIds.get(i))
                    .eq(UserPlayHistory::getScreenplayId, targetIds.get(i));
        }
        userPlayHistoryMapper.delete(wrapper);
    }

    @Override
    public void singleDelete(Long userId, Long targetId) {
        LambdaQueryWrapper<UserPlayHistory> wrapper = new LambdaQueryWrapper<>();
        wrapper.or().eq(UserPlayHistory::getUserId, userId)
                .eq(UserPlayHistory::getScreenplayId, targetId);
        userPlayHistoryMapper.delete(wrapper);
    }

    @Override
    public void updateStatistics(Map<Long, Long> countMap) {
        graphFeignClient.batchUpdatePlayCount(countMap);
    }

    @Override
    public void updateOwnerStatistics(Map<Long, Long> countMap) {

    }

    @Override
    public Long getTargetOwnerId(Long targetId) {
        return 0L;
    }

    @Override
    public void handleSideEffects(List<UserPlayHistory> insertedList, Set<Long> targetIds) {

    }
}
