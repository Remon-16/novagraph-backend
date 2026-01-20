package com.tech.novagraphbackendgraphservice.application.screenplay.Impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tech.novagraphbackendcommon.common.CanalHandleVO;
import com.tech.novagraphbackendgraphservice.application.screenplay.ScreenplayCommentApplicationService;
import com.tech.novagraphbackendgraphservice.domain.screenplay.service.ScreenplayCommentDomainService;
import com.tech.novagraphbackendmodel.dto.graph.ScreenplayCommentQueryRequest;
import com.tech.novagraphbackendmodel.dto.graph.ScreenplayCommentRequest;
import com.tech.novagraphbackendmodel.graph.entity.ScreenplayComment;
import com.tech.novagraphbackendmodel.vo.graph.ScreenplayCommentRootVO;
import com.tech.novagraphbackendmodel.vo.graph.ScreenplayCommentVO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ScreenplayCommentApplicationServiceImpl implements ScreenplayCommentApplicationService {

    @Resource
    private ScreenplayCommentDomainService screenplayCommentDomainService;

    @Override
    public ScreenplayComment saveOrUpdateScreenplayComment(ScreenplayCommentRequest screenplayCommentRequest) {
        return screenplayCommentDomainService.saveOrUpdateScreenplayComment(screenplayCommentRequest);
    }

    @Override
    public Boolean deleteScreenplayComment(ScreenplayCommentRequest screenplayCommentRequest) {
        return screenplayCommentDomainService.deleteScreenplayComment(screenplayCommentRequest);
    }

    @Override
    public Page<ScreenplayCommentRootVO> getScreenplayCommentRootVo(ScreenplayCommentQueryRequest screenplayCommentQueryRequest) {
        return screenplayCommentDomainService.getScreenplayCommentRootVo(screenplayCommentQueryRequest);
    }

    @Override
    public Page<ScreenplayCommentVO> getScreenplayCommentVo(ScreenplayCommentQueryRequest screenplayCommentQueryRequest) {
        return screenplayCommentDomainService.getScreenplayCommentVo(screenplayCommentQueryRequest);
    }

    @Override
    public void canalHandleScreenplayComment(List<CanalHandleVO> canalHandleVOList) {
        screenplayCommentDomainService.canalHandleScreenplayComment(canalHandleVOList);
    }

    @Override
    public ScreenplayComment getById(Long ScreenplayCommentId) {
        return screenplayCommentDomainService.getById(ScreenplayCommentId);
    }
}
