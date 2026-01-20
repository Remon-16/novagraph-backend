package com.tech.novagraphbackendgraphservice.application.screenplay;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tech.novagraphbackendmodel.dto.graph.ScreenplayAddRequest;
import com.tech.novagraphbackendmodel.dto.graph.ScreenplayQueryRequest;
import com.tech.novagraphbackendmodel.dto.graph.ScreenplayUpdateRequest;
import com.tech.novagraphbackendmodel.graph.entity.Screenplay;
import com.tech.novagraphbackendmodel.graph.entity.ScreenplayThumb;
import com.tech.novagraphbackendmodel.vo.graph.ScreenplayVO;

public interface ScreenplayApplicationService {
    Screenplay addScreenplay(ScreenplayAddRequest screenplayAddRequest);

    Boolean updateScreenplay(ScreenplayUpdateRequest screenplayUpdateRequest);

    ScreenplayVO queryScreenplayById(Long id);

    Page<ScreenplayVO> queryScreenplayPage(ScreenplayQueryRequest screenplayQueryRequest);
}
