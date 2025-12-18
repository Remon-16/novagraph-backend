package com.tech.novagraphbackenduserservice.application.service.impl;

import com.tech.novagraphbackendmodel.user.entity.User;
import com.tech.novagraphbackendmodel.vo.user.LoginUserVO;
import com.tech.novagraphbackenduserservice.application.service.UserApplicationService;
import com.tech.novagraphbackenduserservice.domain.user.service.UserDomainService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

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
}
