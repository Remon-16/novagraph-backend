package com.tech.novagraphbackendgraphservice.domain.screenplay.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tech.novagraphbackendcommon.common.CanalHandleVO;
import com.tech.novagraphbackendmodel.dto.graph.DoThumbRequest;
import com.tech.novagraphbackendmodel.dto.graph.ScreenplayCommentQueryRequest;
import com.tech.novagraphbackendmodel.dto.graph.ScreenplayCommentRequest;
import com.tech.novagraphbackendmodel.graph.entity.ScreenplayComment;
import com.tech.novagraphbackendmodel.user.entity.User;
import com.tech.novagraphbackendmodel.vo.graph.ScreenplayCommentRootVo;
import com.tech.novagraphbackendmodel.vo.graph.ScreenplayCommentVo;
import com.tech.novagraphbackendmodel.vo.graph.ScreenplayVO;

import java.util.List;

public interface ScreenplayCommentDomainService extends IService<ScreenplayComment> {
    /**
     * 保存或更新评论
     * @param screenplayCommentRequest
     * @return
     */
    ScreenplayComment saveOrUpdateScreenplayComment(ScreenplayCommentRequest screenplayCommentRequest);

    /**
     * 删除评论
     * @param screenplayCommentRequest
     * @return
     */
    Boolean deleteScreenplayComment(ScreenplayCommentRequest screenplayCommentRequest);

    /**
     * QueryWrapper
     * @param screenplayCommentQueryRequest
     * @return
     */
    QueryWrapper<ScreenplayComment> getQueryWrapper(ScreenplayCommentQueryRequest screenplayCommentQueryRequest);

    /**
     * 查询根评论
     * @param screenplayCommentQueryRequest
     * @return
     */
    Page<ScreenplayCommentRootVo> getScreenplayCommentRootVo(ScreenplayCommentQueryRequest screenplayCommentQueryRequest);

    /**
     * 查询次级评论
     * @param screenplayCommentQueryRequest
     * @return
     */
    Page<ScreenplayCommentVo> getScreenplayCommentVo(ScreenplayCommentQueryRequest screenplayCommentQueryRequest);

    /**
     * 评论缓存同步
     * @param canalHandleVOList
     */
    void canalHandleScreenplayComment(List<CanalHandleVO> canalHandleVOList);

    /**
     * 根据 ID 获取评论
     * @param ScreenplayCommentId
     * @return
     */
    ScreenplayComment getById(Long ScreenplayCommentId);
}
