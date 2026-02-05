package com.tech.novagraphbackenduserservice.application.service;

import com.tech.novagraphbackendmodel.dto.user.PostThumbRequest;
import com.tech.novagraphbackendmodel.user.entity.User;
import com.tech.novagraphbackendmodel.vo.user.UserPostVO;

import java.util.List;

public interface UserPostThumbApplicationService {
    Boolean doThumb(PostThumbRequest postThumbRequest, User loginUser);

    Boolean undoThumb(PostThumbRequest postThumbRequest, User loginUser);

    Boolean hasThumb(Long postId, Long userId);

    List<UserPostVO> getPostThumbStatus(List<UserPostVO> userPostVOList, User loginUser);
}
