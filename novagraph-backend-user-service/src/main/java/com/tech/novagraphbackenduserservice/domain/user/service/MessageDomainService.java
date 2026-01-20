package com.tech.novagraphbackenduserservice.domain.user.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tech.novagraphbackendmodel.dto.user.UserMessageRequest;
import com.tech.novagraphbackendmodel.user.entity.Message;
import com.tech.novagraphbackendmodel.vo.user.MessageVO;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public interface MessageDomainService extends IService<Message> {
    QueryWrapper<Message> getQueryWrapper(UserMessageRequest userMessageRequest);

    void changeMessageStatus(Long messageId, String status);

    void messageSend(Message message);

    void messageBatchSend(List<Message> messages);

    void allMessageREAD(Long userId, String messageType);

    Page<MessageVO> listMessageVoByPage(UserMessageRequest userMessageRequest, HttpServletRequest request);

    Boolean getExistUnReadMessage(UserMessageRequest userMessageRequest);
}
