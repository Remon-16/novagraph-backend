package com.tech.novagraphbackendgraphservice.infrastructure.mapper;

import com.tech.novagraphbackendmodel.graph.entity.Screenplay;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

/**
* @author Remon
* @description 针对表【screenplay(剧本表)】的数据库操作Mapper
* @createDate 2025-12-11 16:46:54
* @Entity com.tech.novagraphbackendmodel.graph.entity.Screenplay
*/
public interface ScreenplayMapper extends BaseMapper<Screenplay> {
    void batchUpdateThumbCount(@Param("countMap") Map<Long, Long> countMap);
}




