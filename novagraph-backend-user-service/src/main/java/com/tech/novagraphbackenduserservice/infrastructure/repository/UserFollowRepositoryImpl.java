package com.tech.novagraphbackenduserservice.infrastructure.repository;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tech.novagraphbackendmodel.user.entity.UserFollow;
import com.tech.novagraphbackenduserservice.domain.user.repository.UserFollowRepository;
import com.tech.novagraphbackenduserservice.infrastructure.mapper.UserFollowMapper;
import org.springframework.stereotype.Service;

/**
* @author Remon
* @description 针对表【user_follow(用户关注表)】的数据库操作Service实现
* @createDate 2026-01-26 17:24:59
*/
@Service
public class UserFollowRepositoryImpl extends ServiceImpl<UserFollowMapper, UserFollow>
    implements UserFollowRepository {

}




