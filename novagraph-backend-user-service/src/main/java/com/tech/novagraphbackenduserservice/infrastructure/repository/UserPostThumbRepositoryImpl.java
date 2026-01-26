package com.tech.novagraphbackenduserservice.infrastructure.repository;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tech.novagraphbackendmodel.user.entity.UserPostThumb;
import com.tech.novagraphbackenduserservice.domain.user.repository.UserPostThumbRepository;
import com.tech.novagraphbackenduserservice.infrastructure.mapper.UserPostThumbMapper;
import org.springframework.stereotype.Service;

/**
* @author Remon
* @description 针对表【user_post_thumb(动态点赞记录表)】的数据库操作Service实现
* @createDate 2026-01-26 17:25:23
*/
@Service
public class UserPostThumbRepositoryImpl extends ServiceImpl<UserPostThumbMapper, UserPostThumb>
    implements UserPostThumbRepository {

}




