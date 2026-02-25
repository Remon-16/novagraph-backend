package com.tech.novagraphbackenduserservice.application.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tech.novagraphbackendcommon.common.CanalHandleVO;
import com.tech.novagraphbackendmodel.dto.user.UserPostAddRequest;
import com.tech.novagraphbackendmodel.dto.user.UserPostQueryRequest;
import com.tech.novagraphbackendmodel.user.entity.UserPost;
import com.tech.novagraphbackendmodel.vo.user.UserPostVO;

import java.util.List;

public interface UserPostApplicationService {
    UserPost saveOrUpdate(UserPostAddRequest userPostAddRequest);

    void delete(Long userPostId);

    Page<UserPostVO> getUserPostVOPage(UserPostQueryRequest userPostQueryRequest);

    void canalHandleUserPost(List<CanalHandleVO> canalHandleVoList);
}
