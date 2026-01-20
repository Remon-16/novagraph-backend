package com.tech.novagraphbackendgraphservice.application.screenplay;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tech.novagraphbackendmodel.dto.graph.ScreenplaySectionAddRequest;
import com.tech.novagraphbackendmodel.dto.graph.ScreenplaySectionQueryRequest;
import com.tech.novagraphbackendmodel.dto.graph.ScreenplaySectionUpdateRequest;
import com.tech.novagraphbackendmodel.graph.entity.ScreenplaySection;
import com.tech.novagraphbackendmodel.vo.graph.ScreenplayContentVO;
import com.tech.novagraphbackendmodel.vo.graph.ScreenplaySectionVO;

import java.util.List;

public interface ScreenplaySectionApplicationService {
    ScreenplaySection addScreenplaySection(ScreenplaySectionAddRequest screenplaySectionAddRequest);

    ScreenplaySection updateScreenplaySection(ScreenplaySectionUpdateRequest screenplaySectionUpdateRequest);

    ScreenplaySectionVO queryScreenplaySectionById(Long id);

    ScreenplayContentVO queryScreenplayContent(Long id);

    Page<ScreenplaySectionVO> queryScreenplaySectionByPage(ScreenplaySectionQueryRequest screenplaySectionQueryRequest);

    List<ScreenplaySectionVO> queryScreenplaySectionByList(ScreenplaySectionQueryRequest screenplaySectionQueryRequest);
}
