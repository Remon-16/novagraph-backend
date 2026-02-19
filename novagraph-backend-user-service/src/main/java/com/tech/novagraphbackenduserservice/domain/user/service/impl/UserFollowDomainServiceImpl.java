package com.tech.novagraphbackenduserservice.domain.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tech.novagraphbackendcommon.cache.CacheManager;
import com.tech.novagraphbackendcommon.cache.valueobject.LuaStatusEnum;
import com.tech.novagraphbackendcommon.cache.valueobject.UserActionEnum;
import com.tech.novagraphbackendcommon.exception.BusinessException;
import com.tech.novagraphbackendcommon.exception.ErrorCode;
import com.tech.novagraphbackendcommon.exception.ThrowUtils;
import com.tech.novagraphbackendcommon.utils.CacheUtils;
import com.tech.novagraphbackendcommon.utils.ToolUtils;
import com.tech.novagraphbackendmodel.dto.user.DoFollowRequest;
import com.tech.novagraphbackendmodel.dto.user.FollowQueryRequest;
import com.tech.novagraphbackendmodel.user.constant.UserCacheConstant;
import com.tech.novagraphbackendmodel.user.constant.UserRedisLuaScriptConstant;
import com.tech.novagraphbackendmodel.user.entity.User;
import com.tech.novagraphbackendmodel.user.entity.UserFollow;
import com.tech.novagraphbackendmodel.vo.user.UserVO;
import com.tech.novagraphbackenduserservice.domain.user.service.UserDomainService;
import com.tech.novagraphbackenduserservice.domain.user.service.UserFollowDomainService;
import com.tech.novagraphbackenduserservice.infrastructure.mapper.UserFollowMapper;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

