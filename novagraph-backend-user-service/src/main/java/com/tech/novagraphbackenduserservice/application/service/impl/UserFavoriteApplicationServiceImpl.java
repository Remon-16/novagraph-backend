package com.tech.novagraphbackenduserservice.application.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tech.novagraphbackendmodel.dto.user.UserFavoriteAddRequest;
import com.tech.novagraphbackendmodel.dto.user.UserFavoriteDelRequest;
import com.tech.novagraphbackendmodel.dto.user.UserFavoriteQueryRequest;
import com.tech.novagraphbackendmodel.user.entity.UserFavorite;
import com.tech.novagraphbackendmodel.vo.user.UserFavoriteVO;
import com.tech.novagraphbackenduserservice.application.service.UserFavoriteApplicationService;
import com.tech.novagraphbackenduserservice.domain.user.service.UserFavoriteDomainService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserFavoriteApplicationServiceImpl implements UserFavoriteApplicationService {
    @Resource
    private UserFavoriteDomainService userFavoriteDomainService;

    @Override
    public void addUserFavorite(UserFavoriteAddRequest userFavoriteAddRequest) {
        userFavoriteDomainService.addUserFavorite(userFavoriteAddRequest);
    }

    @Override
    public void deleteUserFavorite(UserFavoriteDelRequest userFavoriteDelRequest) {
        userFavoriteDomainService.deleteUserFavorite(userFavoriteDelRequest);
    }

    @Override
    public Page<UserFavoriteVO> getUserFavoriteVOPage(UserFavoriteQueryRequest userFavoriteQueryRequest) {
        return userFavoriteDomainService.getUserFavoriteVOPage(userFavoriteQueryRequest);
    }

    @Override
    public List<UserFavorite> getUserFavoriteList(Long userId) {
        return userFavoriteDomainService.getUserFavoriteList(userId);
    }

    @Override
    public UserFavoriteVO userHasFavorite(Long screenplayId, Long userId) {
        return userFavoriteDomainService.userHasFavorite(screenplayId, userId);
    }

    @Override
    public Long getUserFavoriteCount(Long screenplayId) {
        return userFavoriteDomainService.getUserFavoriteCount(screenplayId);
    }

    @Override
    public void putFavoriteListToCache(Long userId) {
        userFavoriteDomainService.putFavoriteListToCache(userId);
    }

    @Override
    public void putFavoriteListToCache(List<UserFavorite> userFavoriteList, Long userId) {
        userFavoriteDomainService.putFavoriteListToCache(userFavoriteList, userId);
    }
}
