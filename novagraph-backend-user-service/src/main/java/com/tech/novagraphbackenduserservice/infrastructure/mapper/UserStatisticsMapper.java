package com.tech.novagraphbackenduserservice.infrastructure.mapper;

import com.tech.novagraphbackendmodel.user.entity.UserStatistics;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

/**
* @author Remon
* @description 针对表【user_statistics(用户统计数据)】的数据库操作Mapper
* @createDate 2026-01-26 17:25:30
* @Entity com.tech.novagraphbackendmodel.user.entity.UserStatistics
*/
public interface UserStatisticsMapper extends BaseMapper<UserStatistics> {
    /**
     * 更新关注数量
     */
    void batchUpdateFollowingCount(@Param("countMap") Map<Long, Long> countMap);
    /**
     * 更新粉丝数量
     */
    void batchUpdateFollowerCount(@Param("countMap") Map<Long, Long> countMap);
}




