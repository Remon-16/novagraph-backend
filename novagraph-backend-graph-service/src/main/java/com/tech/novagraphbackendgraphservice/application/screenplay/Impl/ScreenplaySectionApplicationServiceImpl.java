package com.tech.novagraphbackendgraphservice.application.screenplay.Impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tech.novagraphbackendgraphservice.application.screenplay.ScreenplaySectionApplicationService;
import com.tech.novagraphbackendgraphservice.domain.screenplay.service.ScreenplaySectionDomainService;
import com.tech.novagraphbackendmodel.dto.graph.ScreenplaySectionAddRequest;
import com.tech.novagraphbackendmodel.dto.graph.ScreenplaySectionQueryRequest;
import com.tech.novagraphbackendmodel.dto.graph.ScreenplaySectionUpdateRequest;
import com.tech.novagraphbackendmodel.graph.entity.ScreenplaySection;
import com.tech.novagraphbackendmodel.vo.graph.ScreenplayContentVO;
import com.tech.novagraphbackendmodel.vo.graph.ScreenplaySectionVO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ScreenplaySectionApplicationServiceImpl implements ScreenplaySectionApplicationService {

    @Resource
    private ScreenplaySectionDomainService screenplaySectionDomainService;

    @Override
    public ScreenplaySection addScreenplaySection(ScreenplaySectionAddRequest screenplaySectionAddRequest) {
        return screenplaySectionDomainService.addScreenplaySection(screenplaySectionAddRequest);
    }

    @Override
    public ScreenplaySection updateScreenplaySection(ScreenplaySectionUpdateRequest screenplaySectionUpdateRequest) {
        return screenplaySectionDomainService.updateScreenplaySection(screenplaySectionUpdateRequest);
    }

    @Override
    public ScreenplaySectionVO queryScreenplaySectionById(Long id) {
        return screenplaySectionDomainService.queryScreenplaySectionById(id);
    }

    @Override
    public ScreenplayContentVO queryScreenplayContent(Long id) {
        return screenplaySectionDomainService.queryScreenplayContent(id);
    }

    @Override
    public Page<ScreenplaySectionVO> queryScreenplaySectionByPage(ScreenplaySectionQueryRequest screenplaySectionQueryRequest) {
        return screenplaySectionDomainService.queryScreenplaySectionByPage(screenplaySectionQueryRequest);
    }

    @Override
    public List<ScreenplaySectionVO> queryScreenplaySectionByList(ScreenplaySectionQueryRequest screenplaySectionQueryRequest) {
        return screenplaySectionDomainService.queryScreenplaySectionByList(screenplaySectionQueryRequest);
    }
}
