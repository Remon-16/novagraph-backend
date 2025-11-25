package com.tech.novagraphbackenduserservice.infrastructure.repository;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import generator.domain.User;
import com.tech.novagraphbackenduserservice.domain.user.repository.UserRepository;
import com.tech.novagraphbackenduserservice.infrastructure.mapper.UserMapper;
import org.springframework.stereotype.Service;

/**
* @author Remon
* @description 针对表【user(用户)】的数据库操作Service实现
* @createDate 2025-11-25 15:35:28
*/
@Service
public class UserRepositoryImpl extends ServiceImpl<UserMapper, User>
    implements UserRepository {

}




