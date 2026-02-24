package com.tech.novagraphbackendgraphservice.domain.screenplay.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tech.novagraphbackendmodel.graph.entity.ScreenplayStatistics;
import com.tech.novagraphbackendmodel.user.entity.User;
import com.tech.novagraphbackendmodel.vo.graph.ScreenplayVO;

import java.util.List;
import java.util.Map;

public interface ScreenplayStatisticsDomainService extends IService<ScreenplayStatistics> {
    void playCountAdd(Map<Long, Long> countMap);

    void favouriteCountUpdate(Map<Long, Long> countMap);

    ScreenplayVO getScreenplayStatistics(ScreenplayVO screenplayVO, Long userId);

    List<ScreenplayVO> getScreenplayStatisticsList(List<ScreenplayVO> screenplayVOList, Long userId);
}
