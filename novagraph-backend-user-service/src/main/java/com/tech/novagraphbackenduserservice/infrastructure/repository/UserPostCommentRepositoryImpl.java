package com.tech.novagraphbackenduserservice.infrastructure.repository;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tech.novagraphbackendmodel.user.entity.UserPostComment;
import com.tech.novagraphbackenduserservice.domain.user.repository.UserPostCommentRepository;
import com.tech.novagraphbackenduserservice.infrastructure.mapper.UserPostCommentMapper;
import org.springframework.stereotype.Service;

/**
* @author Remon
* @description 针对表【user_post_comment(动态评论表)】的数据库操作Service实现
* @createDate 2026-01-26 17:26:13
*/
@Service
public class UserPostCommentRepositoryImpl extends ServiceImpl<UserPostCommentMapper, UserPostComment>
    implements UserPostCommentRepository {

}




