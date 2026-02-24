package com.tech.novagraphbackenduserservice.interfaces.controller.inner;

import cn.hutool.json.JSONUtil;
import com.tech.novagraphbackendmodel.user.entity.User;
import com.tech.novagraphbackendmodel.vo.user.UserFavoriteVO;
import com.tech.novagraphbackendmodel.vo.user.UserListVO;
import com.tech.novagraphbackendserviceclient.UserFeignClient;
import com.tech.novagraphbackenduserservice.application.service.UserApplicationService;
import com.tech.novagraphbackenduserservice.application.service.UserFavoriteApplicationService;
import com.tech.novagraphbackenduserservice.application.service.UserPlayHistoryApplicationService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/inner")
public class UserInnerController implements UserFeignClient {

    @Resource
    private UserApplicationService userApplicationService;

    @Resource
    private UserFavoriteApplicationService userFavoriteApplicationService;

    @Resource
    private UserPlayHistoryApplicationService userPlayHistoryApplicationService;

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

    @Override
    @GetMapping("/get/userHasFavorite")
    public UserFavoriteVO userHasFavorite(Long screenplayId, Long userId) {
        return userFavoriteApplicationService.userHasFavorite(screenplayId, userId);
    }

    @Override
    @GetMapping("/get/getUserFavoriteCount")
    public Long getUserFavoriteCount(Long screenplayId) {
        return userFavoriteApplicationService.getUserFavoriteCount(screenplayId);
    }

    @Override
    @GetMapping("/get/getUserPlayHistoryCount")
    public Long getUserPlayHistoryCount(Long spId) {
        return userFavoriteApplicationService.getUserFavoriteCount(spId);
    }
}
