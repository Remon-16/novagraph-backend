package com.tech.novagraphbackendgraphservice.application.screenplay.Impl;

import com.tech.novagraphbackendgraphservice.application.screenplay.ScreenplayStatisticsApplicationService;
import com.tech.novagraphbackendgraphservice.domain.screenplay.service.ScreenplayStatisticsDomainService;
import com.tech.novagraphbackendmodel.vo.graph.ScreenplayVO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ScreenplayStatisticsApplicationServiceImpl implements ScreenplayStatisticsApplicationService {

    @Resource
    private ScreenplayStatisticsDomainService screenplayStatisticsDomainService;

    @Override
    public void playCountAdd(Map<Long, Long> countMap) {
        screenplayStatisticsDomainService.playCountAdd(countMap);
    }

    @Override
    public void favouriteCountUpdate(Map<Long, Long> countMap) {
        screenplayStatisticsDomainService.favouriteCountUpdate(countMap);
    }

    @Override
    public ScreenplayVO getScreenplayStatistics(ScreenplayVO screenplayVO, Long userId) {
        return screenplayStatisticsDomainService.getScreenplayStatistics(screenplayVO, userId);
    }

    @Override
    public List<ScreenplayVO> getScreenplayStatisticsList(List<ScreenplayVO> screenplayVOList, Long userId) {
        return screenplayStatisticsDomainService.getScreenplayStatisticsList(screenplayVOList, userId);
    }
}
