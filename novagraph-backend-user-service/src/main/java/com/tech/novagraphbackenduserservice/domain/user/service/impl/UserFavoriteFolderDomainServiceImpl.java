package com.tech.novagraphbackenduserservice.domain.user.service.impl;

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
import com.tech.novagraphbackendmodel.dto.user.FavoriteFolderQueryRequest;
import com.tech.novagraphbackendmodel.dto.user.UserAddFavoriteFolderRequest;
import com.tech.novagraphbackendmodel.dto.user.UserDelFavoriteFolderRequest;
import com.tech.novagraphbackendmodel.user.constant.UserCacheConstant;
import com.tech.novagraphbackendmodel.user.entity.UserFavoriteFolder;
import com.tech.novagraphbackendmodel.vo.user.UserFavoriteFolderVO;
import com.tech.novagraphbackenduserservice.domain.user.service.UserFavoriteFolderDomainService;
import com.tech.novagraphbackenduserservice.infrastructure.mapper.UserFavoriteFolderMapper;
import jakarta.annotation.Resource;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserFavoriteFolderDomainServiceImpl extends ServiceImpl<UserFavoriteFolderMapper, UserFavoriteFolder>
        implements UserFavoriteFolderDomainService {

    @Resource
    private ValuePageCacheTemplate valuePageCacheTemplate;

    @Override
    public void addUserFavoriteFolder(UserAddFavoriteFolderRequest userAddFavoriteFolderRequest) {
        ThrowUtils.throwIf(userAddFavoriteFolderRequest == null, new BusinessException(ErrorCode.PARAMS_ERROR));
        ThrowUtils.throwIf(StringUtils.isEmpty(userAddFavoriteFolderRequest.getFolderName()), new BusinessException(ErrorCode.PARAMS_ERROR));
        UserFavoriteFolder userFavoriteFolder = new UserFavoriteFolder();
        userFavoriteFolder.setFolderName(userAddFavoriteFolderRequest.getFolderName());
        userFavoriteFolder.setUserId(userAddFavoriteFolderRequest.getUserId());
        this.save(userFavoriteFolder);
    }

    @Override
    public void deleteUserFavoriteFolder(UserDelFavoriteFolderRequest userDelFavoriteFolderRequest) {
        ThrowUtils.throwIf(userDelFavoriteFolderRequest == null, new BusinessException(ErrorCode.PARAMS_ERROR));
        ThrowUtils.throwIf(userDelFavoriteFolderRequest.getFolderId() == null, new BusinessException(ErrorCode.PARAMS_ERROR));
        this.removeById(userDelFavoriteFolderRequest.getFolderId());
    }

    @Override
    public Page<UserFavoriteFolderVO> getUserFavoriteFolderVOList(FavoriteFolderQueryRequest queryRequest) {
        Long userId = queryRequest.getUserId();
        int size = queryRequest.getPageSize();
        int current = queryRequest.getCurrent();

        String cacheKey = UserCacheConstant.getUserFdCache(CacheUtils.getHexLockString(queryRequest));
        ValueQueryBean valueQueryBean = new ValueQueryBean();
        valueQueryBean.setCacheKey(cacheKey);
        valueQueryBean.setVOClass(UserFavoriteFolderVO.class);

        return valuePageCacheTemplate.valueQuery(queryRequest, valueQueryBean,
                () -> this.page(new Page<>(current, size), this.getQueryWrapper(queryRequest)),
                UserFavoriteFolderVO::listObjToVo);
    }

    private QueryWrapper<UserFavoriteFolder> getQueryWrapper(FavoriteFolderQueryRequest queryRequest){
        QueryWrapper<UserFavoriteFolder> queryWrapper = new QueryWrapper<>();
        if(queryRequest == null){
            return queryWrapper;
        }
        Long userId = queryRequest.getUserId();
        ThrowUtils.throwIf(userId == null, new BusinessException(ErrorCode.PARAMS_ERROR));
        queryWrapper.eq("userId", userId);
        String sortField = queryRequest.getSortField();
        String sortOrder = queryRequest.getSortOrder();
        queryWrapper.orderBy(StrUtil.isNotEmpty(sortField), sortOrder.equals("ascend"), sortField);
        return queryWrapper;
    }

}
