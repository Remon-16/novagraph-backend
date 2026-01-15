package com.tech.novagraphbackenduserservice.domain.user.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tech.novagraphbackendcommon.exception.BusinessException;
import com.tech.novagraphbackendcommon.exception.ErrorCode;
import com.tech.novagraphbackendcommon.exception.ThrowUtils;
import com.tech.novagraphbackendcommon.utils.JwtUtils;
import com.tech.novagraphbackendmodel.user.entity.User;
import com.tech.novagraphbackendmodel.user.valueobject.UserRoleEnum;
import com.tech.novagraphbackendmodel.vo.user.LoginUserVO;
import com.tech.novagraphbackenduserservice.domain.user.repository.UserRepository;
import com.tech.novagraphbackenduserservice.domain.user.service.UserDomainService;
import com.tech.novagraphbackenduserservice.infrastructure.mapper.UserMapper;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.crypto.SecretKey;
import java.util.*;

@Slf4j
@Service
public class UserDomainServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserDomainService {

    @Resource
    private UserRepository userRepository;

    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword) {
        // 1. 判断输入
        ThrowUtils.throwIf(userAccount == null, ErrorCode.PARAMS_ERROR, "账号为空");
        ThrowUtils.throwIf(userPassword == null, ErrorCode.PARAMS_ERROR, "密码为空");
        ThrowUtils.throwIf(checkPassword == null, ErrorCode.PARAMS_ERROR, "确认密码为空");
        ThrowUtils.throwIf(!checkPassword.equals(userPassword), ErrorCode.PARAMS_ERROR, "输入密码不一致");

        // 2. 检查用户账号是否和数据库中已有的重复
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        long count = userRepository.getBaseMapper().selectCount(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号重复，请重新输入账号");
        }
        // 3. 密码一定要加密
        String encryptPassword = getEncryptPassword(userPassword);
        // 4. 插入数据到数据库中
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        user.setUserName(userAccount);
        user.setRoomCode(UUID.randomUUID().toString());
        user.setUserRole(UserRoleEnum.USER.getValue());
        boolean saveResult = userRepository.save(user);
        if (!saveResult) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败，数据库错误");
        }
        return user.getId();
    }

    @Override
    public LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        // 2. 对用户传递的密码进行加密
        String encryptPassword = getEncryptPassword(userPassword);
        // 3. 查询数据库中的用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userPassword", encryptPassword);
        User user = userRepository.getBaseMapper().selectOne(queryWrapper);
        // 不存在，抛异常
        if (user == null) {
            log.info("user login failed, userAccount cannot match userPassword");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在或者密码错误");
        }
        // 4. 保存用户的登录态
        SecretKey secretKey = JwtUtils.createSecretKey();

        Map<String, Object> claims = new HashMap<>();
        claims.put("userAccount", userAccount);
        claims.put("userId", user.getId());
        claims.put("userRole", user.getUserRole());

        String token = JwtUtils.generateToken(claims, user.getUserAccount(), secretKey);
        request.setAttribute(JwtUtils.JWT_HEADER, token);

        LoginUserVO loginUserVO = this.getLoginUserVO(user);
        loginUserVO.setToken(token);
        return loginUserVO;
    }

    @Override
    public User getLoginUser(HttpServletRequest request) {
        User currentUser = getUserFromRequest(request);
        if (currentUser.getId() == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        // 数据库查完整信息
        currentUser = userRepository.getById(currentUser.getId());
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return currentUser;
    }

    @Override
    public LoginUserVO getLoginUserVO(HttpServletRequest request) {
        User currentUser = getUserFromRequest(request);
        // 数据库查完整信息
        currentUser = this.getById(currentUser.getId());
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return this.getLoginUserVO(currentUser);
    }

    @Override
    public List<User> listByIds(Set<Long> userIdSet) {
        return userRepository.listByIds(userIdSet);
    }

    private LoginUserVO getLoginUserVO(User user) {
        if (user == null) {
            return null;
        }
        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtil.copyProperties(user, loginUserVO);
        return loginUserVO;
    }


    private String getEncryptPassword(String userPassword) {
        final String SALT = "novaSalt";
        return DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
    }

    private User getUserFromRequest(HttpServletRequest request){

        String userIdStr = request.getHeader("userId");
        String userAccount = request.getHeader("userAccount");
        String userRole = request.getHeader("userRole");
        if (userIdStr == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        try {
            // 构建用户对象
            User currentUser = new User();
            currentUser.setId(Long.parseLong(userIdStr));
            currentUser.setUserAccount(userAccount);
            currentUser.setUserRole(userRole);
            return currentUser;
        } catch (NumberFormatException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "用户信息格式错误");
        }
    }
}
