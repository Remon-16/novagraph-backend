package com.tech.novagraphbackenduserservice.application.service.impl;

import com.tech.novagraphbackendcommon.exception.ErrorCode;
import com.tech.novagraphbackendcommon.exception.ThrowUtils;
import com.tech.novagraphbackendmodel.dto.user.UserUpdateInfoRequest;
import com.tech.novagraphbackendmodel.user.entity.User;
import com.tech.novagraphbackendmodel.vo.user.LoginUserVO;
import com.tech.novagraphbackenduserservice.application.service.UserApplicationService;
import com.tech.novagraphbackenduserservice.domain.user.service.UserDomainService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;

@Service
public class UserApplicationServiceImpl implements UserApplicationService {

    @Resource
    private UserDomainService userDomainService;

    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword) {
        return userDomainService.userRegister(userAccount, userPassword, checkPassword);
    }

    @Override
    public LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        return userDomainService.userLogin(userAccount, userPassword, request);
    }

    @Override
    public User getLoginUser(HttpServletRequest request) {
        return userDomainService.getLoginUser(request);
    }

    @Override
    public LoginUserVO getLoginUserVO(HttpServletRequest request) {
        return userDomainService.getLoginUserVO(request);
    }

    @Override
    public List<User> listByIds(Set<Long> userIdSet) {
        return userDomainService.listByIds(userIdSet);
    }

    @Override
    public User getUserById(long id) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        User user = userDomainService.getById(id);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR);
        user.setUserPassword("");
        return user;
    }

    @Override
    public void updateUser(User user) {
        boolean result = userDomainService.updateById(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
    }

    @Override
    public boolean updateUserAvatar(MultipartFile avatar, UserUpdateInfoRequest userUpdateInfoRequest, User loginUser) {
        return userDomainService.updateUserAvatar(avatar, userUpdateInfoRequest, loginUser);
    }
}
