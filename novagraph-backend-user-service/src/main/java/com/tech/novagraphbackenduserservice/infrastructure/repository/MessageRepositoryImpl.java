package com.tech.novagraphbackenduserservice.infrastructure.repository;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tech.novagraphbackendmodel.user.entity.Message;
import com.tech.novagraphbackenduserservice.domain.user.repository.MessageRepository;
import com.tech.novagraphbackenduserservice.infrastructure.mapper.MessageMapper;
import org.springframework.stereotype.Service;

/**
* @author Remon
* @description 针对表【message(消息表)】的数据库操作Service实现
* @createDate 2026-01-20 16:07:40
*/
@Service
public class MessageRepositoryImpl extends ServiceImpl<MessageMapper, Message>
    implements MessageRepository {

}




