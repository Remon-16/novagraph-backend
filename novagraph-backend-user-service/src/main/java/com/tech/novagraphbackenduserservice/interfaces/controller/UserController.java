package com.tech.novagraphbackenduserservice.interfaces.controller;

import com.tech.novagraphbackendcommon.common.BaseResponse;
import com.tech.novagraphbackendcommon.common.ResultUtils;
import com.tech.novagraphbackendcommon.exception.BusinessException;
import com.tech.novagraphbackendcommon.exception.ErrorCode;
import com.tech.novagraphbackendcommon.exception.ThrowUtils;
import com.tech.novagraphbackendmodel.dto.user.UserLoginRequest;
import com.tech.novagraphbackendmodel.dto.user.UserRegisterRequest;
import com.tech.novagraphbackendmodel.dto.user.UserUpdateInfoRequest;
import com.tech.novagraphbackendmodel.user.entity.User;
import com.tech.novagraphbackendmodel.vo.user.LoginUserVO;
import com.tech.novagraphbackenduserservice.application.service.UserApplicationService;
import com.tech.novagraphbackenduserservice.interfaces.controller.assembler.UserAssembler;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/")
public class UserController {

    @Resource
    private UserApplicationService userApplicationService;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        ThrowUtils.throwIf(userRegisterRequest == null, ErrorCode.PARAMS_ERROR);
        long result = userApplicationService.userRegister(userRegisterRequest.getUserAccount(),
                userRegisterRequest.getUserPassword(), userRegisterRequest.getCheckPassword());
        return ResultUtils.success(result);
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public BaseResponse<LoginUserVO> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(userLoginRequest == null, ErrorCode.PARAMS_ERROR);
        LoginUserVO loginUserVO = userApplicationService.userLogin(userLoginRequest.getUserAccount(),
                userLoginRequest.getUserPassword(), request);
        return ResultUtils.success(loginUserVO);
    }

    /**
     * 获取当前登录用户
     */
    @GetMapping("/get/login")
    public BaseResponse<LoginUserVO> getLoginUser(HttpServletRequest request) {
        return ResultUtils.success(userApplicationService.getLoginUserVO(request));
    }

    /**
     * 给用户用的更新用户信息
     */
    @PostMapping("/update_user_info")
    public BaseResponse<Boolean> updateUserInfo(UserUpdateInfoRequest userUpdateInfoRequest){
        if (userUpdateInfoRequest == null || userUpdateInfoRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User userEntity = UserAssembler.toUserEntity(userUpdateInfoRequest);
        userApplicationService.updateUser(userEntity);
        return ResultUtils.success(true);
    }

    /**
     * 用户上传头像
     */
    @PostMapping("/update_user_avatar")
    public BaseResponse<Boolean> updateUserAvatar(
            @RequestPart("file") MultipartFile multipartFile,
            UserUpdateInfoRequest userUpdateInfoRequest,
            HttpServletRequest request){
        User loginUser = userApplicationService.getLoginUser(request);
        boolean res = userApplicationService.updateUserAvatar(multipartFile, userUpdateInfoRequest, loginUser);
        return ResultUtils.success(res);
    }
}
