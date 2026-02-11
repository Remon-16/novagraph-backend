package com.tech.novagraphbackendgraphservice.application.screenplay;

import java.util.Map;

public interface ScreenplayStatisticsApplicationService {
    void playCountAdd(Map<Long, Long> countMap);

    void favouriteCountUpdate(Map<Long, Long> countMap);
}
