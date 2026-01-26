package com.tech.novagraphbackenduserservice.infrastructure.repository;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tech.novagraphbackendmodel.user.entity.UserFavorite;
import com.tech.novagraphbackenduserservice.domain.user.repository.UserFavoriteRepository;
import com.tech.novagraphbackenduserservice.infrastructure.mapper.UserFavoriteMapper;
import org.springframework.stereotype.Service;

/**
* @author Remon
* @description 针对表【user_favorite(用户收藏)】的数据库操作Service实现
* @createDate 2026-01-26 17:24:43
*/
@Service
public class UserFavoriteRepositoryImpl extends ServiceImpl<UserFavoriteMapper, UserFavorite>
    implements UserFavoriteRepository {

}




