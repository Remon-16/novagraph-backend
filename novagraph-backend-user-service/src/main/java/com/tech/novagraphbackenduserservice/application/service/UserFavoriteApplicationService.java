package com.tech.novagraphbackenduserservice.application.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tech.novagraphbackendmodel.dto.user.UserFavoriteAddRequest;
import com.tech.novagraphbackendmodel.dto.user.UserFavoriteDelRequest;
import com.tech.novagraphbackendmodel.dto.user.UserFavoriteQueryRequest;
import com.tech.novagraphbackendmodel.user.entity.UserFavorite;
import com.tech.novagraphbackendmodel.vo.user.UserFavoriteVO;

import java.util.List;

public interface UserFavoriteApplicationService {

    void addUserFavorite(UserFavoriteAddRequest userFavoriteAddRequest);

    void deleteUserFavorite(UserFavoriteDelRequest userFavoriteDelRequest);

    Page<UserFavoriteVO> getUserFavoriteVOPage(UserFavoriteQueryRequest userFavoriteQueryRequest);

    List<UserFavorite> getUserFavoriteList(Long userId);

    UserFavoriteVO userHasFavorite(Long screenplayId, Long userId);

    Long getUserFavoriteCount(Long screenplayId);

    void putFavoriteListToCache(Long userId);

    void putFavoriteListToCache(List<UserFavorite> userFavoriteList, Long userId);
}
