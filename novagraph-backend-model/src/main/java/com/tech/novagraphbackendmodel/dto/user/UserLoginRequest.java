package com.tech.novagraphbackendmodel.dto.user;

import lombok.Data;

import java.io.Serial;

@Data
public class UserLoginRequest {

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 密码
     */
    private String userPassword;

    @Serial
    private static final long serialVersionUID = 1871291290712L;
}
