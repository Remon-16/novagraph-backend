package com.tech.novagraphbackendserviceclient;

import com.alibaba.cloud.commons.lang.StringUtils;
import com.tech.novagraphbackendcommon.exception.BusinessException;
import com.tech.novagraphbackendcommon.exception.ErrorCode;
import com.tech.novagraphbackendmodel.user.entity.User;
import com.tech.novagraphbackendmodel.vo.user.UserListVO;
import com.tech.novagraphbackendmodel.vo.user.UserVO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.BeanUtils;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Set;

@FeignClient(name = "novagraph-backend-user-service", path = "/api/user/inner")
public interface UserFeignClient {

    /**
     * 根据 id 获取用户列表
     * @param idList
     * @return
     */
    @PostMapping("/post/ids")
    UserListVO listByIds(@RequestBody Set<Long> idList);

    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    default User getLoginUser(HttpServletRequest request){
        // 构建用户对象
        User currentUser = getUserFromRequest(request);
        if (currentUser.getId() == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return currentUser;
    }

    /**
     * 获取脱敏的用户信息
     *
     * @param user
     * @return
     */
    default UserVO getUserVO(User user) {
        if (user == null) {
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return userVO;
    }

    default User getUserFromRequest(HttpServletRequest request){
        String userIdStr = request.getHeader("userId");
        String userAccount = request.getHeader("userAccount");
        String userRole = request.getHeader("userRole");

        if (StringUtils.isEmpty(userIdStr)) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        // 构建用户对象
        User currentUser = new User();
        try {
            currentUser.setId(Long.parseLong(userIdStr));
            currentUser.setUserAccount(userAccount);
            currentUser.setUserRole(userRole);
            return currentUser;
        } catch (NumberFormatException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "用户信息格式错误");
        }
    }
}
