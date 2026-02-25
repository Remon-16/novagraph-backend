package com.tech.novagraphbackendgraphservice.application.screenplay.Impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tech.novagraphbackendcommon.common.CanalHandleVO;
import com.tech.novagraphbackendcommon.exception.ErrorCode;
import com.tech.novagraphbackendcommon.exception.ThrowUtils;
import com.tech.novagraphbackendgraphservice.application.screenplay.ScreenplayApplicationService;
import com.tech.novagraphbackendgraphservice.domain.screenplay.service.ScreenplayDomainService;
import com.tech.novagraphbackendmodel.dto.graph.ScreenplayAddRequest;
import com.tech.novagraphbackendmodel.dto.graph.ScreenplayQueryRequest;
import com.tech.novagraphbackendmodel.dto.graph.ScreenplayReviewRequest;
import com.tech.novagraphbackendmodel.dto.graph.ScreenplayUpdateRequest;
import com.tech.novagraphbackendmodel.graph.entity.Screenplay;
import com.tech.novagraphbackendmodel.user.entity.User;
import com.tech.novagraphbackendmodel.vo.graph.ScreenplayVO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ScreenplayApplicationServiceImpl implements ScreenplayApplicationService {

    @Resource
    private ScreenplayDomainService screenplayDomainService;

    @Override
    public Screenplay addScreenplay(ScreenplayAddRequest screenplayAddRequest) {
        return screenplayDomainService.addScreenplay(screenplayAddRequest);
    }

    @Override
    public Boolean updateScreenplay(ScreenplayUpdateRequest screenplayUpdateRequest) {
        return screenplayDomainService.updateScreenplay(screenplayUpdateRequest);
    }

    @Override
    public ScreenplayVO queryScreenplayById(Long id, Long userId) {
        return screenplayDomainService.queryScreenplayById(id, userId);
    }

    @Override
    public ScreenplayVO queryScreenplayById(Long id) {
        return screenplayDomainService.queryScreenplayById(id, null);
    }

    @Override
    public Page<ScreenplayVO> queryScreenplayPage(ScreenplayQueryRequest screenplayQueryRequest) {
        ThrowUtils.throwIf(screenplayQueryRequest == null, ErrorCode.PARAMS_ERROR);
        screenplayQueryRequest.setVisibility(1);
        screenplayQueryRequest.setReviewStatus(1);
        return screenplayDomainService.queryScreenplayPage(screenplayQueryRequest);
    }

    @Override
    public Page<ScreenplayVO> queryScreenplayPageForAdmin(ScreenplayQueryRequest screenplayQueryRequest) {
        ThrowUtils.throwIf(screenplayQueryRequest == null, ErrorCode.PARAMS_ERROR);
        screenplayQueryRequest.setVisibility(1);
        return screenplayDomainService.queryScreenplayPage(screenplayQueryRequest);
    }

    @Override
    public void doScreenplayReview(ScreenplayReviewRequest screenplayReviewRequest, User loginUser) {
        screenplayDomainService.doScreenplayReview(screenplayReviewRequest, loginUser);
    }

    @Override
    public void canalHandleScreenplay(List<CanalHandleVO> canalHandleVoList) {
        screenplayDomainService.canalHandleScreenplay(canalHandleVoList);
    }
}
