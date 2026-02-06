package com.tech.novagraphbackenduserservice.domain.user.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tech.novagraphbackendcommon.cache.CacheManager;
import com.tech.novagraphbackendcommon.cache.valueobject.LuaStatusEnum;
import com.tech.novagraphbackendcommon.cache.valueobject.UserActionEnum;
import com.tech.novagraphbackendcommon.exception.BusinessException;
import com.tech.novagraphbackendcommon.exception.ErrorCode;
import com.tech.novagraphbackendcommon.utils.CacheUtils;
import com.tech.novagraphbackendmodel.dto.user.UserPlayHistoryAddRequest;
import com.tech.novagraphbackendmodel.user.constant.UserCacheConstant;
import com.tech.novagraphbackendmodel.user.constant.UserRedisLuaScriptConstant;
import com.tech.novagraphbackendmodel.user.entity.User;
import com.tech.novagraphbackendmodel.user.entity.UserPlayHistory;
import com.tech.novagraphbackendmodel.vo.user.UserPlayHistoryVO;
import com.tech.novagraphbackenduserservice.domain.user.service.UserPlayHistoryDomainService;
import com.tech.novagraphbackenduserservice.infrastructure.mapper.UserPlayHistoryMapper;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class UserPlayHistoryDomainServiceImpl extends ServiceImpl<UserPlayHistoryMapper, UserPlayHistory>
        implements UserPlayHistoryDomainService {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private CacheManager cacheManager;

    @Override
    public void addUserPlayHistory(UserPlayHistoryAddRequest userPlayHistoryAddRequest, User loginUser) {
        if(userPlayHistoryAddRequest == null || userPlayHistoryAddRequest.getScreenplayId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数错误");
        }
        Long screenplayId = userPlayHistoryAddRequest.getScreenplayId();
        String timeSlice = CacheUtils.getTimeSlice();

        String tempHisKey = UserCacheConstant.buildRedisKey(UserCacheConstant.getTempHisKey(timeSlice));
        String userHisKey = UserCacheConstant.buildRedisKey(UserCacheConstant.getUserHisKey(loginUser.getId()));
        String SPHisKey = UserCacheConstant.buildRedisKey(UserCacheConstant.getSpHisKey(screenplayId.toString()));

        redisTemplate.execute(
                UserRedisLuaScriptConstant.POST_THUMB_SCRIPT,
                Arrays.asList(tempHisKey, userHisKey, SPHisKey),
                loginUser.getId(),
                screenplayId
        );

        // 如果存在本地缓存，则写入
        putCaffineIfPresent(loginUser, screenplayId, UserActionEnum.INCR.getValue());

    }

    @Override
    public List<UserPlayHistoryVO> getUserPlayHistory(User loginUser) {
        return List.of();
    }

    private void putCaffineIfPresent(User loginUser, Long screenplayId, Integer thumbState){
        if (loginUser == null || screenplayId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数错误");
        }
        String hashKey = UserCacheConstant.USER_HIS_KEY_PREFIX + loginUser.getId();
        String fieldKey = screenplayId.toString();
        cacheManager.putIfPresentLocalHash(hashKey, fieldKey, thumbState);
        cacheManager.putThumbCountIfPresentLocal(UserCacheConstant.getSpHisKey(fieldKey), thumbState);
    }
}
