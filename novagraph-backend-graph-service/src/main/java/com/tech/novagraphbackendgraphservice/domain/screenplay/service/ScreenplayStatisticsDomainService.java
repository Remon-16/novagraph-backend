package com.tech.novagraphbackendgraphservice.domain.screenplay.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tech.novagraphbackendmodel.graph.entity.ScreenplayStatistics;

import java.util.Map;

public interface ScreenplayStatisticsDomainService extends IService<ScreenplayStatistics> {
    void playCountAdd(Map<Long, Long> countMap);

    void favouriteCountUpdate(Map<Long, Long> countMap);
}
