package com.tech.novagraphbackenduserservice.application.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tech.novagraphbackendmodel.dto.user.UserMessageRequest;
import com.tech.novagraphbackendmodel.user.entity.Message;
import com.tech.novagraphbackendmodel.vo.user.MessageVO;
import com.tech.novagraphbackenduserservice.application.service.MessageApplicationService;
import com.tech.novagraphbackenduserservice.domain.user.service.MessageDomainService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MessageApplicationServiceImpl implements MessageApplicationService {

    @Resource
    private MessageDomainService messageDomainService;

    @Override
    public void changeMessageStatus(Long messageId, String status) {
        messageDomainService.changeMessageStatus(messageId, status);
    }

    @Override
    public void messageSend(Message message) {
        messageDomainService.messageSend(message);
    }

    @Override
    public void messageBatchSend(List<Message> messages) {
        messageDomainService.messageBatchSend(messages);
    }

    @Override
    public void allMessageREAD(Long userId, String messageType) {
        messageDomainService.allMessageREAD(userId, messageType);
    }

    @Override
    public Page<MessageVO> listMessageVoByPage(UserMessageRequest userMessageRequest, HttpServletRequest request) {
        return messageDomainService.listMessageVoByPage(userMessageRequest, request);
    }

    @Override
    public Boolean getExistUnReadMessage(UserMessageRequest userMessageRequest) {
        return messageDomainService.getExistUnReadMessage(userMessageRequest);
    }
}
