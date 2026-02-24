package com.tech.novagraphbackenduserservice.application.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tech.novagraphbackendmodel.dto.user.UserPlayHistoryAddRequest;
import com.tech.novagraphbackendmodel.dto.user.UserPlayHistoryQueryRequest;
import com.tech.novagraphbackendmodel.user.entity.User;
import com.tech.novagraphbackendmodel.vo.user.UserPlayHistoryVO;

public interface UserPlayHistoryApplicationService {

    /**
     * 添加一条用户浏览记录
     */
    void addUserPlayHistory(UserPlayHistoryAddRequest userPlayHistoryAddRequest, User loginUser);

    /**
     * 查询用户浏览记录
     */
    Page<UserPlayHistoryVO> getUserPlayHistory(UserPlayHistoryQueryRequest userPlayHistoryQueryRequest);

    Long getUserPlayHistoryCount(Long spId);
}
