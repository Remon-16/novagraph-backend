package com.tech.novagraphbackendmodel.vo.user;

import cn.hutool.core.lang.TypeReference;
import cn.hutool.json.JSONUtil;
import com.tech.novagraphbackendmodel.user.entity.User;
import lombok.Data;

import java.util.List;

@Data
public class UserListVO {
    private String userListJson;


    public List<User> getUserList(String userListStr){

        return JSONUtil.toBean(
                userListStr,
                new TypeReference<List<User>>() {}, // 指定完整泛型结构
                false // 是否忽略转换错误
        );
    }
}