@Service
public class UserFollowDomainServiceImpl extends ServiceImpl<UserFollowMapper, UserFollow>
        implements UserFollowDomainService {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private CacheManager cacheManager;

    @Resource
    private UserDomainService userDomainService;

    private final static String FOLLOWING = "following";

    private final static String FOLLOWER = "follower";

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

        String score = ToolUtils.getNowTimeString();
        Integer expireTime = cacheManager.getOneMonth();

        long result = redisTemplate.execute(
                UserRedisLuaScriptConstant.USER_FOLLOW_SCRIPT,
                Arrays.asList(tempFollowKey, UserFollowingKey, UserFollowerKey, UserFollowingCountKey, UserFollowerCountKey),
                loginUser.getId(),
                targetUserId,
                score,
                expireTime
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
        Integer expireTime = cacheManager.getOneMonth();

        String tempFollowKey = UserCacheConstant.buildRedisKey(UserCacheConstant.getUserFollowTempKey(timeSlice));
        String UserFollowingKey = UserCacheConstant.buildRedisKey(UserCacheConstant.getUserFollowingKey(userId.toString()));
        String UserFollowerKey = UserCacheConstant.buildRedisKey(UserCacheConstant.getUserFollowerKey(targetUserId.toString()));
        String UserFollowingCountKey = UserCacheConstant.buildRedisKey(UserCacheConstant.getUserFollowingCountKey(userId.toString()));
        String UserFollowerCountKey = UserCacheConstant.buildRedisKey(UserCacheConstant.getUserFollowerCountKey(targetUserId.toString()));
        long result = redisTemplate.execute(
                UserRedisLuaScriptConstant.USER_UNFOLLOW_SCRIPT,
                Arrays.asList(tempFollowKey, UserFollowingKey, UserFollowerKey, UserFollowingCountKey, UserFollowerCountKey),
                loginUser.getId(),
                targetUserId,
                expireTime
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
    public Page<UserVO> getFollowingPage(FollowQueryRequest followQueryRequest) {
        ThrowUtils.throwIf(followQueryRequest == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(followQueryRequest.getLoginUser() == null, ErrorCode.PARAMS_ERROR);
        Long userId = followQueryRequest.getLoginUser().getId();
        String UserFollowingCountKey = UserCacheConstant.buildRedisKey(UserCacheConstant.getUserFollowingCountKey(userId.toString()));
        String UserFollowingKey = UserCacheConstant.buildRedisKey(UserCacheConstant.getUserFollowingKey(userId.toString()));
        int size = followQueryRequest.getPageSize();
        int current = followQueryRequest.getCurrent();
        Set<Object> values = cacheManager.zSetPageQuery(UserFollowingKey, (long) current, (long) size);
        Object totalValue = cacheManager.getValueCache(UserFollowingCountKey);
        List<UserVO> userVOList = new ArrayList<>();
        values.forEach(value -> {
            Long followingId = ((Number) value).longValue();
            UserVO userVO = userDomainService.getUserVOById(followingId);
            userVOList.add(userVO);
        });
        Page<UserVO> page = new Page<>(current, size);
        page.setRecords(userVOList);
        page.setTotal((Long) totalValue);
        return page;
    }

    @Override
    public Page<UserVO> getFollowerPage(FollowQueryRequest followQueryRequest) {
        ThrowUtils.throwIf(followQueryRequest == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(followQueryRequest.getLoginUser() == null, ErrorCode.PARAMS_ERROR);
        Long userId = followQueryRequest.getLoginUser().getId();
        String UserFollowerCountKey = UserCacheConstant.buildRedisKey(UserCacheConstant.getUserFollowerCountKey(userId.toString()));
        String UserFollowerKey = UserCacheConstant.buildRedisKey(UserCacheConstant.getUserFollowerKey(userId.toString()));
        int size = followQueryRequest.getPageSize();
        int current = followQueryRequest.getCurrent();
        Set<Object> values = cacheManager.zSetPageQuery(UserFollowerKey, (long) current, (long) size);
        Object totalValue = cacheManager.getValueCache(UserFollowerCountKey);
        List<UserVO> userVOList = new ArrayList<>();
        values.forEach(value -> {
            Long followerId = ((Number) value).longValue();
            UserVO userVO = userDomainService.getUserVOById(followerId);
            userVOList.add(userVO);
        });
        Page<UserVO> page = new Page<>(current, size);
        page.setRecords(userVOList);
        page.setTotal((Long) totalValue);
        return page;
    }

    @Override
    public void putUserFollowToCache(Long userId) {
        QueryWrapper<UserFollow> followingQueryWrapper = this.getQueryWrapper(userId, FOLLOWING);
        QueryWrapper<UserFollow> followerQueryWrapper = this.getQueryWrapper(userId, FOLLOWER);
        List<UserFollow> followingList = this.list(followingQueryWrapper);
        List<UserFollow> followerList = this.list(followerQueryWrapper);

        String UserFollowingCountKey = UserCacheConstant.getUserFollowingCountKey(userId.toString());
        String UserFollowingKey = UserCacheConstant.buildRedisKey(UserCacheConstant.getUserFollowingKey(userId.toString()));

        String UserFollowerCountKey = UserCacheConstant.getUserFollowerCountKey(userId.toString());
        String UserFollowerKey = UserCacheConstant.buildRedisKey(UserCacheConstant.getUserFollowerKey(userId.toString()));

        followingList.forEach(UserFollow -> {
            Double score = (double) UserFollow.getCreateTime().getTime();
            cacheManager.zSetAdd(UserFollowingKey, UserFollow.getFollowingId(), score);
        });
        cacheManager.putValueToCache(UserFollowingCountKey, followingList.size());

        followerList.forEach(UserFollow -> {
            Double score = (double) UserFollow.getCreateTime().getTime();
            cacheManager.zSetAdd(UserFollowerKey, UserFollow.getUserId(), score);
        });
        cacheManager.putValueToCache(UserFollowerCountKey, followerList.size());
    }

    private QueryWrapper<UserFollow> getQueryWrapper(Long userId, String queryType){
        ThrowUtils.throwIf(userId == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(queryType == null, ErrorCode.PARAMS_ERROR);
        QueryWrapper<UserFollow> queryWrapper = new QueryWrapper<>();
        if(FOLLOWING.equals(queryType)){
            queryWrapper.eq("userId", userId);
        }else {
            queryWrapper.eq("followingId", userId);
        }
        return queryWrapper;
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
