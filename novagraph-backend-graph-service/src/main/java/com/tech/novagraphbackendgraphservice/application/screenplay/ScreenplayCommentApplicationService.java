package com.tech.novagraphbackendgraphservice.application.screenplay;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tech.novagraphbackendcommon.common.CanalHandleVO;
import com.tech.novagraphbackendmodel.dto.graph.ScreenplayCommentQueryRequest;
import com.tech.novagraphbackendmodel.dto.graph.ScreenplayCommentRequest;
import com.tech.novagraphbackendmodel.graph.entity.ScreenplayComment;
import com.tech.novagraphbackendmodel.vo.graph.ScreenplayCommentRootVO;
import com.tech.novagraphbackendmodel.vo.graph.ScreenplayCommentVO;

import java.util.List;

public interface ScreenplayCommentApplicationService {
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
     * 查询根评论
     * @param screenplayCommentQueryRequest
     * @return
     */
    Page<ScreenplayCommentRootVO> getScreenplayCommentRootVo(ScreenplayCommentQueryRequest screenplayCommentQueryRequest);

    /**
     * 查询次级评论
     * @param screenplayCommentQueryRequest
     * @return
     */
    Page<ScreenplayCommentVO> getScreenplayCommentVo(ScreenplayCommentQueryRequest screenplayCommentQueryRequest);

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
