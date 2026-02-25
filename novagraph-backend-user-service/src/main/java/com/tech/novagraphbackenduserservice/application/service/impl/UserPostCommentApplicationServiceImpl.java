package com.tech.novagraphbackenduserservice.application.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tech.novagraphbackendcommon.common.CanalHandleVO;
import com.tech.novagraphbackendmodel.dto.user.UserPostCommentQueryRequest;
import com.tech.novagraphbackendmodel.dto.user.UserPostRequest;
import com.tech.novagraphbackendmodel.user.entity.UserPostComment;
import com.tech.novagraphbackendmodel.vo.user.UserPostZSetRootVO;
import com.tech.novagraphbackendmodel.vo.user.UserPostZSetVO;
import com.tech.novagraphbackenduserservice.application.service.UserPostCommentApplicationService;
import com.tech.novagraphbackenduserservice.domain.user.service.UserPostCommentDomainService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserPostCommentApplicationServiceImpl implements UserPostCommentApplicationService {

    @Resource
    private UserPostCommentDomainService userPostCommentDomainService;

    @Override
    public UserPostComment saveOrUpdateUserPostComment(UserPostRequest userPostRequest) {
        return userPostCommentDomainService.saveOrUpdateUserPostComment(userPostRequest);
    }

    @Override
    public Boolean deleteUserPostComment(UserPostRequest userPostRequest) {
        return userPostCommentDomainService.deleteUserPostComment(userPostRequest);
    }

    @Override
    public Page<UserPostZSetRootVO> getUserPostCommentRootVo(UserPostCommentQueryRequest userPostCommentQueryRequest) {
        return userPostCommentDomainService.getUserPostCommentRootVo(userPostCommentQueryRequest);
    }

    @Override
    public Page<UserPostZSetVO> getUserPostCommentVo(UserPostCommentQueryRequest userPostCommentQueryRequest) {
        return userPostCommentDomainService.getUserPostCommentVo(userPostCommentQueryRequest);
    }

    @Override
    public void canalHandleUserPostComment(List<CanalHandleVO> canalHandleVOList) {
        userPostCommentDomainService.canalHandleUserPostComment(canalHandleVOList);
    }

    @Override
    public UserPostComment getById(Long postCommentId) {
        return userPostCommentDomainService.getById(postCommentId);
    }
}
