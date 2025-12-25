package com.tech.novagraphbackendgraphservice.domain.screenplay.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tech.novagraphbackendmodel.dto.graph.DoThumbRequest;
import com.tech.novagraphbackendmodel.graph.entity.ScreenplayThumb;
import com.tech.novagraphbackendmodel.user.entity.User;
import com.tech.novagraphbackendmodel.vo.graph.ScreenplayVO;

import java.util.List;

public interface ScreenplayThumbDomainService extends IService<ScreenplayThumb> {
    Boolean doThumb(DoThumbRequest doThumbRequest, User loginUser);

    Boolean undoThumb(DoThumbRequest doThumbRequest, User loginUser);

    Boolean hasThumb(Long screenplayId, Long userId);

    List<ScreenplayVO> getScreenplayThumbState(List<ScreenplayVO> screenplayVOList, User loginUser);
}
