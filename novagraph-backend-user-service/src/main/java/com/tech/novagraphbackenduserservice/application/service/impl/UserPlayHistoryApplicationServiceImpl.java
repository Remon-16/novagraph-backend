package com.tech.novagraphbackenduserservice.application.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tech.novagraphbackendmodel.dto.user.UserPlayHistoryAddRequest;
import com.tech.novagraphbackendmodel.dto.user.UserPlayHistoryQueryRequest;
import com.tech.novagraphbackendmodel.user.entity.User;
import com.tech.novagraphbackendmodel.vo.user.UserPlayHistoryVO;
import com.tech.novagraphbackenduserservice.application.service.UserPlayHistoryApplicationService;
import com.tech.novagraphbackenduserservice.domain.user.service.UserPlayHistoryDomainService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;


@Service
public class UserPlayHistoryApplicationServiceImpl implements UserPlayHistoryApplicationService {

    @Resource
    private UserPlayHistoryDomainService userPlayHistoryDomainService;

    @Override
    public void addUserPlayHistory(UserPlayHistoryAddRequest userPlayHistoryAddRequest, User loginUser) {
        userPlayHistoryDomainService.addUserPlayHistory(userPlayHistoryAddRequest, loginUser);
    }

    @Override
    public Page<UserPlayHistoryVO> getUserPlayHistory(UserPlayHistoryQueryRequest userPlayHistoryQueryRequest) {
        return userPlayHistoryDomainService.getUserPlayHistory(userPlayHistoryQueryRequest);
    }

    @Override
    public Long getUserPlayHistoryCount(Long spId) {
        return userPlayHistoryDomainService.getUserPlayHistoryCount(spId);
    }

    @Override
    public Long getUserPlayHistoryCountForLogin(Long userId) {
        return userPlayHistoryDomainService.getUserPlayHistoryCountForLogin(userId);
    }
}
