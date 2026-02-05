package com.tech.novagraphbackenduserservice.infrastructure.mapper;

import com.tech.novagraphbackendmodel.user.entity.UserPostStatistics;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

/**
* @author Remon
* @description 针对表【user_post_statistics(动态统计数据)】的数据库操作Mapper
* @createDate 2026-01-26 17:25:18
* @Entity com.tech.novagraphbackendmodel.user.entity.UserPostStatistics
*/
public interface UserPostStatisticsMapper extends BaseMapper<UserPostStatistics> {
    void batchUpdateThumbCount(@Param("countMap") Map<Long, Long> countMap);
}




