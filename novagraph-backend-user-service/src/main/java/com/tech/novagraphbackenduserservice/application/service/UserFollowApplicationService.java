package com.tech.novagraphbackenduserservice.application.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tech.novagraphbackendmodel.dto.user.DoFollowRequest;
import com.tech.novagraphbackendmodel.dto.user.FollowQueryRequest;
import com.tech.novagraphbackendmodel.user.entity.User;
import com.tech.novagraphbackendmodel.vo.user.UserVO;

public interface UserFollowApplicationService {

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
