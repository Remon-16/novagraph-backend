package com.tech.novagraphbackenduserservice.domain.user.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tech.novagraphbackendmodel.dto.user.UserPlayHistoryAddRequest;
import com.tech.novagraphbackendmodel.dto.user.UserPlayHistoryQueryRequest;
import com.tech.novagraphbackendmodel.user.entity.User;
import com.tech.novagraphbackendmodel.user.entity.UserPlayHistory;
import com.tech.novagraphbackendmodel.vo.user.UserPlayHistoryVO;

import java.util.List;

public interface UserPlayHistoryDomainService extends IService<UserPlayHistory> {
    /**
     * 添加一条用户浏览记录
     */
    void addUserPlayHistory(UserPlayHistoryAddRequest userPlayHistoryAddRequest, User loginUser);

    /**
     * 查询用户浏览记录
     */
    Page<UserPlayHistoryVO> getUserPlayHistory(UserPlayHistoryQueryRequest userPlayHistoryQueryRequest);
}
