package com.tech.novagraphbackendgraphservice.infrastructure.mapper;

import com.tech.novagraphbackendmodel.graph.entity.ScreenplayStatistics;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

/**
* @author Remon
* @description 针对表【screenplay_statistics(剧本统计数据)】的数据库操作Mapper
* @createDate 2026-01-26 17:27:13
* @Entity com.tech.novagraphbackendmodel.graph.entity.ScreenplayStatistics
*/
public interface ScreenplayStatisticsMapper extends BaseMapper<ScreenplayStatistics> {
    void batchUpdateThumbCount(@Param("countMap") Map<Long, Long> countMap);

    void batchUpdatePlayCount(@Param("countMap") Map<Long, Long> countMap);
}




