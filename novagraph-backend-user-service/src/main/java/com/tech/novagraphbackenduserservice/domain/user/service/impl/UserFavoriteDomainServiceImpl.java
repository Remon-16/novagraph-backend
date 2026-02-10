package com.tech.novagraphbackenduserservice.domain.user.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tech.novagraphbackendcommon.cache.ValuePageCacheTemplate;
import com.tech.novagraphbackendcommon.cache.bean.ValueQueryBean;
import com.tech.novagraphbackendcommon.exception.BusinessException;
import com.tech.novagraphbackendcommon.exception.ErrorCode;
import com.tech.novagraphbackendcommon.exception.ThrowUtils;
import com.tech.novagraphbackendcommon.utils.CacheUtils;
import com.tech.novagraphbackendmodel.dto.user.UserFavoriteAddRequest;
import com.tech.novagraphbackendmodel.dto.user.UserFavoriteDelRequest;
import com.tech.novagraphbackendmodel.dto.user.UserFavoriteQueryRequest;
import com.tech.novagraphbackendmodel.user.constant.UserCacheConstant;
import com.tech.novagraphbackendmodel.user.entity.UserFavorite;
import com.tech.novagraphbackendmodel.vo.user.UserFavoriteVO;
import com.tech.novagraphbackenduserservice.domain.user.service.UserFavoriteDomainService;
import com.tech.novagraphbackenduserservice.infrastructure.mapper.UserFavoriteMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class UserFavoriteDomainServiceImpl extends ServiceImpl<UserFavoriteMapper, UserFavorite>
        implements UserFavoriteDomainService {

    @Resource
    private ValuePageCacheTemplate valuePageCacheTemplate;

    @Override
    public void addUserFavorite(UserFavoriteAddRequest userFavoriteAddRequest) {
        ThrowUtils.throwIf(userFavoriteAddRequest == null, new BusinessException(ErrorCode.PARAMS_ERROR));
        if(userFavoriteAddRequest.getUserId() == null || userFavoriteAddRequest.getScreenplayId() == null || userFavoriteAddRequest.getFolderId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        UserFavorite userFavorite = new UserFavorite();
        userFavorite.setUserId(userFavoriteAddRequest.getUserId());
        userFavorite.setScreenplayId(userFavoriteAddRequest.getScreenplayId());
        userFavorite.setFolderId(userFavoriteAddRequest.getFolderId());
        this.save(userFavorite);
    }

    @Override
    public void deleteUserFavorite(UserFavoriteDelRequest userFavoriteDelRequest) {
        ThrowUtils.throwIf(userFavoriteDelRequest == null, new BusinessException(ErrorCode.PARAMS_ERROR));
        ThrowUtils.throwIf(userFavoriteDelRequest.getFavoriteId() == null, new BusinessException(ErrorCode.PARAMS_ERROR));
        this.removeById(userFavoriteDelRequest.getFavoriteId());
    }

    @Override
    public Page<UserFavoriteVO> getUserFavoriteVOPage(UserFavoriteQueryRequest userFavoriteQueryRequest) {
        int size = userFavoriteQueryRequest.getPageSize();
        int current = userFavoriteQueryRequest.getCurrent();

        String cacheKey = UserCacheConstant.getUserFavoriteKey(CacheUtils.getHexLockString(userFavoriteQueryRequest));

        ValueQueryBean valueQueryBean = new ValueQueryBean();
        valueQueryBean.setCacheKey(cacheKey);
        valueQueryBean.setVOClass(UserFavoriteVO.class);

        return valuePageCacheTemplate.valueQuery(userFavoriteQueryRequest, valueQueryBean,
                () -> this.page(new Page<>(current, size), this.getQueryWrapper(userFavoriteQueryRequest)),
                UserFavoriteVO::listObjToVo);
    }

    private QueryWrapper<UserFavorite> getQueryWrapper(UserFavoriteQueryRequest userFavoriteQueryRequest){
        QueryWrapper<UserFavorite> queryWrapper = new QueryWrapper<>();
        if(userFavoriteQueryRequest == null){
            return queryWrapper;
        }
        Long userId = userFavoriteQueryRequest.getUserId();
        Long favoriteId = userFavoriteQueryRequest.getFavoriteId();
        Long spId = userFavoriteQueryRequest.getScreenplayId();
        ThrowUtils.throwIf(userId == null, new BusinessException(ErrorCode.PARAMS_ERROR));
        queryWrapper.eq("userId", userId);
        queryWrapper.eq(ObjectUtil.isNotEmpty(favoriteId), "favoriteId", favoriteId);
        queryWrapper.eq(ObjectUtil.isNotEmpty(spId), "screenplayId", spId);
        String sortField = userFavoriteQueryRequest.getSortField();
        String sortOrder = userFavoriteQueryRequest.getSortOrder();
        queryWrapper.orderBy(StrUtil.isNotEmpty(sortField), sortOrder.equals("ascend"), sortField);
        return queryWrapper;
    }
}
