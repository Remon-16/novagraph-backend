package com.tech.novagraphbackenduserservice.domain.user.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tech.novagraphbackendmodel.dto.user.DoFollowRequest;
import com.tech.novagraphbackendmodel.dto.user.FollowQueryRequest;
import com.tech.novagraphbackendmodel.user.entity.User;
import com.tech.novagraphbackendmodel.user.entity.UserFollow;
import com.tech.novagraphbackendmodel.vo.user.UserVO;

import java.util.List;

public interface UserFollowDomainService extends IService<UserFollow> {
    Boolean doFollow(DoFollowRequest doFollowRequest, User loginUser);

    Boolean undoFollow(DoFollowRequest doFollowRequest, User loginUser);

    Boolean hasFollow(Long targetUserId, Long userId);

    /**
     * 关注列表
     */
    Page<UserVO> getFollowingPage(FollowQueryRequest followQueryRequest);

    /**
     * 粉丝列表
     */
    Page<UserVO> getFollowerPage(FollowQueryRequest followQueryRequest);

    void putUserFollowToCache(Long userId);

    Long getUserFollowingCount(Long userId);

    Long getUserFollowerCount(Long userId);
}
