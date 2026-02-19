package com.tech.novagraphbackenduserservice.domain.user.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tech.novagraphbackendcommon.common.CanalHandleVO;
import com.tech.novagraphbackendmodel.dto.user.UserPostCommentQueryRequest;
import com.tech.novagraphbackendmodel.dto.user.UserPostRequest;
import com.tech.novagraphbackendmodel.user.entity.UserPostComment;
import com.tech.novagraphbackendmodel.vo.user.UserPostCommentRootVO;
import com.tech.novagraphbackendmodel.vo.user.UserPostCommentVO;

import java.util.List;

public interface UserPostCommentDomainService {

    /**
     * 保存或更新评论
     */
    UserPostComment saveOrUpdateUserPostComment(UserPostRequest userPostRequest);

    /**
     * 删除评论
     */
    Boolean deleteUserPostComment(UserPostRequest userPostRequest);

    /**
     * QueryWrapper
     */
    QueryWrapper<UserPostComment> getQueryWrapper(UserPostCommentQueryRequest userPostCommentQueryRequest);

    /**
     * 查询根评论
     */
    Page<UserPostCommentRootVO> getUserPostCommentRootVo(UserPostCommentQueryRequest userPostCommentQueryRequest);

    /**
     * 查询次级评论
     */
    Page<UserPostCommentVO> getUserPostCommentVo(UserPostCommentQueryRequest userPostCommentQueryRequest);

    /**
     * 评论缓存同步
     */
    void canalHandleUserPostComment(List<CanalHandleVO> canalHandleVOList);

    /**
     * 根据 ID 获取评论
     */
    UserPostComment getById(Long postCommentId);
}
