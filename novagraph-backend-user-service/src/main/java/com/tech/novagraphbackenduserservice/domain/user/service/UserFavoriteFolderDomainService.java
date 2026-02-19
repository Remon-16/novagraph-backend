package com.tech.novagraphbackenduserservice.domain.user.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tech.novagraphbackendmodel.dto.user.FavoriteFolderQueryRequest;
import com.tech.novagraphbackendmodel.dto.user.UserAddFavoriteFolderRequest;
import com.tech.novagraphbackendmodel.dto.user.UserDelFavoriteFolderRequest;
import com.tech.novagraphbackendmodel.user.entity.UserFavoriteFolder;
import com.tech.novagraphbackendmodel.vo.user.UserFavoriteFolderVO;

import java.util.List;


public interface UserFavoriteFolderDomainService extends IService<UserFavoriteFolder> {

    void addUserFavoriteFolder(UserAddFavoriteFolderRequest userAddFavoriteFolderRequest);

    void deleteUserFavoriteFolder(UserDelFavoriteFolderRequest userDelFavoriteFolderRequest);

    Page<UserFavoriteFolderVO> getUserFavoriteFolderVOList(FavoriteFolderQueryRequest queryRequest);

    /**
     * 内部查询使用
     */
    String getFolderNameById(Long id, Long userId);
    /**
     * 内部查询使用
     */
    UserFavoriteFolder getUserFavoriteFolderById(Long id, Long userId);
    /**
     * 内部查询使用
     */
    List<UserFavoriteFolder> getUserFavoriteFolderList(Long userId);
    /**
     * 内部查询使用
     */
    void putUserFavoriteFolderToCache(List<UserFavoriteFolder> userFavoriteFolderList, Long userId);
    /**
     * 内部查询使用
     */
    void putUserFavoriteFolderToCache(Long userId);

}
