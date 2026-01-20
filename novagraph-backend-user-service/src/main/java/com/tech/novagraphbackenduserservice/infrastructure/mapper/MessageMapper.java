package com.tech.novagraphbackenduserservice.infrastructure.mapper;

import com.tech.novagraphbackendmodel.user.entity.Message;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

/**
* @author Remon
* @description 针对表【message(消息表)】的数据库操作Mapper
* @createDate 2026-01-20 16:07:40
* @Entity com.tech.novagraphbackendmodel.user.entity.Message
*/
public interface MessageMapper extends BaseMapper<Message> {
    void updateReadByUser(@Param("userId") Long userId, @Param("messageStatus") String messageStatus, @Param("messageType") String messageType);
}




