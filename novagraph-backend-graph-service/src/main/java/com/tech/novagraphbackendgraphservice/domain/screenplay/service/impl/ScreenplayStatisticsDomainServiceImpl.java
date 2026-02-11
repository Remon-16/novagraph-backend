package com.tech.novagraphbackendgraphservice.domain.screenplay.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tech.novagraphbackendgraphservice.domain.screenplay.service.ScreenplayStatisticsDomainService;
import com.tech.novagraphbackendgraphservice.infrastructure.mapper.ScreenplayStatisticsMapper;
import com.tech.novagraphbackendmodel.graph.entity.ScreenplayStatistics;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class ScreenplayStatisticsDomainServiceImpl extends ServiceImpl<ScreenplayStatisticsMapper, ScreenplayStatistics>
        implements ScreenplayStatisticsDomainService {

    @Resource
    private ScreenplayStatisticsMapper screenplayStatisticsMapper;

    @Override
    public void playCountAdd(Map<Long, Long> countMap) {
        screenplayStatisticsMapper.batchUpdatePlayCount(countMap);
    }

    @Override
    public void favouriteCountUpdate(Map<Long, Long> countMap) {
        screenplayStatisticsMapper.batchUpdateFavouriteCount(countMap);
    }
}
