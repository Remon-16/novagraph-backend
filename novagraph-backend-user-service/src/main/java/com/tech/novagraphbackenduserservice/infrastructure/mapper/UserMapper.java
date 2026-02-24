package com.tech.novagraphbackenduserservice.infrastructure.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tech.novagraphbackendmodel.user.entity.User;
import com.tech.novagraphbackendmodel.user.entity.UserWithStats;
import org.apache.ibatis.annotations.Param;


/**
* @author Remon
* @description 针对表【user(用户)】的数据库操作Mapper
* @createDate 2025-11-25 15:35:28
* @Entity com.tech.novagraphbackendmodel.user.entity.User
*/
public interface UserMapper extends BaseMapper<User> {
    UserWithStats selectUserWithStatsById(@Param("userId") Long userId);
}




