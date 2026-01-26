package com.tech.novagraphbackenduserservice.infrastructure.repository;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tech.novagraphbackendmodel.user.entity.UserFavoriteFolder;
import com.tech.novagraphbackenduserservice.domain.user.repository.UserFavoriteFolderRepository;
import com.tech.novagraphbackenduserservice.infrastructure.mapper.UserFavoriteFolderMapper;
import org.springframework.stereotype.Service;

/**
* @author Remon
* @description 针对表【user_favorite_folder(用户收藏夹)】的数据库操作Service实现
* @createDate 2026-01-26 17:24:50
*/
@Service
public class UserFavoriteFolderRepositoryImpl extends ServiceImpl<UserFavoriteFolderMapper, UserFavoriteFolder>
    implements UserFavoriteFolderRepository {

}




