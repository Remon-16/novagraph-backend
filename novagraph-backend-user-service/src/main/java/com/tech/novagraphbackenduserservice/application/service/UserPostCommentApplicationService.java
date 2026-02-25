package com.tech.novagraphbackenduserservice.application.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tech.novagraphbackendcommon.common.CanalHandleVO;
import com.tech.novagraphbackendmodel.dto.user.UserPostCommentQueryRequest;
import com.tech.novagraphbackendmodel.dto.user.UserPostRequest;
import com.tech.novagraphbackendmodel.user.entity.UserPostComment;
import com.tech.novagraphbackendmodel.vo.user.UserPostZSetRootVO;
import com.tech.novagraphbackendmodel.vo.user.UserPostZSetVO;

import java.util.List;

public interface UserPostCommentApplicationService {

    /**
     * 保存或更新评论
     */
    UserPostComment saveOrUpdateUserPostComment(UserPostRequest userPostRequest);

    /**
     * 删除评论
     */
    Boolean deleteUserPostComment(UserPostRequest userPostRequest);

    /**
     * 查询根评论
     */
    Page<UserPostZSetRootVO> getUserPostCommentRootVo(UserPostCommentQueryRequest userPostCommentQueryRequest);

    /**
     * 查询次级评论
     */
    Page<UserPostZSetVO> getUserPostCommentVo(UserPostCommentQueryRequest userPostCommentQueryRequest);

    /**
     * 评论缓存同步
     */
    void canalHandleUserPostComment(List<CanalHandleVO> canalHandleVOList);

    /**
     * 根据 ID 获取评论
     */
    UserPostComment getById(Long postCommentId);
}
