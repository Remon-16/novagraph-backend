package com.tech.novagraphbackendgraphservice.application.screenplay.Impl;

import com.tech.novagraphbackendgraphservice.application.screenplay.ScreenplayStatisticsApplicationService;
import com.tech.novagraphbackendgraphservice.domain.screenplay.service.ScreenplayStatisticsDomainService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class ScreenplayStatisticsApplicationServiceImpl implements ScreenplayStatisticsApplicationService {

    @Resource
    private ScreenplayStatisticsDomainService screenplayStatisticsDomainService;

    @Override
    public void playCountAdd(Map<Long, Long> countMap) {
        screenplayStatisticsDomainService.playCountAdd(countMap);
    }
}
