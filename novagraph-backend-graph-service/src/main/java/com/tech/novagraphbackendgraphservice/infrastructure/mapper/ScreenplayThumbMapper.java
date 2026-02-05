package com.tech.novagraphbackendgraphservice.infrastructure.mapper;

import com.tech.novagraphbackendmodel.graph.entity.ScreenplayThumb;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
* @author Remon
* @description 针对表【screenplay_thumb(剧本点赞记录表)】的数据库操作Mapper
* @createDate 2025-12-23 15:44:15
* @Entity com.tech.novagraphbackendmodel.graph.entity.ScreenplayThumb
*/
public interface ScreenplayThumbMapper extends BaseMapper<ScreenplayThumb> {
    void batchIgnoreInsert(@Param("thumbs") List<ScreenplayThumb> thumbs);
}




