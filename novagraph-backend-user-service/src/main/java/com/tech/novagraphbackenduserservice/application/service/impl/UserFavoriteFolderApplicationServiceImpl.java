package com.tech.novagraphbackenduserservice.application.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tech.novagraphbackendmodel.dto.user.FavoriteFolderQueryRequest;
import com.tech.novagraphbackendmodel.dto.user.UserAddFavoriteFolderRequest;
import com.tech.novagraphbackendmodel.dto.user.UserDelFavoriteFolderRequest;
import com.tech.novagraphbackendmodel.user.entity.UserFavoriteFolder;
import com.tech.novagraphbackendmodel.vo.user.UserFavoriteFolderVO;
import com.tech.novagraphbackenduserservice.application.service.UserFavoriteFolderApplicationService;
import com.tech.novagraphbackenduserservice.domain.user.service.UserFavoriteFolderDomainService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserFavoriteFolderApplicationServiceImpl implements UserFavoriteFolderApplicationService {

    @Resource
    private UserFavoriteFolderDomainService userFavoriteFolderDomainService;

    @Override
    public void addUserFavoriteFolder(UserAddFavoriteFolderRequest userAddFavoriteFolderRequest) {
        userFavoriteFolderDomainService.addUserFavoriteFolder(userAddFavoriteFolderRequest);
    }

    @Override
    public void deleteUserFavoriteFolder(UserDelFavoriteFolderRequest userDelFavoriteFolderRequest) {
        userFavoriteFolderDomainService.deleteUserFavoriteFolder(userDelFavoriteFolderRequest);
    }

    @Override
    public Page<UserFavoriteFolderVO> getUserFavoriteFolderVOList(FavoriteFolderQueryRequest queryRequest) {
        return userFavoriteFolderDomainService.getUserFavoriteFolderVOList(queryRequest);
    }

    @Override
    public String getFolderNameById(Long id, Long userId) {
        return userFavoriteFolderDomainService.getFolderNameById(id, userId);
    }

    @Override
    public UserFavoriteFolder getUserFavoriteFolderById(Long id, Long userId) {
        return userFavoriteFolderDomainService.getUserFavoriteFolderById(id, userId);
    }

    @Override
    public List<UserFavoriteFolder> getUserFavoriteFolderList(Long userId) {
        return userFavoriteFolderDomainService.getUserFavoriteFolderList(userId);
    }

    @Override
    public void putUserFavoriteFolderToCache(List<UserFavoriteFolder> userFavoriteFolderList, Long userId) {
        userFavoriteFolderDomainService.putUserFavoriteFolderToCache(userFavoriteFolderList, userId);
    }

    @Override
    public void putUserFavoriteFolderToCache(Long userId) {
        userFavoriteFolderDomainService.putUserFavoriteFolderToCache(userId);
    }
}
