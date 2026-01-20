package com.tech.novagraphbackenduserservice.interfaces.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tech.novagraphbackendcommon.common.BaseResponse;
import com.tech.novagraphbackendcommon.common.ResultUtils;
import com.tech.novagraphbackendmodel.dto.user.UserMessageRequest;
import com.tech.novagraphbackendmodel.vo.user.MessageVO;
import com.tech.novagraphbackenduserservice.application.service.MessageApplicationService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/message")
public class MessageController {

    @Resource
    private MessageApplicationService messageApplicationService;

    @PostMapping("/allMessageREAD")
    public BaseResponse<Boolean> allMessageREAD(@RequestBody UserMessageRequest userMessageRequest){
        messageApplicationService.allMessageREAD(userMessageRequest.getUserId(), userMessageRequest.getMessageType());
        return ResultUtils.success(true);
    }

    @PostMapping("/listMessageVoByPage")
    public BaseResponse<Page<MessageVO>> listMessageVoByPage(@RequestBody UserMessageRequest userMessageRequest, HttpServletRequest request){
        return ResultUtils.success(messageApplicationService.listMessageVoByPage(userMessageRequest, request));
    }

    @PostMapping("/changeMessageStatus")
    public BaseResponse<Boolean> changeMessageStatus(@RequestBody UserMessageRequest userMessageRequest){
        messageApplicationService.changeMessageStatus(userMessageRequest.getMessageId(), userMessageRequest.getMessageStatus());
        return ResultUtils.success(true);
    }
    @PostMapping("/getExistUnReadMessage")
    public BaseResponse<Boolean> getExistUnReadMessage(@RequestBody UserMessageRequest userMessageRequest){
        return ResultUtils.success(messageApplicationService.getExistUnReadMessage(userMessageRequest));
    }
}
