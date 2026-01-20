package com.tech.novagraphbackendgraphservice.application.screenplay.Impl;

import com.tech.novagraphbackendgraphservice.application.screenplay.ScreenplayThumbApplicationService;
import com.tech.novagraphbackendgraphservice.domain.screenplay.service.ScreenplayThumbDomainService;
import com.tech.novagraphbackendmodel.dto.graph.DoThumbRequest;
import com.tech.novagraphbackendmodel.graph.entity.ScreenplayThumb;
import com.tech.novagraphbackendmodel.user.entity.User;
import com.tech.novagraphbackendmodel.vo.graph.ScreenplayVO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ScreenplayThumbApplicationServiceImpl implements ScreenplayThumbApplicationService {

    @Resource
    private ScreenplayThumbDomainService screenplayThumbDomainService;

    @Override
    public Boolean doThumb(DoThumbRequest doThumbRequest, User loginUser) {
        return screenplayThumbDomainService.doThumb(doThumbRequest, loginUser);
    }

    @Override
    public Boolean undoThumb(DoThumbRequest doThumbRequest, User loginUser) {
        return screenplayThumbDomainService.undoThumb(doThumbRequest, loginUser);
    }

    @Override
    public Boolean hasThumb(Long screenplayId, Long userId) {
        return screenplayThumbDomainService.hasThumb(screenplayId, userId);
    }

    @Override
    public List<ScreenplayVO> getScreenplayThumbState(List<ScreenplayVO> screenplayVOList, User loginUser) {
        return screenplayThumbDomainService.getScreenplayThumbState(screenplayVOList, loginUser);
    }

    @Override
    public ScreenplayThumb getThumbById(Long commentId) {
        return screenplayThumbDomainService.getById(commentId);
    }
}
