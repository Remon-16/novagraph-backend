package com.tech.novagraphbackenduserservice.domain.user.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tech.novagraphbackendcommon.cache.CacheManager;
import com.tech.novagraphbackendcommon.cache.valueobject.LuaStatusEnum;
import com.tech.novagraphbackendcommon.cache.valueobject.UserActionEnum;
import com.tech.novagraphbackendcommon.exception.BusinessException;
import com.tech.novagraphbackendcommon.exception.ErrorCode;
import com.tech.novagraphbackendcommon.utils.CacheUtils;
import com.tech.novagraphbackendmodel.dto.user.DoFollowRequest;
import com.tech.novagraphbackendmodel.user.constant.UserCacheConstant;
import com.tech.novagraphbackendmodel.user.constant.UserRedisLuaScriptConstant;
import com.tech.novagraphbackendmodel.user.entity.User;
import com.tech.novagraphbackendmodel.user.entity.UserFollow;
import com.tech.novagraphbackendmodel.vo.user.UserVO;
import com.tech.novagraphbackenduserservice.domain.user.service.UserFollowDomainService;
import com.tech.novagraphbackenduserservice.infrastructure.mapper.UserFollowMapper;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class UserFollowDomainServiceImpl extends ServiceImpl<UserFollowMapper, UserFollow>
        implements UserFollowDomainService {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private CacheManager cacheManager;

    @Override
    public Boolean doFollow(DoFollowRequest doFollowRequest, User loginUser) {
        if(doFollowRequest == null || doFollowRequest.getTargetUserId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数错误");
        }
        Long targetUserId = doFollowRequest.getTargetUserId();
        Long userId = loginUser.getId();
        String timeSlice = CacheUtils.getTimeSlice();

        String tempFollowKey = UserCacheConstant.buildRedisKey(UserCacheConstant.getUserFollowTempKey(timeSlice));
        String UserFollowingKey = UserCacheConstant.buildRedisKey(UserCacheConstant.getUserFollowingKey(userId.toString()));
        String UserFollowerKey = UserCacheConstant.buildRedisKey(UserCacheConstant.getUserFollowerKey(targetUserId.toString()));
        String UserFollowingCountKey = UserCacheConstant.buildRedisKey(UserCacheConstant.getUserFollowingCountKey(userId.toString()));
        String UserFollowerCountKey = UserCacheConstant.buildRedisKey(UserCacheConstant.getUserFollowerCountKey(targetUserId.toString()));

        long result = redisTemplate.execute(
                UserRedisLuaScriptConstant.USER_FOLLOW_SCRIPT,
                Arrays.asList(tempFollowKey, UserFollowingKey, UserFollowerKey, UserFollowingCountKey, UserFollowerCountKey),
                loginUser.getId(),
                targetUserId
        );
        if (LuaStatusEnum.FAIL.getValue() == result) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户已关注");
        }
        // 如果存在本地缓存，则写入
        putCaffineIfPresent(loginUser, targetUserId, UserActionEnum.INCR.getValue());

        return LuaStatusEnum.SUCCESS.getValue() == result;
    }

    @Override
    public Boolean undoFollow(DoFollowRequest doFollowRequest, User loginUser) {
        if(doFollowRequest == null || doFollowRequest.getTargetUserId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数错误");
        }
        Long targetUserId = doFollowRequest.getTargetUserId();
        Long userId = loginUser.getId();
        String timeSlice = CacheUtils.getTimeSlice();

        String tempFollowKey = UserCacheConstant.buildRedisKey(UserCacheConstant.getUserFollowTempKey(timeSlice));
        String UserFollowingKey = UserCacheConstant.buildRedisKey(UserCacheConstant.getUserFollowingKey(userId.toString()));
        String UserFollowerKey = UserCacheConstant.buildRedisKey(UserCacheConstant.getUserFollowerKey(targetUserId.toString()));
        String UserFollowingCountKey = UserCacheConstant.buildRedisKey(UserCacheConstant.getUserFollowingCountKey(userId.toString()));
        String UserFollowerCountKey = UserCacheConstant.buildRedisKey(UserCacheConstant.getUserFollowerCountKey(targetUserId.toString()));
        long result = redisTemplate.execute(
                UserRedisLuaScriptConstant.USER_UNFOLLOW_SCRIPT,
                Arrays.asList(tempFollowKey, UserFollowingKey, UserFollowerKey, UserFollowingCountKey, UserFollowerCountKey),
                loginUser.getId(),
                targetUserId
        );

        // 根据返回值处理结果
        if (result == LuaStatusEnum.FAIL.getValue()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户未关注");
        }

        // 如果存在本地缓存，则写入
        putCaffineIfPresent(loginUser, targetUserId, UserActionEnum.DECR.getValue());

        return LuaStatusEnum.SUCCESS.getValue() == result;
    }

    @Override
    public Boolean hasFollow(Long targetUserId, Long userId) {
        Object followingIdObj = cacheManager.getHashCache(
                UserCacheConstant.USER_FOLLOWING_KEY_PREFIX + userId, targetUserId.toString()
        );
        if (followingIdObj == null) {
            return false;
        }
        Long followingId = ((Number) followingIdObj).longValue();
        return !followingId.equals(UserCacheConstant.UN_FOLLOWING_CONSTANT);
    }

    @Override
    public List<UserVO> getFollowingList(User loginUser) {
        return List.of();
    }

    @Override
    public List<UserVO> getFollowerList(User loginUser) {
        return List.of();
    }

    private void putCaffineIfPresent(User loginUser, Long targetUserId, Integer followState) {
        if(loginUser == null || targetUserId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数错误");
        }
        String followingHashKey = UserCacheConstant.USER_FOLLOWING_KEY_PREFIX + loginUser.getId();
        String followingFieldKey = targetUserId.toString();

        cacheManager.putIfPresentLocalHash(followingHashKey, followingFieldKey, followState);
        cacheManager.putThumbCountIfPresentLocal(UserCacheConstant.getPostCacheKey(followingFieldKey), followState);

        String followerHashKey = UserCacheConstant.USER_FOLLOWER_KEY_PREFIX + targetUserId;
        String followerFieldKey = loginUser.getId().toString();

        cacheManager.putIfPresentLocalHash(followerHashKey, followerFieldKey, followState);
        cacheManager.putThumbCountIfPresentLocal(UserCacheConstant.getPostCacheKey(followerFieldKey), followState);
    }
}
