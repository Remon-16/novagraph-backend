package com.tech.novagraphbackenduserservice.application.service;

import com.tech.novagraphbackendmodel.dto.user.UserUpdateInfoRequest;
import com.tech.novagraphbackendmodel.user.entity.User;
import com.tech.novagraphbackendmodel.vo.user.LoginUserVO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;

public interface UserApplicationService {

    /**
     * 用户注册
     *
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     * @return 新用户 id
     */
    long userRegister(String userAccount, String userPassword, String checkPassword);

    /**
     * 用户登录
     *
     * @param userAccount  用户账户
     * @param userPassword 用户密码
     * @param request
     * @return 脱敏后的用户信息
     */
    LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 获取登录用户信息
     * @param request
     * @return
     */
    LoginUserVO getLoginUserVO(HttpServletRequest request);

    User getUserById(long id);

    List<User> listByIds(Set<Long> userIdSet);

    void updateUser(User user);

    boolean updateUserAvatar(MultipartFile avatar, UserUpdateInfoRequest userUpdateInfoRequest, User loginUser);
}
