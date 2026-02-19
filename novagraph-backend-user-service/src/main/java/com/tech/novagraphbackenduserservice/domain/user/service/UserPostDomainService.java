package com.tech.novagraphbackenduserservice.domain.user.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tech.novagraphbackendmodel.dto.user.UserPostAddRequest;
import com.tech.novagraphbackendmodel.dto.user.UserPostQueryRequest;
import com.tech.novagraphbackendmodel.user.entity.UserPost;

public interface UserPostDomainService {
    UserPost saveOrUpdate(UserPostAddRequest userPostAddRequest);

    void delete(Long userPostId);
    Page<UserPost> getUserPostPage(UserPostQueryRequest userPostQueryRequest);
}
