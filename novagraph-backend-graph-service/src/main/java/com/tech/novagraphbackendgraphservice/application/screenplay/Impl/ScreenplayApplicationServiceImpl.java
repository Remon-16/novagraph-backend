package com.tech.novagraphbackendgraphservice.application.screenplay.Impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tech.novagraphbackendgraphservice.application.screenplay.ScreenplayApplicationService;
import com.tech.novagraphbackendgraphservice.domain.screenplay.service.ScreenplayDomainService;
import com.tech.novagraphbackendmodel.dto.graph.ScreenplayAddRequest;
import com.tech.novagraphbackendmodel.dto.graph.ScreenplayQueryRequest;
import com.tech.novagraphbackendmodel.dto.graph.ScreenplayUpdateRequest;
import com.tech.novagraphbackendmodel.graph.entity.Screenplay;
import com.tech.novagraphbackendmodel.graph.entity.ScreenplayThumb;
import com.tech.novagraphbackendmodel.vo.graph.ScreenplayVO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

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
    public ScreenplayVO queryScreenplayById(Long id) {
        return screenplayDomainService.queryScreenplayById(id);
    }

    @Override
    public Page<ScreenplayVO> queryScreenplayPage(ScreenplayQueryRequest screenplayQueryRequest) {
        return screenplayDomainService.queryScreenplayPage(screenplayQueryRequest);
    }
}
