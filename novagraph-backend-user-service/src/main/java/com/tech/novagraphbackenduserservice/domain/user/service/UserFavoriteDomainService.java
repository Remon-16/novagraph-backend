package com.tech.novagraphbackenduserservice.domain.user.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tech.novagraphbackendmodel.dto.user.UserFavoriteAddRequest;
import com.tech.novagraphbackendmodel.dto.user.UserFavoriteDelRequest;
import com.tech.novagraphbackendmodel.dto.user.UserFavoriteQueryRequest;
import com.tech.novagraphbackendmodel.user.entity.UserFavorite;
import com.tech.novagraphbackendmodel.vo.user.UserFavoriteVO;

public interface UserFavoriteDomainService extends IService<UserFavorite> {

    void addUserFavorite(UserFavoriteAddRequest userFavoriteAddRequest);

    void deleteUserFavorite(UserFavoriteDelRequest userFavoriteDelRequest);

    Page<UserFavoriteVO> getUserFavoriteVOPage(UserFavoriteQueryRequest userFavoriteQueryRequest);
}
