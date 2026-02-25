package com.tech.novagraphbackenduserservice.application.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tech.novagraphbackendmodel.dto.user.DoFollowRequest;
import com.tech.novagraphbackendmodel.dto.user.FollowQueryRequest;
import com.tech.novagraphbackendmodel.user.entity.User;
import com.tech.novagraphbackendmodel.vo.user.UserVO;
import com.tech.novagraphbackenduserservice.application.service.UserFollowApplicationService;
import com.tech.novagraphbackenduserservice.domain.user.service.UserFollowDomainService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class UserFollowApplicationServiceImpl implements UserFollowApplicationService {

    @Resource
    private UserFollowDomainService userFollowDomainService;

    @Override
    public Boolean doFollow(DoFollowRequest doFollowRequest, User loginUser) {
        return userFollowDomainService.doFollow(doFollowRequest, loginUser);
    }

    @Override
    public Boolean undoFollow(DoFollowRequest doFollowRequest, User loginUser) {
        return userFollowDomainService.undoFollow(doFollowRequest, loginUser);
    }

    @Override
    public Boolean hasFollow(Long targetUserId, Long userId) {
        return userFollowDomainService.hasFollow(targetUserId, userId);
    }

    @Override
    public Page<UserVO> getFollowingPage(FollowQueryRequest followQueryRequest) {
        return userFollowDomainService.getFollowingPage(followQueryRequest);
    }

    @Override
    public Page<UserVO> getFollowerPage(FollowQueryRequest followQueryRequest) {
        return userFollowDomainService.getFollowerPage(followQueryRequest);
    }

    @Override
    public void putUserFollowToCache(Long userId) {
        userFollowDomainService.putUserFollowToCache(userId);
    }

    @Override
    public Long getUserFollowingCount(Long userId) {
        return userFollowDomainService.getUserFollowingCount(userId);
    }

    @Override
    public Long getUserFollowerCount(Long userId) {
        return userFollowDomainService.getUserFollowerCount(userId);
    }
}
