package com.tech.novagraphbackenduserservice.interfaces.controller;

import com.tech.novagraphbackendcommon.common.BaseResponse;
import com.tech.novagraphbackendcommon.common.ResultUtils;
import com.tech.novagraphbackendcommon.exception.BusinessException;
import com.tech.novagraphbackendcommon.exception.ErrorCode;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class UserController {

    @GetMapping("/hello")
    public BaseResponse<String> demo(){
        return ResultUtils.success("Hello World");
    }

    @GetMapping("/error")
    public BaseResponse<String> error(){
        throw new BusinessException(ErrorCode.SYSTEM_ERROR);
    }
}
