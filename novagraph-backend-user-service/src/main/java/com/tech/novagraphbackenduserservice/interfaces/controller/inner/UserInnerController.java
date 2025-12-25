package com.tech.novagraphbackenduserservice.interfaces.controller.inner;

import cn.hutool.json.JSONUtil;
import com.tech.novagraphbackendmodel.user.entity.User;
import com.tech.novagraphbackendmodel.vo.user.UserListVO;
import com.tech.novagraphbackendserviceclient.UserFeignClient;
import com.tech.novagraphbackenduserservice.application.service.UserApplicationService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/inner")
public class UserInnerController implements UserFeignClient {

    @Resource
    private UserApplicationService userApplicationService;

    @Override
    @GetMapping("/get/id")
    public User getUserById(long userId) {
        return userApplicationService.getUserById(userId);
    }

    @Override
    @PostMapping("/post/ids")
    public UserListVO listByIds(@RequestBody Set<Long> idList) {
        UserListVO userListVO = new UserListVO();
        List<User> userList = userApplicationService.listByIds(idList);
        userListVO.setUserListJson(JSONUtil.toJsonStr(userList));
        return userListVO;
    }
}
