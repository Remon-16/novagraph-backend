package com.tech.novagraphbackendgraphservice.domain.screenplay.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tech.novagraphbackendcommon.cache.CacheManager;
import com.tech.novagraphbackendcommon.exception.BusinessException;
import com.tech.novagraphbackendcommon.exception.ErrorCode;
import com.tech.novagraphbackendcommon.utils.CacheUtils;
import com.tech.novagraphbackendgraphservice.domain.screenplay.service.ScreenplayThumbDomainService;
import com.tech.novagraphbackendgraphservice.infrastructure.mapper.ScreenplayThumbMapper;
import com.tech.novagraphbackendmodel.dto.graph.DoThumbRequest;
import com.tech.novagraphbackendmodel.graph.constant.RedisLuaScriptConstant;
import com.tech.novagraphbackendmodel.graph.constant.ScreenplayCacheConstant;
import com.tech.novagraphbackendmodel.graph.entity.ScreenplayThumb;
import com.tech.novagraphbackendcommon.cache.valueobject.LuaStatusEnum;
import com.tech.novagraphbackendcommon.cache.valueobject.UserActionEnum;
import com.tech.novagraphbackendmodel.user.entity.User;
import com.tech.novagraphbackendmodel.vo.graph.ScreenplayVO;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;


@Service
public class ScreenplayThumbDomainServiceImpl extends ServiceImpl<ScreenplayThumbMapper, ScreenplayThumb>
        implements ScreenplayThumbDomainService {

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
        Long dateScore = new Date().getTime();
        Integer expTime = cacheManager.getRedisZSetExpireTime();

        // 执行 Lua 脚本
        long result = redisTemplate.execute(
                RedisLuaScriptConstant.SCREENPLAY_THUMB_SCRIPT,
                Arrays.asList(tempThumbKey, userThumbKey, screenplayKey),
                loginUser.getId(),
                screenplayId,
                dateScore,
                expTime
        );

        if (LuaStatusEnum.FAIL.getValue() == result) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户已点赞");
        }
        // 如果存在本地缓存，则写入
        putCaffineIfPresent(loginUser, screenplayId, UserActionEnum.INCR.getValue());

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
        Integer expTime = cacheManager.getRedisZSetExpireTime();

        // 执行 Lua 脚本
        long result = redisTemplate.execute(
                RedisLuaScriptConstant.SCREENPLAY_UNTHUMB_SCRIPT,
                Arrays.asList(tempThumbKey, userThumbKey, screenplayKey),
                loginUser.getId(),
                screenplayId,
                expTime
        );
        // 根据返回值处理结果
        if (result == LuaStatusEnum.FAIL.getValue()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户未点赞");
        }

        // 如果存在本地缓存，则写入
        putCaffineIfPresent(loginUser, screenplayId, UserActionEnum.DECR.getValue());

        return LuaStatusEnum.SUCCESS.getValue() == result;
    }

    @Override
    public Boolean hasThumb(Long screenplayId, Long userId) {
        String userThumbKey = ScreenplayCacheConstant.buildRedisKey(ScreenplayCacheConstant.getUserThumbKey(userId));
        Boolean result = cacheManager.zSetUserActionHas(userThumbKey, screenplayId);
        if(result != null){
            return result;
        }else{
            // 缓存没命中
            LambdaQueryWrapper<ScreenplayThumb> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.or().eq(ScreenplayThumb::getScreenplayId, screenplayId)
                    .eq(ScreenplayThumb::getUserId, userId);
            ScreenplayThumb screenplayThumb = this.getOne(queryWrapper);
            String v =  screenplayThumb != null? screenplayId + CacheManager.getUSER_ACTION_HAS(): screenplayId + CacheManager.getUSER_NO_ACTION_HAS();
            Double score = (double) new Date().getTime();
            cacheManager.zSetAdd(userThumbKey, v, score);
            return screenplayThumb != null;
        }
    }

    private List<Boolean> hasThumbBatch(List<Long> screenplayIdList, Long userId){
        String userThumbKey = ScreenplayCacheConstant.buildRedisKey(ScreenplayCacheConstant.getUserThumbKey(userId));
        List<Boolean> hasThumbBatch = new ArrayList<>(screenplayIdList.size());
        List<Integer> lossThumbList = new ArrayList<>();
        int i = 0;
        for (Long screenplayId : screenplayIdList) {
            Boolean result = cacheManager.zSetUserActionHas(userThumbKey, screenplayId);
            if(result != null){
                hasThumbBatch.add(i, result);
            }else{
                lossThumbList.add(i);
            }
            i++;
        }

        if(lossThumbList.isEmpty()){
            return hasThumbBatch;
        }else{
            // 缓存失效的查数据库
            LambdaQueryWrapper<ScreenplayThumb> queryWrapper = new LambdaQueryWrapper<>();
            lossThumbList.forEach(lossThumb -> {
                Long lossThumbId = screenplayIdList.get(lossThumb);
                queryWrapper.or().eq(ScreenplayThumb::getScreenplayId, lossThumbId)
                        .eq(ScreenplayThumb::getUserId, userId);
            });
            List<ScreenplayThumb> resList = this.list(queryWrapper);
            Set<String> existSet = resList.stream()
                    .map(entity -> entity.getUserId() + "_" + entity.getScreenplayId())
                    .collect(Collectors.toSet());

            lossThumbList.forEach(lossThumb -> {
                Long lossThumbId = screenplayIdList.get(lossThumb);
                String k = userId + "_" + lossThumbId;
                hasThumbBatch.add(lossThumb, existSet.contains(k));
                String v =  existSet.contains(k)? lossThumbId + CacheManager.getUSER_ACTION_HAS(): lossThumbId + CacheManager.getUSER_NO_ACTION_HAS();
                Double score = (double) new Date().getTime();
                cacheManager.zSetAdd(userThumbKey, v, score);
            });
        }
        return hasThumbBatch;
    }

    @Override
    public List<ScreenplayVO> getScreenplayThumbState(List<ScreenplayVO> screenplayVOList, User loginUser) {
        List<Long> screenplayIdList = screenplayVOList.stream().map(ScreenplayVO::getId).toList();
        List<Boolean> hasThumbList = this.hasThumbBatch(screenplayIdList, loginUser.getId());
        int i = 0;
        for(ScreenplayVO screenplayVO: screenplayVOList){
            screenplayVO.setHasThumb(hasThumbList.get(i));
        }
        return screenplayVOList;
    }

    private void putCaffineIfPresent(User loginUser, Long screenplayId, Integer thumbState){
        String hashKey = ScreenplayCacheConstant.USER_SCREENPLAY_THUMB_KEY_PREFIX + loginUser.getId();
        String fieldKey = screenplayId.toString();
        cacheManager.putIfPresentLocalHash(hashKey, fieldKey, thumbState);
        cacheManager.putThumbCountIfPresentLocal(ScreenplayCacheConstant.getScreenplayCacheKey(fieldKey), thumbState);
    }
}
