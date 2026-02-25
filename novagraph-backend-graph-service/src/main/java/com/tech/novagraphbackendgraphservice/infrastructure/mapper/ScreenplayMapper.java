package com.tech.novagraphbackendgraphservice.infrastructure.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tech.novagraphbackendmodel.dto.graph.ScreenplayQueryRequest;
import com.tech.novagraphbackendmodel.graph.entity.Screenplay;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tech.novagraphbackendmodel.graph.entity.ScreenplayWithStats;
import org.apache.ibatis.annotations.Param;

/**
* @author Remon
* @description 针对表【screenplay(剧本表)】的数据库操作Mapper
* @createDate 2025-12-11 16:46:54
* @Entity com.tech.novagraphbackendmodel.graph.entity.Screenplay
*/
public interface ScreenplayMapper extends BaseMapper<Screenplay> {
    Page<ScreenplayWithStats> selectScreenplayWithStatsPage(Page<ScreenplayWithStats> page,
                                                            @Param("request") ScreenplayQueryRequest request);

    ScreenplayWithStats selectScreenplayWithStatsById(@Param("screenplayId") Long screenplayId);
}




