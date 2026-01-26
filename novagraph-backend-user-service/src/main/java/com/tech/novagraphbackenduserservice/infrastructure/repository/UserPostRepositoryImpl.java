package com.tech.novagraphbackenduserservice.infrastructure.repository;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tech.novagraphbackendmodel.user.entity.UserPost;
import com.tech.novagraphbackenduserservice.domain.user.repository.UserPostRepository;
import com.tech.novagraphbackenduserservice.infrastructure.mapper.UserPostMapper;
import org.springframework.stereotype.Service;

/**
* @author Remon
* @description 针对表【user_post(用户动态表)】的数据库操作Service实现
* @createDate 2026-01-26 17:25:12
*/
@Service
public class UserPostRepositoryImpl extends ServiceImpl<UserPostMapper, UserPost>
    implements UserPostRepository {

}




