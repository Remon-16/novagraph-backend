package com.tech.novagraphbackendgraphservice.application.screenplay;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tech.novagraphbackendmodel.dto.graph.ScreenplayAddRequest;
import com.tech.novagraphbackendmodel.dto.graph.ScreenplayQueryRequest;
import com.tech.novagraphbackendmodel.dto.graph.ScreenplayReviewRequest;
import com.tech.novagraphbackendmodel.dto.graph.ScreenplayUpdateRequest;
import com.tech.novagraphbackendmodel.graph.entity.Screenplay;
import com.tech.novagraphbackendmodel.graph.entity.ScreenplayThumb;
import com.tech.novagraphbackendmodel.user.entity.User;
import com.tech.novagraphbackendmodel.vo.graph.ScreenplayVO;

public interface ScreenplayApplicationService {
    Screenplay addScreenplay(ScreenplayAddRequest screenplayAddRequest);

    Boolean updateScreenplay(ScreenplayUpdateRequest screenplayUpdateRequest);

    ScreenplayVO queryScreenplayById(Long id);

    /**
     * 用户查询接口，必须即可见又审核通过
     */
    Page<ScreenplayVO> queryScreenplayPage(ScreenplayQueryRequest screenplayQueryRequest);

    /**
     * 管理员专用查询接口
     */
    Page<ScreenplayVO> queryScreenplayPageForAdmin(ScreenplayQueryRequest screenplayQueryRequest);

    void doScreenplayReview(ScreenplayReviewRequest screenplayReviewRequest, User loginUser);
}
