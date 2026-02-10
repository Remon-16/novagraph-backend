package com.tech.novagraphbackenduserservice.domain.user.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tech.novagraphbackendmodel.dto.user.FavoriteFolderQueryRequest;
import com.tech.novagraphbackendmodel.dto.user.UserAddFavoriteFolderRequest;
import com.tech.novagraphbackendmodel.dto.user.UserDelFavoriteFolderRequest;
import com.tech.novagraphbackendmodel.user.entity.UserFavoriteFolder;
import com.tech.novagraphbackendmodel.vo.user.UserFavoriteFolderVO;


public interface UserFavoriteFolderDomainService extends IService<UserFavoriteFolder> {

    void addUserFavoriteFolder(UserAddFavoriteFolderRequest userAddFavoriteFolderRequest);

    void deleteUserFavoriteFolder(UserDelFavoriteFolderRequest userDelFavoriteFolderRequest);

    Page<UserFavoriteFolderVO> getUserFavoriteFolderVOList(FavoriteFolderQueryRequest queryRequest);

}
