package com.tech.novagraphbackenduserservice.infrastructure.manager.handler;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tech.novagraphbackendcommon.exception.BusinessException;
import com.tech.novagraphbackendcommon.exception.ErrorCode;
import com.tech.novagraphbackendcommon.handler.IUserActionSyncHandler;
import com.tech.novagraphbackendmodel.user.constant.UserCacheConstant;
import com.tech.novagraphbackendmodel.user.entity.UserFollow;
import com.tech.novagraphbackenduserservice.infrastructure.mapper.UserFollowMapper;
import com.tech.novagraphbackenduserservice.infrastructure.mapper.UserStatisticsMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class UserFollowHandler implements IUserActionSyncHandler<UserFollow> {

    @Resource
    private UserFollowMapper userFollowMapper;

    @Resource
    private UserStatisticsMapper userStatisticsMapper;

    @Override
    public String getRedisKeyPrefix() {
        return UserCacheConstant.buildRedisKey(UserCacheConstant.USER_FOLLOW_TEMP_KEY_PREFIX);
    }

    @Override
    public UserFollow buildEntity(Long userId, Long targetId) {
        UserFollow userFollow = new UserFollow();
        userFollow.setUserId(userId);
        userFollow.setFollowingId(targetId);
        return userFollow;
    }

    @Override
    public void batchInsert(List<UserFollow> entityList) {
        userFollowMapper.batchIgnoreInsert(entityList);
    }

    @Override
    public void singleInsert(UserFollow entity) {
        userFollowMapper.insert(entity);
    }

    @Override
    public void batchRemove(List<Long> userIds, List<Long> targetIds) {
        LambdaQueryWrapper<UserFollow> wrapper = new LambdaQueryWrapper<>();
        for (int i = 0; i<userIds.size(); i++) {
            wrapper.or().eq(UserFollow::getUserId, userIds.get(i)).eq(UserFollow::getFollowingId, targetIds.get(i));
        }
        userFollowMapper.delete(wrapper);
    }

    @Override
    public void singleDelete(Long userId, Long targetId) {
        LambdaQueryWrapper<UserFollow> wrapper = new LambdaQueryWrapper<>();
        wrapper.or().eq(UserFollow::getUserId, userId).eq(UserFollow::getFollowingId, targetId);
        userFollowMapper.delete(wrapper);
    }

    @Override
    public void updateStatistics(Map<Long, Long> countMap) {
        userStatisticsMapper.batchUpdateFollowerCount(countMap);
    }

    @Override
    public Long getTargetOwnerId(Long targetId) {
        return 0L;
    }

    @Override
    public void handleSideEffects(List<UserFollow> insertedList, Set<Long> targetIds) {

    }
}
