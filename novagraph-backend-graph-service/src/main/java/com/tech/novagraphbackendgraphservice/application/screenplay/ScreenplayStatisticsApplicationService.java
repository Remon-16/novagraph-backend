package com.tech.novagraphbackendgraphservice.application.screenplay;

import com.tech.novagraphbackendmodel.vo.graph.ScreenplayVO;

import java.util.List;
import java.util.Map;

public interface ScreenplayStatisticsApplicationService {
    void playCountAdd(Map<Long, Long> countMap);

    void favouriteCountUpdate(Map<Long, Long> countMap);

    ScreenplayVO getScreenplayStatistics(ScreenplayVO screenplayVO, Long userId);

    List<ScreenplayVO> getScreenplayStatisticsList(List<ScreenplayVO> screenplayVOList, Long userId);
}
