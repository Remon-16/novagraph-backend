package com.tech.novagraphbackenduserservice.interfaces.controller.assembler;

import com.tech.novagraphbackendmodel.dto.user.UserUpdateInfoRequest;
import com.tech.novagraphbackendmodel.user.entity.User;
import org.springframework.beans.BeanUtils;

public class UserAssembler {

    public static User toUserEntity(UserUpdateInfoRequest request) {
        User user = new User();
        BeanUtils.copyProperties(request, user);
        return user;
    }
}
