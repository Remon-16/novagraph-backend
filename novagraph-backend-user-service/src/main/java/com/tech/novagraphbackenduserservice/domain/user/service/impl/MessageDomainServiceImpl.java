package com.tech.novagraphbackenduserservice.domain.user.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tech.novagraphbackendcommon.exception.ErrorCode;
import com.tech.novagraphbackendcommon.exception.ThrowUtils;
import com.tech.novagraphbackendmodel.dto.user.UserMessageRequest;
import com.tech.novagraphbackendmodel.graph.entity.ScreenplayComment;
import com.tech.novagraphbackendmodel.graph.entity.ScreenplayThumb;
import com.tech.novagraphbackendmodel.user.constant.MessageConstant;
import com.tech.novagraphbackendmodel.user.entity.Message;
import com.tech.novagraphbackendmodel.user.entity.User;
import com.tech.novagraphbackendmodel.user.valueobject.MessageTypeEnum;
import com.tech.novagraphbackendmodel.vo.graph.ScreenplayCommentVO;
import com.tech.novagraphbackendmodel.vo.user.MessageVO;
import com.tech.novagraphbackendmodel.vo.user.UserVO;
import com.tech.novagraphbackendserviceclient.GraphFeignClient;
import com.tech.novagraphbackenduserservice.domain.user.repository.MessageRepository;
import com.tech.novagraphbackenduserservice.domain.user.service.MessageDomainService;
import com.tech.novagraphbackenduserservice.domain.user.service.UserDomainService;
import com.tech.novagraphbackenduserservice.infrastructure.mapper.MessageMapper;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MessageDomainServiceImpl extends ServiceImpl<MessageMapper, Message>
        implements MessageDomainService {
    @Lazy
    @Resource
    private UserDomainService userDomainService;

    @Resource
    private GraphFeignClient graphFeignClient;

    @Resource
    MessageMapper messageMapper;

    @Resource
    MessageRepository messageRepository;

    @Override
    public QueryWrapper<Message> getQueryWrapper(UserMessageRequest userMessageRequest) {
        QueryWrapper<Message> queryWrapper = new QueryWrapper<>();
        if (userMessageRequest == null) {
            return queryWrapper;
        }
        Long userId = userMessageRequest.getUserId();
        String messageType = userMessageRequest.getMessageType();
        String sortField = userMessageRequest.getSortField();
        String sortOrder = userMessageRequest.getSortOrder();
        queryWrapper.eq(ObjUtil.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq(ObjUtil.isNotEmpty(messageType), "messageType", messageType);
        queryWrapper.orderBy(StrUtil.isNotEmpty(sortField), sortOrder.equals("ascend"), sortField);
        return queryWrapper;
    }

    @Override
    public void changeMessageStatus(Long messageId, String status) {
        Message message = new Message();
        message.setId(messageId);
        message.setMessageState(status);
        this.updateById(message);
    }

    @Override
    public void messageSend(Message message) {
        this.save(message);
    }

    @Override
    public void messageBatchSend(List<Message> messages) {
        messageRepository.saveBatch(messages);
    }

    @Override
    public void allMessageREAD(Long userId, String messageType) {
        messageMapper.updateReadByUser(userId, MessageConstant.MESSAGE_READ, messageType);
    }

    @Override
    public Page<MessageVO> listMessageVoByPage(UserMessageRequest userMessageRequest, HttpServletRequest request) {
        long current = userMessageRequest.getCurrent();
        long size = userMessageRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 30, ErrorCode.PARAMS_ERROR);
        Page<Message> messagePage = page(new Page<>(current, size),
                getQueryWrapper(userMessageRequest));

        return getMessageVoPage(messagePage, userMessageRequest.getMessageType());
    }

    @Override
    public Boolean getExistUnReadMessage(UserMessageRequest userMessageRequest) {
        QueryWrapper<Message> queryWrapper = new QueryWrapper<>();
        if (userMessageRequest == null) {
            return false;
        }
        Long userId = userMessageRequest.getUserId();
        queryWrapper.eq(ObjUtil.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq("messageState", "0");
        Page<Message> messagePage = page(new Page<>(1, 10),
                queryWrapper);
        return !messagePage.getRecords().isEmpty();
    }

    private Page<MessageVO> getMessageVoPage(Page<Message> messagePage, String messageType) {
        List<Message> messageList = messagePage.getRecords();
        Page<MessageVO> messageVoPage = new Page<>(messagePage.getCurrent(), messagePage.getSize(), messagePage.getTotal());
        if(CollUtil.isEmpty(messageList)){
            return messageVoPage;
        }
        List<MessageVO> messageVoList = messageList.stream().map(MessageVO::objToVo).toList();
        if(MessageTypeEnum.THUMB.getValue().equals(messageType)){
            messageVoList.forEach((messageVo) -> {
                ScreenplayThumb thumb = graphFeignClient.getThumbById(messageVo.getCommentId());
                if(thumb != null){
                    User user = userDomainService.getById(thumb.getUserId());
                    ScreenplayCommentVO screenplayCommentVO = new ScreenplayCommentVO();
                    UserVO userVO = userDomainService.getUserVO(user);
                    screenplayCommentVO.setUser(userVO);
                    messageVo.setScreenplayCommentVO(screenplayCommentVO);
                }else{
                    messageVo.setContent(MessageConstant.DELETED_MESSAGE);
                }
            });
        } else if (MessageTypeEnum.COMMENT.getValue().equals(messageType)) {
            messageVoList.forEach((messageVo) -> {
                ScreenplayComment screenplayComment = graphFeignClient.getScreenplayCommentById(messageVo.getCommentId());
                if(screenplayComment != null){
                    ScreenplayCommentVO screenplayCommentVO = ScreenplayCommentVO.objToVo(screenplayComment);
                    User user = userDomainService.getById(screenplayCommentVO.getUserId());
                    UserVO userVO = userDomainService.getUserVO(user);
                    screenplayCommentVO.setUser(userVO);
                    messageVo.setScreenplayCommentVO(screenplayCommentVO);
                }else{
                    messageVo.setContent(MessageConstant.DELETED_MESSAGE);
                }
            });
        }
        messageVoPage.setRecords(messageVoList);
        return messageVoPage;
    }
}
