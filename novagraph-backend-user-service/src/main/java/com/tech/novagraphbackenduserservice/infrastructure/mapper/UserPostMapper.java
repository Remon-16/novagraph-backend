package com.tech.novagraphbackenduserservice.infrastructure.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tech.novagraphbackendmodel.dto.user.UserPostQueryRequest;
import com.tech.novagraphbackendmodel.user.entity.UserPost;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tech.novagraphbackendmodel.user.entity.UserPostWithStats;
import org.apache.ibatis.annotations.Param;


/**
* @author Remon
* @description 针对表【user_post(用户动态表)】的数据库操作Mapper
* @createDate 2026-01-26 17:25:12
* @Entity com.tech.novagraphbackendmodel.user.entity.UserPost
*/
public interface UserPostMapper extends BaseMapper<UserPost> {
    Page<UserPostWithStats> selectUserPostWithStatsPage(Page<UserPostWithStats> page,
                                                        @Param("request") UserPostQueryRequest request);
}




