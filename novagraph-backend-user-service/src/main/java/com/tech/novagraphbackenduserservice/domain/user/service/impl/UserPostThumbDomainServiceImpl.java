package com.tech.novagraphbackenduserservice.domain.user.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tech.novagraphbackendcommon.cache.CacheManager;
import com.tech.novagraphbackendcommon.cache.valueobject.LuaStatusEnum;
import com.tech.novagraphbackendcommon.cache.valueobject.UserActionEnum;
import com.tech.novagraphbackendcommon.exception.BusinessException;
import com.tech.novagraphbackendcommon.exception.ErrorCode;
import com.tech.novagraphbackendcommon.utils.CacheUtils;
import com.tech.novagraphbackendmodel.dto.user.PostThumbRequest;
import com.tech.novagraphbackendmodel.graph.constant.ScreenplayCacheConstant;
import com.tech.novagraphbackendmodel.user.constant.UserCacheConstant;
import com.tech.novagraphbackendmodel.user.constant.UserRedisLuaScriptConstant;
import com.tech.novagraphbackendmodel.user.entity.User;
import com.tech.novagraphbackendmodel.user.entity.UserPostThumb;
import com.tech.novagraphbackendmodel.vo.user.UserPostVO;
import com.tech.novagraphbackenduserservice.domain.user.service.UserPostThumbDomainService;
import com.tech.novagraphbackenduserservice.infrastructure.mapper.UserPostThumbMapper;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class UserPostThumbDomainServiceImpl extends ServiceImpl<UserPostThumbMapper, UserPostThumb>
        implements UserPostThumbDomainService {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private CacheManager cacheManager;

    @Override
    public Boolean doThumb(PostThumbRequest postThumbRequest, User loginUser) {
        if (postThumbRequest == null || postThumbRequest.getPostId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数错误");
        }
        Long postId = postThumbRequest.getPostId();
        String timeSlice = CacheUtils.getTimeSlice();

        String tempThumbKey = UserCacheConstant.buildRedisKey(UserCacheConstant.getTempThumbKey(timeSlice));
        String userThumbKey = UserCacheConstant.buildRedisKey(UserCacheConstant.getUserThumbKey(loginUser.getId()));
        String postKey = UserCacheConstant.buildRedisKey(UserCacheConstant.getPostCacheKey(postId.toString()));
        long result = redisTemplate.execute(
                UserRedisLuaScriptConstant.POST_THUMB_SCRIPT,
                Arrays.asList(tempThumbKey, userThumbKey, postKey),
                loginUser.getId(),
                postId
        );
        if (LuaStatusEnum.FAIL.getValue() == result) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户已点赞");
        }
        // 如果存在本地缓存，则写入
        putCaffineIfPresent(loginUser, postId, UserActionEnum.INCR.getValue());

        // 更新成功才执行
        return LuaStatusEnum.SUCCESS.getValue() == result;
    }

    @Override
    public Boolean undoThumb(PostThumbRequest postThumbRequest, User loginUser) {
        if (postThumbRequest == null || postThumbRequest.getPostId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数错误");
        }
        Long postId = postThumbRequest.getPostId();
        String timeSlice = CacheUtils.getTimeSlice();
        String tempThumbKey = UserCacheConstant.buildRedisKey(UserCacheConstant.getTempThumbKey(timeSlice));
        String userThumbKey = UserCacheConstant.buildRedisKey(UserCacheConstant.getUserThumbKey(loginUser.getId()));
        String postKey = UserCacheConstant.buildRedisKey(UserCacheConstant.getPostCacheKey(postId.toString()));
        long result = redisTemplate.execute(
                UserRedisLuaScriptConstant.POST_UNTHUMB_SCRIPT,
                Arrays.asList(tempThumbKey, userThumbKey, postKey),
                loginUser.getId(),
                postId
        );

        // 根据返回值处理结果
        if (result == LuaStatusEnum.FAIL.getValue()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户未点赞");
        }

        // 如果存在本地缓存，则写入
        putCaffineIfPresent(loginUser, postId, UserActionEnum.DECR.getValue());

        return LuaStatusEnum.SUCCESS.getValue() == result;
    }

    @Override
    public Boolean hasThumb(Long postId, Long userId) {
        Object thumbIdObj = cacheManager.getHashCache(
                UserCacheConstant.USER_POST_THUMB_KEY_PREFIX + userId, postId.toString()
        );
        if (thumbIdObj == null) {
            return false;
        }
        Long thumbId = ((Number) thumbIdObj).longValue();
        return !thumbId.equals(UserCacheConstant.UN_THUMB_CONSTANT);
    }

    @Override
    public List<UserPostVO> getPostThumbStatus(List<UserPostVO> userPostVOList, User loginUser) {
        return List.of();
    }

    private void putCaffineIfPresent(User loginUser, Long postId, Integer thumbState){
        if (loginUser == null || postId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数错误");
        }
        String hashKey = UserCacheConstant.USER_POST_THUMB_KEY_PREFIX + loginUser.getId();
        String fieldKey = postId.toString();
        cacheManager.putIfPresentLocalHash(hashKey, fieldKey, thumbState);
        cacheManager.putThumbCountIfPresentLocal(ScreenplayCacheConstant.getScreenplayCacheKey(fieldKey), thumbState);
    }
}
