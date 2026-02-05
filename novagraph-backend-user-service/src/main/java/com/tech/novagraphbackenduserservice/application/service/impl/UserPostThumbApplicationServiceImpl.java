package com.tech.novagraphbackenduserservice.application.service.impl;

import com.tech.novagraphbackendmodel.dto.user.PostThumbRequest;
import com.tech.novagraphbackendmodel.user.entity.User;
import com.tech.novagraphbackendmodel.vo.user.UserPostVO;
import com.tech.novagraphbackenduserservice.application.service.UserPostThumbApplicationService;
import com.tech.novagraphbackenduserservice.domain.user.service.UserPostThumbDomainService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserPostThumbApplicationServiceImpl implements UserPostThumbApplicationService {

    @Resource
    private UserPostThumbDomainService userPostThumbDomainService;

    @Override
    public Boolean doThumb(PostThumbRequest postThumbRequest, User loginUser) {
        return userPostThumbDomainService.doThumb(postThumbRequest, loginUser);
    }

    @Override
    public Boolean undoThumb(PostThumbRequest postThumbRequest, User loginUser) {
        return userPostThumbDomainService.undoThumb(postThumbRequest, loginUser);
    }

    @Override
    public Boolean hasThumb(Long postId, Long userId) {
        return userPostThumbDomainService.hasThumb(postId, userId);
    }

    @Override
    public List<UserPostVO> getPostThumbStatus(List<UserPostVO> userPostVOList, User loginUser) {
        return userPostThumbDomainService.getPostThumbStatus(userPostVOList, loginUser);
    }
}
