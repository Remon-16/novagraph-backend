package com.tech.novagraphbackenduserservice.domain.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tech.novagraphbackendcommon.cache.CacheManager;
import com.tech.novagraphbackendcommon.cache.ValuePageCacheTemplate;
import com.tech.novagraphbackendcommon.cache.ZSetPageCacheTemplate;
import com.tech.novagraphbackendcommon.cache.bean.ValueQueryBean;
import com.tech.novagraphbackendcommon.cache.bean.ZSetQueryBean;
import com.tech.novagraphbackendcommon.cache.bean.ZSetSaveBean;
import com.tech.novagraphbackendcommon.cache.valueobject.LuaStatusEnum;
import com.tech.novagraphbackendcommon.cache.valueobject.UserActionEnum;
import com.tech.novagraphbackendcommon.exception.BusinessException;
import com.tech.novagraphbackendcommon.exception.ErrorCode;
import com.tech.novagraphbackendcommon.exception.ThrowUtils;
import com.tech.novagraphbackendcommon.utils.CacheUtils;
import com.tech.novagraphbackendcommon.utils.ToolUtils;
import com.tech.novagraphbackendmodel.dto.user.UserPlayHistoryAddRequest;
import com.tech.novagraphbackendmodel.dto.user.UserPlayHistoryQueryRequest;
import com.tech.novagraphbackendmodel.user.constant.UserCacheConstant;
import com.tech.novagraphbackendmodel.user.constant.UserRedisLuaScriptConstant;
import com.tech.novagraphbackendmodel.user.entity.User;
import com.tech.novagraphbackendmodel.user.entity.UserPlayHistory;
import com.tech.novagraphbackendmodel.user.entity.UserPost;
import com.tech.novagraphbackendmodel.vo.user.UserPlayHistoryVO;
import com.tech.novagraphbackendserviceclient.GraphFeignClient;
import com.tech.novagraphbackenduserservice.domain.user.service.UserPlayHistoryDomainService;
import com.tech.novagraphbackenduserservice.infrastructure.mapper.UserPlayHistoryMapper;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class UserPlayHistoryDomainServiceImpl extends ServiceImpl<UserPlayHistoryMapper, UserPlayHistory>
        implements UserPlayHistoryDomainService {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private CacheManager cacheManager;

    @Resource
    private ZSetPageCacheTemplate ZSetPageCacheTemplate;

    @Resource
    private GraphFeignClient graphFeignClient;

    @Override
    public void addUserPlayHistory(UserPlayHistoryAddRequest userPlayHistoryAddRequest, User loginUser) {
        if(userPlayHistoryAddRequest == null || userPlayHistoryAddRequest.getScreenplayId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数错误");
        }
        Long screenplayId = userPlayHistoryAddRequest.getScreenplayId();
        String timeSlice = CacheUtils.getTimeSlice();

        String tempHisKey = UserCacheConstant.buildRedisKey(UserCacheConstant.getTempHisKey(timeSlice));
        String userHisKey = UserCacheConstant.buildRedisKey(UserCacheConstant.getUserHisStoredKey(loginUser.getId()));
        String userHisTotalKey = UserCacheConstant.buildRedisKey(UserCacheConstant.getUserHisTotalKey(loginUser.getId()));
        String SPHisKey = UserCacheConstant.buildRedisKey(UserCacheConstant.getSpHisKey(screenplayId.toString()));

        String score = ToolUtils.getNowTimeString();
        Integer expireTime = cacheManager.getOneMonth();

        redisTemplate.execute(
                UserRedisLuaScriptConstant.SP_HIS_ADD_SCRIPT,
                Arrays.asList(tempHisKey, userHisKey, SPHisKey, userHisTotalKey),
                loginUser.getId(),
                screenplayId,
                score,
                expireTime
        );

        // 如果存在本地缓存，则写入
        putCaffineIfPresent(loginUser, screenplayId, UserActionEnum.INCR.getValue());

    }

    @Override
    public Page<UserPlayHistoryVO> getUserPlayHistory(UserPlayHistoryQueryRequest userPlayHistoryQueryRequest) {
        ThrowUtils.throwIf(userPlayHistoryQueryRequest == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(userPlayHistoryQueryRequest.getLoginUser() == null, ErrorCode.PARAMS_ERROR);
        String order = userPlayHistoryQueryRequest.getSortOrder();
        Long loginUserId = userPlayHistoryQueryRequest.getLoginUser().getId();
        String sortedKey = UserCacheConstant.getUserHisStoredKey(loginUserId);
        String sortedTotalKey = UserCacheConstant.getUserHisTotalKey(loginUserId);

        ZSetQueryBean ZSetQueryBean = new ZSetQueryBean();
        ZSetSaveBean zSetSaveBean = new ZSetSaveBean();

        long current =userPlayHistoryQueryRequest.getCurrent();
        long size = userPlayHistoryQueryRequest.getPageSize();

        ZSetQueryBean.setSortedKey(sortedKey);
        ZSetQueryBean.setPage(current);
        ZSetQueryBean.setSize(size);
        ZSetQueryBean.setVOClass(UserPost.class);
        ZSetQueryBean.setSortedTotalKey(sortedTotalKey);
        ZSetQueryBean.setOrder(order);
        ZSetQueryBean.setValueKeyHead(UserCacheConstant.USER_HIS_KEY_PREFIX);

        zSetSaveBean.setValueKeyHead(UserCacheConstant.USER_HIS_KEY_PREFIX);
        zSetSaveBean.setSortedKey(sortedKey);
        zSetSaveBean.setSortedTotalKey(sortedTotalKey);

        return ZSetPageCacheTemplate.zSetQuery(userPlayHistoryQueryRequest, ZSetQueryBean, zSetSaveBean,
                () -> this.page(new Page<>(current, size), getQueryWrapper(userPlayHistoryQueryRequest)),
                this::EntityToVo
        );
    }

    @Override
    public Long getUserPlayHistoryCount(Long spId) {
        String SPHisKey = UserCacheConstant.getSpHisKey(spId.toString());
        Object value = cacheManager.getValueCache(SPHisKey);
        if(value != null) {
            return (Long) value;
        }else {
            return null;
        }
    }

    private QueryWrapper<UserPlayHistory> getQueryWrapper(UserPlayHistoryQueryRequest userPlayHistoryQueryRequest){
        QueryWrapper<UserPlayHistory> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", userPlayHistoryQueryRequest.getLoginUser().getId());
        return queryWrapper;
    }

    private List<UserPlayHistoryVO> EntityToVo(List<UserPlayHistory> userPlayHistoryList){
        List<UserPlayHistoryVO> userPlayHistoryVOList = new ArrayList<>();
        for(UserPlayHistory userPlayHistory : userPlayHistoryList){
            UserPlayHistoryVO userPlayHistoryVO = new UserPlayHistoryVO();
            BeanUtils.copyProperties(userPlayHistory, userPlayHistoryVO);
            userPlayHistoryVO.setScreenplayVo(graphFeignClient.getScreenplayById(userPlayHistory.getScreenplayId()));
            userPlayHistoryVOList.add(userPlayHistoryVO);
        }
        return userPlayHistoryVOList;
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
