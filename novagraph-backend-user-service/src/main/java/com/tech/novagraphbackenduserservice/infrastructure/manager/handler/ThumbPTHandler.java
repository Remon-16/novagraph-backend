package com.tech.novagraphbackenduserservice.infrastructure.manager.handler;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tech.novagraphbackendcommon.handler.IUserActionSyncHandler;
import com.tech.novagraphbackendmodel.user.constant.UserCacheConstant;
import com.tech.novagraphbackendmodel.user.entity.UserPostThumb;
import com.tech.novagraphbackenduserservice.infrastructure.mapper.UserPostStatisticsMapper;
import com.tech.novagraphbackenduserservice.infrastructure.mapper.UserPostThumbMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class ThumbPTHandler implements IUserActionSyncHandler<UserPostThumb> {

    private UserPostStatisticsMapper userPostStatisticsMapper;

    @Resource
    private UserPostThumbMapper userPostThumbMapper;


    @Override
    public String getRedisKeyPrefix() {
        return UserCacheConstant.buildRedisKey(UserCacheConstant.TEMP_THUMB_KEY_PREFIX);
    }

    @Override
    public UserPostThumb buildEntity(Long userId, Long targetId) {
        UserPostThumb userPostThumb = new UserPostThumb();
        userPostThumb.setUserId(userId);
        userPostThumb.setPostId(targetId);
        return userPostThumb;
    }

    @Override
    public void batchInsert(List<UserPostThumb> entityList) {
        userPostThumbMapper.batchIgnoreInsert(entityList);
    }

    @Override
    public void singleInsert(UserPostThumb entity) {
        userPostThumbMapper.insert(entity);
    }

    @Override
    public void batchRemove(List<Long> userIds, List<Long> targetIds) {
        LambdaQueryWrapper<UserPostThumb> wrapper = new LambdaQueryWrapper<>();
        for (int i = 0; i<userIds.size(); i++) {
            wrapper.or().eq(UserPostThumb::getUserId, userIds.get(i)).eq(UserPostThumb::getPostId, targetIds.get(i));
        }
        userPostThumbMapper.delete(wrapper);
    }

    @Override
    public void singleDelete(Long userId, Long targetId) {
        LambdaQueryWrapper<UserPostThumb> wrapper = new LambdaQueryWrapper<>();
        wrapper.or().eq(UserPostThumb::getUserId, userId).eq(UserPostThumb::getPostId, targetId);
        userPostThumbMapper.delete(wrapper);
    }

    @Override
    public void updateStatistics(Map<Long, Long> countMap) {
        userPostStatisticsMapper.batchUpdateThumbCount(countMap);
    }

    @Override
    public Long getTargetOwnerId(Long targetId) {
        return 0L;
    }

    @Override
    public void handleSideEffects(List<UserPostThumb> insertedList, Set<Long> targetIds) {

    }
}
