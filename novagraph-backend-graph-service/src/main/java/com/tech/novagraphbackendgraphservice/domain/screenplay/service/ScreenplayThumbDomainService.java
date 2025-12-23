package com.tech.novagraphbackendgraphservice.domain.screenplay.service;

import com.tech.novagraphbackendmodel.dto.graph.DoThumbRequest;
import com.tech.novagraphbackendmodel.user.entity.User;
import com.tech.novagraphbackendmodel.vo.graph.ScreenplayVO;

import java.util.List;

public interface ScreenplayThumbDomainService {
    Boolean doThumb(DoThumbRequest doThumbRequest, User loginUser);

    Boolean undoThumb(DoThumbRequest doThumbRequest, User loginUser);

    Boolean hasThumb(Long screenplayId, Long userId);

    List<ScreenplayVO> getScreenplayThumbState(List<ScreenplayVO> screenplayVOList, User loginUser);
}
