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
import com.tech.novagraphbackendmodel.dto.user.UserFavoriteAddRequest;
import com.tech.novagraphbackendmodel.dto.user.UserFavoriteDelRequest;
import com.tech.novagraphbackendmodel.dto.user.UserFavoriteQueryRequest;
import com.tech.novagraphbackendmodel.user.constant.UserCacheConstant;
import com.tech.novagraphbackendmodel.user.constant.UserRedisLuaScriptConstant;
import com.tech.novagraphbackendmodel.user.entity.UserFavorite;
import com.tech.novagraphbackendmodel.vo.user.UserFavoriteVO;
import com.tech.novagraphbackendserviceclient.GraphFeignClient;
import com.tech.novagraphbackenduserservice.domain.user.service.UserFavoriteDomainService;
import com.tech.novagraphbackenduserservice.domain.user.service.UserFavoriteFolderDomainService;
import com.tech.novagraphbackenduserservice.infrastructure.mapper.UserFavoriteMapper;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

@Service
public class UserFavoriteDomainServiceImpl extends ServiceImpl<UserFavoriteMapper, UserFavorite>
        implements UserFavoriteDomainService {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private CacheManager cacheManager;

    @Resource
    private GraphFeignClient graphFeignClient;

    @Resource
    private UserFavoriteFolderDomainService userFavoriteFolderDomainService;

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
        String userFavoriteTotalKey = UserCacheConstant.buildRedisKey(UserCacheConstant.getUserFavoriteTotalKey(userId));

        String now = ToolUtils.getNowTimeString();

        Integer expireTime = cacheManager.getOneMonth();

        long result = redisTemplate.execute(
                UserRedisLuaScriptConstant.SP_FAVORITE_SCRIPT,
                Arrays.asList(tempKey, userFavoriteKey, spFavoriteKey, userFavoriteTotalKey),
                userId,
                screenplayId,
                folderId,
                now,
                expireTime
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

        Integer expireTime = cacheManager.getOneMonth();

        long result = redisTemplate.execute(
                UserRedisLuaScriptConstant.SP_UNFAVORITE_SCRIPT,
                Arrays.asList(tempKey, userFavoriteKey, spFavoriteKey),
                userId,
                screenplayId,
                folderId,
                expireTime
        );
        putCaffineIfPresent(userId, screenplayId, folderId, UserActionEnum.DECR.getValue());
        ThrowUtils.throwIf(LuaStatusEnum.SUCCESS.getValue() != result, ErrorCode.SYSTEM_ERROR, "取消收藏失败，请稍后重试");
    }

    @Override
    public Page<UserFavoriteVO> getUserFavoriteVOPage(UserFavoriteQueryRequest userFavoriteQueryRequest) {
        int size = userFavoriteQueryRequest.getPageSize();
        int current = userFavoriteQueryRequest.getCurrent();
        Long userId = userFavoriteQueryRequest.getUserId();

        String userFavoriteKey = UserCacheConstant.buildRedisKey(UserCacheConstant.getUserFavoriteKey(userId));
        String userFavoriteTotalKey = UserCacheConstant.buildRedisKey(UserCacheConstant.getUserFavoriteTotalKey(userId));
        Set<Object> values = cacheManager.zSetPageQuery(userFavoriteKey, (long) current, (long) size);
        Object totalValue = cacheManager.getValueCache(userFavoriteTotalKey);
        List<UserFavoriteVO> resList = new ArrayList<>();
        values.forEach(value -> {
            String stringValue = (String) value;
            UserFavoriteVO userFavoriteVO = new UserFavoriteVO();
            String[] splitStr = stringValue.split(":");
            String screenplayIdStr = splitStr[0];
            String folderIdStr = splitStr[1];
            userFavoriteVO.setScreenplayId(Long.valueOf(screenplayIdStr));
            userFavoriteVO.setScreenplayVo(graphFeignClient.getScreenplayById(Long.valueOf(screenplayIdStr)));
            userFavoriteVO.setFolderId(Long.valueOf(folderIdStr));
            userFavoriteVO.setFolderName(userFavoriteFolderDomainService.getFolderNameById(Long.valueOf(folderIdStr), userId));
            userFavoriteVO.setUserId(userId);
            resList.add(userFavoriteVO);
        });
        Page<UserFavoriteVO> page = new Page<>();
        page.setCurrent(current);
        page.setSize(size);
        page.setTotal((Long) totalValue);
        page.setRecords(resList);
        return page;
    }

    @Override
    public List<UserFavorite> getUserFavoriteList(Long userId) {
        ThrowUtils.throwIf(userId == null, new BusinessException(ErrorCode.PARAMS_ERROR));
        QueryWrapper<UserFavorite> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", userId);
        return list(queryWrapper);
    }

    @Override
    public void putFavoriteListToCache(List<UserFavorite> userFavoriteList, Long userId) {
        String userFavoriteKey = UserCacheConstant.getUserFavoriteKey(userId);
        userFavoriteList.forEach(uFavorite -> {
            String hash = uFavorite.getScreenplayId() + ":" + uFavorite.getFolderId();
            Double score = (double) uFavorite.getCreateTime().getTime();
            cacheManager.zSetAdd(userFavoriteKey, hash, score,cacheManager.getOneMonth());
        });
    }

    @Override
    public void putFavoriteListToCache(Long userId) {
        ThrowUtils.throwIf(userId == null, new BusinessException(ErrorCode.PARAMS_ERROR));
        List<UserFavorite> userFavoriteList = this.getUserFavoriteList(userId);
        this.putFavoriteListToCache(userFavoriteList, userId);
    }

    @Override
    public UserFavoriteVO userHasFavorite(Long screenplayId, Long userId) {
        // TODO 换缓存 一般不集中查询
        QueryWrapper<UserFavorite> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", userId);
        queryWrapper.eq("screenplayId", screenplayId);
        UserFavorite userFavorite = this.getOne(queryWrapper);

        if (userFavorite != null) {
            UserFavoriteVO userFavoriteVO = UserFavoriteVO.objToVo(userFavorite);
            userFavoriteVO.setHasFavorite(true);
            return userFavoriteVO;
        }
        return null;
    }

    @Override
    public Long getUserFavoriteCount(Long screenplayId) {
        String spFavoriteKey = UserCacheConstant.getSpFavoriteKey(screenplayId);
        Object value = cacheManager.getValueCache(spFavoriteKey);
        if (value != null) {
            return (Long) value;
        }else {
            return null;
        }
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
