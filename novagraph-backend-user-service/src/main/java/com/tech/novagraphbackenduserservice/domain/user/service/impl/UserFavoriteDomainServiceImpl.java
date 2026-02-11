package com.tech.novagraphbackenduserservice.domain.user.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tech.novagraphbackendcommon.cache.CacheManager;
import com.tech.novagraphbackendcommon.cache.ValuePageCacheTemplate;
import com.tech.novagraphbackendcommon.cache.bean.ValueQueryBean;
import com.tech.novagraphbackendcommon.cache.valueobject.LuaStatusEnum;
import com.tech.novagraphbackendcommon.cache.valueobject.UserActionEnum;
import com.tech.novagraphbackendcommon.exception.BusinessException;
import com.tech.novagraphbackendcommon.exception.ErrorCode;
import com.tech.novagraphbackendcommon.exception.ThrowUtils;
import com.tech.novagraphbackendcommon.utils.CacheUtils;
import com.tech.novagraphbackendmodel.dto.user.UserFavoriteAddRequest;
import com.tech.novagraphbackendmodel.dto.user.UserFavoriteDelRequest;
import com.tech.novagraphbackendmodel.dto.user.UserFavoriteQueryRequest;
import com.tech.novagraphbackendmodel.user.constant.UserCacheConstant;
import com.tech.novagraphbackendmodel.user.constant.UserRedisLuaScriptConstant;
import com.tech.novagraphbackendmodel.user.entity.User;
import com.tech.novagraphbackendmodel.user.entity.UserFavorite;
import com.tech.novagraphbackendmodel.vo.user.UserFavoriteVO;
import com.tech.novagraphbackenduserservice.domain.user.service.UserFavoriteDomainService;
import com.tech.novagraphbackenduserservice.infrastructure.mapper.UserFavoriteMapper;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
public class UserFavoriteDomainServiceImpl extends ServiceImpl<UserFavoriteMapper, UserFavorite>
        implements UserFavoriteDomainService {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private CacheManager cacheManager;

    @Resource
    private ValuePageCacheTemplate valuePageCacheTemplate;

    @Override
    public void addUserFavorite(UserFavoriteAddRequest userFavoriteAddRequest) {
        ThrowUtils.throwIf(userFavoriteAddRequest == null, new BusinessException(ErrorCode.PARAMS_ERROR));
        if(userFavoriteAddRequest.getUserId() == null || userFavoriteAddRequest.getScreenplayId() == null || userFavoriteAddRequest.getFolderId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long userId = userFavoriteAddRequest.getUserId();
        Long screenplayId = userFavoriteAddRequest.getScreenplayId();
        Long folderId = userFavoriteAddRequest.getFolderId();
        String timeSlice = CacheUtils.getTimeSlice();

        String tempKey = UserCacheConstant.buildRedisKey(UserCacheConstant.getTempUserFavoriteKey(timeSlice));
        String userFavoriteKey = UserCacheConstant.buildRedisKey(UserCacheConstant.getUserFavoriteKey(userId));
        String spFavoriteKey = UserCacheConstant.buildRedisKey(UserCacheConstant.getSpFavoriteKey(screenplayId));

        long result = redisTemplate.execute(
                UserRedisLuaScriptConstant.SP_FAVORITE_SCRIPT,
                Arrays.asList(tempKey, userFavoriteKey, spFavoriteKey),
                userId,
                screenplayId,
                folderId
        );

        putCaffineIfPresent(userId, screenplayId, folderId, UserActionEnum.INCR.getValue());
        ThrowUtils.throwIf(LuaStatusEnum.SUCCESS.getValue() != result, ErrorCode.SYSTEM_ERROR, "收藏失败，请稍后重试");
    }

    @Override
    public void deleteUserFavorite(UserFavoriteDelRequest userFavoriteDelRequest) {
        ThrowUtils.throwIf(userFavoriteDelRequest == null, new BusinessException(ErrorCode.PARAMS_ERROR));
        if(userFavoriteDelRequest.getUserId() == null || userFavoriteDelRequest.getScreenplayId() == null || userFavoriteDelRequest.getFolderId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long userId = userFavoriteDelRequest.getUserId();
        Long screenplayId = userFavoriteDelRequest.getScreenplayId();
        Long folderId = userFavoriteDelRequest.getFolderId();
        String timeSlice = CacheUtils.getTimeSlice();

        String tempKey = UserCacheConstant.buildRedisKey(UserCacheConstant.getTempUserFavoriteKey(timeSlice));
        String userFavoriteKey = UserCacheConstant.buildRedisKey(UserCacheConstant.getUserFavoriteKey(userId));
        String spFavoriteKey = UserCacheConstant.buildRedisKey(UserCacheConstant.getSpFavoriteKey(screenplayId));

        long result = redisTemplate.execute(
                UserRedisLuaScriptConstant.SP_UNFAVORITE_SCRIPT,
                Arrays.asList(tempKey, userFavoriteKey, spFavoriteKey),
                userId,
                screenplayId,
                folderId
        );
        putCaffineIfPresent(userId, screenplayId, folderId, UserActionEnum.DECR.getValue());
        ThrowUtils.throwIf(LuaStatusEnum.SUCCESS.getValue() != result, ErrorCode.SYSTEM_ERROR, "取消收藏失败，请稍后重试");
    }

    @Override
    public Page<UserFavoriteVO> getUserFavoriteVOPage(UserFavoriteQueryRequest userFavoriteQueryRequest) {
        int size = userFavoriteQueryRequest.getPageSize();
        int current = userFavoriteQueryRequest.getCurrent();
        return null;
    }

    private void putCaffineIfPresent(Long userId, Long screenplayId, Long folderId, Integer favoriteState){
        if(userId == null || screenplayId == null || folderId == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String hashKey = UserCacheConstant.USER_FAVORITE_KEY_PREFIX + userId;
        String fieldKey = screenplayId + ":" + folderId;
        cacheManager.putIfPresentLocalHash(hashKey, fieldKey, favoriteState);
        cacheManager.putThumbCountIfPresentLocal(UserCacheConstant.getSpFavoriteKey(screenplayId), favoriteState);
    }
}
