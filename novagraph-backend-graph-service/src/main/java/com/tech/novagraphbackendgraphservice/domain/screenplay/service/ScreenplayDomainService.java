package com.tech.novagraphbackendgraphservice.domain.screenplay.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tech.novagraphbackendcommon.common.CanalHandleVO;
import com.tech.novagraphbackendmodel.dto.graph.ScreenplayAddRequest;
import com.tech.novagraphbackendmodel.dto.graph.ScreenplayQueryRequest;
import com.tech.novagraphbackendmodel.dto.graph.ScreenplayReviewRequest;
import com.tech.novagraphbackendmodel.dto.graph.ScreenplayUpdateRequest;
import com.tech.novagraphbackendmodel.graph.entity.Screenplay;
import com.tech.novagraphbackendmodel.graph.entity.ScreenplayThumb;
import com.tech.novagraphbackendmodel.user.entity.User;
import com.tech.novagraphbackendmodel.vo.graph.ScreenplayVO;

import java.util.List;

public interface ScreenplayDomainService extends IService<Screenplay> {
    Screenplay addScreenplay(ScreenplayAddRequest screenplayAddRequest);

    Boolean updateScreenplay(ScreenplayUpdateRequest screenplayUpdateRequest);

    ScreenplayVO queryScreenplayById(Long id, Long userId);

    Page<ScreenplayVO> queryScreenplayPage(ScreenplayQueryRequest screenplayQueryRequest);

    void doScreenplayReview(ScreenplayReviewRequest screenplayReviewRequest, User loginUser);

    void canalHandleScreenplay(List<CanalHandleVO> canalHandleVoList);
}
