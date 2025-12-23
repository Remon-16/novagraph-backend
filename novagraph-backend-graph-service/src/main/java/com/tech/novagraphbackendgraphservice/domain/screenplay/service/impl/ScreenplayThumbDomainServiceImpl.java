package com.tech.novagraphbackendgraphservice.domain.screenplay.service.impl;

import com.tech.novagraphbackendcommon.cache.CacheManager;
import com.tech.novagraphbackendcommon.exception.BusinessException;
import com.tech.novagraphbackendcommon.exception.ErrorCode;
import com.tech.novagraphbackendcommon.utils.CacheUtils;
import com.tech.novagraphbackendgraphservice.domain.screenplay.service.ScreenplayThumbDomainService;
import com.tech.novagraphbackendmodel.dto.graph.DoThumbRequest;
import com.tech.novagraphbackendmodel.graph.constant.RedisLuaScriptConstant;
import com.tech.novagraphbackendmodel.graph.constant.ScreenplayCacheConstant;
import com.tech.novagraphbackendmodel.graph.valueobject.LuaStatusEnum;
import com.tech.novagraphbackendmodel.graph.valueobject.ThumbTypeEnum;
import com.tech.novagraphbackendmodel.user.entity.User;
import com.tech.novagraphbackendmodel.vo.graph.ScreenplayVO;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;


@Service
public class ScreenplayThumbDomainServiceImpl implements ScreenplayThumbDomainService {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private CacheManager cacheManager;

    @Override
    public Boolean doThumb(DoThumbRequest doThumbRequest, User loginUser) {
        if (doThumbRequest == null || doThumbRequest.getScreenplayId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数错误");
        }
        Long screenplayId = doThumbRequest.getScreenplayId();

        String timeSlice = CacheUtils.getTimeSlice();
        // Redis Key
        String tempThumbKey =  ScreenplayCacheConstant.buildRedisKey(ScreenplayCacheConstant.getTempThumbKey(timeSlice));
        String userThumbKey =  ScreenplayCacheConstant.buildRedisKey(ScreenplayCacheConstant.getUserThumbKey(loginUser.getId()));
        String screenplayKey = ScreenplayCacheConstant.buildRedisKey(ScreenplayCacheConstant.getScreenplayCacheKey(screenplayId.toString()));

        // 执行 Lua 脚本
        long result = redisTemplate.execute(
                RedisLuaScriptConstant.SCREENPLAY_THUMB_SCRIPT,
                Arrays.asList(tempThumbKey, userThumbKey, screenplayKey),
                loginUser.getId(),
                screenplayId
        );

        if (LuaStatusEnum.FAIL.getValue() == result) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户已点赞");
        }
        // 如果存在本地缓存，则写入
        putCaffineIfPresent(loginUser, screenplayId, ThumbTypeEnum.INCR.getValue());

        // 更新成功才执行
        return LuaStatusEnum.SUCCESS.getValue() == result;
    }

    @Override
    public Boolean undoThumb(DoThumbRequest doThumbRequest, User loginUser) {
        if (doThumbRequest == null || doThumbRequest.getScreenplayId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        Long screenplayId = doThumbRequest.getScreenplayId();
        // 计算时间片
        String timeSlice = CacheUtils.getTimeSlice();
        // Redis Key
        String tempThumbKey =  ScreenplayCacheConstant.buildRedisKey(ScreenplayCacheConstant.getTempThumbKey(timeSlice));
        String userThumbKey =  ScreenplayCacheConstant.buildRedisKey(ScreenplayCacheConstant.getUserThumbKey(loginUser.getId()));
        String screenplayKey = ScreenplayCacheConstant.buildRedisKey(ScreenplayCacheConstant.getScreenplayCacheKey(screenplayId.toString()));

        // 执行 Lua 脚本
        long result = redisTemplate.execute(
                RedisLuaScriptConstant.SCREENPLAY_UNTHUMB_SCRIPT,
                Arrays.asList(tempThumbKey, userThumbKey, screenplayKey),
                loginUser.getId(),
                screenplayId
        );
        // 根据返回值处理结果
        if (result == LuaStatusEnum.FAIL.getValue()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户未点赞");
        }

        // 如果存在本地缓存，则写入
        putCaffineIfPresent(loginUser, screenplayId, ThumbTypeEnum.DECR.getValue());

        return LuaStatusEnum.SUCCESS.getValue() == result;
    }

    @Override
    public Boolean hasThumb(Long screenplayId, Long userId) {
        Object thumbIdObj = cacheManager.getHashCache(
                ScreenplayCacheConstant.USER_SCREENPLAY_THUMB_KEY_PREFIX + userId, screenplayId.toString());
        if (thumbIdObj == null) {
            return false;
        }
        Long thumbId = ((Number) thumbIdObj).longValue();
        return !thumbId.equals(ScreenplayCacheConstant.UN_THUMB_CONSTANT);
    }

    @Override
    public List<ScreenplayVO> getScreenplayThumbState(List<ScreenplayVO> screenplayVOList, User loginUser) {
        return List.of();
    }

    private void putCaffineIfPresent(User loginUser, Long screenplayId, Integer thumbState){
        String hashKey = ScreenplayCacheConstant.USER_SCREENPLAY_THUMB_KEY_PREFIX + loginUser.getId();
        String fieldKey = screenplayId.toString();
        cacheManager.putIfPresentLocalHash(hashKey, fieldKey, thumbState);
        cacheManager.putThumbCountIfPresentLocal(ScreenplayCacheConstant.getScreenplayCacheKey(fieldKey), thumbState);
    }
}
