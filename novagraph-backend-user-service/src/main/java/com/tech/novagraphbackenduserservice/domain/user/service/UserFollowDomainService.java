package com.tech.novagraphbackenduserservice.domain.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tech.novagraphbackendmodel.dto.user.DoFollowRequest;
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
    List<UserVO> getFollowingList(User loginUser);

    /**
     * 粉丝列表
     */
    List<UserVO> getFollowerList(User loginUser);
}
