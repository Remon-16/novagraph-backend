package com.tech.novagraphbackenduserservice.application.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tech.novagraphbackendmodel.dto.user.UserPostAddRequest;
import com.tech.novagraphbackendmodel.dto.user.UserPostQueryRequest;
import com.tech.novagraphbackendmodel.user.entity.UserPost;
import com.tech.novagraphbackendmodel.vo.user.UserPostVO;
import com.tech.novagraphbackenduserservice.application.service.UserPostApplicationService;
import com.tech.novagraphbackenduserservice.domain.user.service.UserPostDomainService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class UserPostApplicationServiceImpl implements UserPostApplicationService {

    @Resource
    private UserPostDomainService userPostDomainService;

    @Override
    public UserPost saveOrUpdate(UserPostAddRequest userPostAddRequest) {
        return userPostDomainService.saveOrUpdate(userPostAddRequest);
    }

    @Override
    public void delete(Long userPostId) {
        userPostDomainService.delete(userPostId);
    }

    @Override
    public Page<UserPostVO> getUserPostVOPage(UserPostQueryRequest userPostQueryRequest) {
        return userPostDomainService.getUserPostVOPage(userPostQueryRequest);
    }
}
