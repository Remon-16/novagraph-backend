package com.tech.novagraphbackenduserservice.domain.user.service.impl;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tech.novagraphbackendcommon.cache.ValuePageCacheTemplate;
import com.tech.novagraphbackendcommon.exception.ErrorCode;
import com.tech.novagraphbackendcommon.exception.ThrowUtils;
import com.tech.novagraphbackendmodel.dto.user.UserPostAddRequest;
import com.tech.novagraphbackendmodel.dto.user.UserPostQueryRequest;
import com.tech.novagraphbackendmodel.user.entity.UserPost;
import com.tech.novagraphbackenduserservice.domain.user.repository.UserPostRepository;
import com.tech.novagraphbackenduserservice.domain.user.service.UserPostDomainService;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserPostDomainServiceImpl implements UserPostDomainService {

    @Resource
    private UserPostRepository userPostRepository;

    @Override
    public UserPost saveOrUpdate(UserPostAddRequest userPostAddRequest) {
        ThrowUtils.throwIf(userPostAddRequest == null, ErrorCode.PARAMS_ERROR);
        UserPost userPost = new UserPost();
        BeanUtils.copyProperties(userPostAddRequest, userPost);
        if (userPostAddRequest.getId() == null) {
            userPostRepository.save(userPost);
        }else{
            userPostRepository.updateById(userPost);
        }
        return userPost;
    }

    @Override
    public void delete(Long userPostId) {
        ThrowUtils.throwIf(userPostId == null, ErrorCode.PARAMS_ERROR);
        userPostRepository.removeById(userPostId);
    }

    @Override
    public Page<UserPost> getUserPostPage(UserPostQueryRequest userPostQueryRequest) {
        return null;
    }

    private QueryWrapper<UserPost> getQueryWrapper(UserPostQueryRequest userPostQueryRequest){
        QueryWrapper<UserPost> queryWrapper = new QueryWrapper<>();
        if(userPostQueryRequest == null){
            return queryWrapper;
        }
        List<Long> userIdList = userPostQueryRequest.getUserIdList();
        queryWrapper.in(ObjUtil.isNotEmpty(userIdList), "userId", userIdList);
        String sortField = userPostQueryRequest.getSortField();
        String sortOrder = userPostQueryRequest.getSortOrder();
        queryWrapper.orderBy(StrUtil.isNotEmpty(sortField), sortOrder.equals("ascend"), sortField);
        return queryWrapper;
    }
}
