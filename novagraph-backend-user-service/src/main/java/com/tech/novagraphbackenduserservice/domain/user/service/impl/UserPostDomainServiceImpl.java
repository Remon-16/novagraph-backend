package com.tech.novagraphbackenduserservice.domain.user.service.impl;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tech.novagraphbackendcommon.cache.ValuePageCacheTemplate;
import com.tech.novagraphbackendcommon.cache.ZSetPageCacheTemplate;
import com.tech.novagraphbackendcommon.cache.bean.ZSetQueryBean;
import com.tech.novagraphbackendcommon.cache.bean.ZSetSaveBean;
import com.tech.novagraphbackendcommon.exception.ErrorCode;
import com.tech.novagraphbackendcommon.exception.ThrowUtils;
import com.tech.novagraphbackendmodel.dto.user.UserPostAddRequest;
import com.tech.novagraphbackendmodel.dto.user.UserPostQueryRequest;
import com.tech.novagraphbackendmodel.user.constant.UserCacheConstant;
import com.tech.novagraphbackendmodel.user.entity.UserPost;
import com.tech.novagraphbackendmodel.user.valueobject.UserPostEnum;
import com.tech.novagraphbackendmodel.vo.graph.ScreenplayVO;
import com.tech.novagraphbackendmodel.vo.user.UserPostVO;
import com.tech.novagraphbackendserviceclient.GraphFeignClient;
import com.tech.novagraphbackenduserservice.domain.user.repository.UserPostRepository;
import com.tech.novagraphbackenduserservice.domain.user.service.UserPostDomainService;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserPostDomainServiceImpl implements UserPostDomainService {

    @Resource
    private UserPostRepository userPostRepository;

    @Resource
    private ZSetPageCacheTemplate ZSetPageCacheTemplate;

    @Resource
    private GraphFeignClient graphFeignClient;

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
    public Page<UserPostVO> getUserPostVOPage(UserPostQueryRequest userPostQueryRequest) {
        String order = userPostQueryRequest.getSortOrder();
        Long loginUserId = userPostQueryRequest.getLoginUserId();
        String sortedKey = UserCacheConstant.getUserPostSortedCacheKey(loginUserId);
        String sortedTotalKey = UserCacheConstant.getUserPostSortedTotalCacheKey(loginUserId);

        ZSetQueryBean ZSetQueryBean = new ZSetQueryBean();
        ZSetSaveBean zSetSaveBean = new ZSetSaveBean();

        long current =userPostQueryRequest.getCurrent();
        long size = userPostQueryRequest.getPageSize();

        ZSetQueryBean.setSortedKey(sortedKey);
        ZSetQueryBean.setPage(current);
        ZSetQueryBean.setSize(size);
        ZSetQueryBean.setVOClass(UserPost.class);
        ZSetQueryBean.setSortedTotalKey(sortedTotalKey);
        ZSetQueryBean.setOrder(order);
        ZSetQueryBean.setValueKeyHead(UserCacheConstant.USER_POST_KEY_PREFIX);

        zSetSaveBean.setValueKeyHead(UserCacheConstant.USER_POST_KEY_PREFIX);
        zSetSaveBean.setSortedKey(sortedKey);
        zSetSaveBean.setSortedTotalKey(sortedTotalKey);

        return ZSetPageCacheTemplate.zSetQuery(userPostQueryRequest, ZSetQueryBean, zSetSaveBean,
                () -> userPostRepository.page(new Page<>(current, size), getQueryWrapper(userPostQueryRequest)),
                this::EntityToVO
                );
    }

    private List<UserPostVO> EntityToVO(List<UserPost> userPostList){
        List<UserPostVO> userPostVOList = new ArrayList<>();
        for (UserPost userPost : userPostList) {
            UserPostVO userPostVO = new UserPostVO();
            BeanUtils.copyProperties(userPost, userPostVO);
            if(UserPostEnum.SCREENPLAY.getValue().equals(userPost.getPostType())){
                ScreenplayVO screenplayVO = graphFeignClient.getScreenplayById(userPost.getQuotedId());
                userPostVO.setScreenplayVO(screenplayVO);
            }
            userPostVOList.add(userPostVO);
        }
        return userPostVOList;
    }

    private QueryWrapper<UserPost> getQueryWrapper(UserPostQueryRequest userPostQueryRequest){
        QueryWrapper<UserPost> queryWrapper = new QueryWrapper<>();
        if(userPostQueryRequest == null){
            return queryWrapper;
        }
        List<Long> userIdList = userPostQueryRequest.getUserIdList();
        List<Long> postIdList = userPostQueryRequest.getPostIdList();
        queryWrapper.in(ObjUtil.isNotEmpty(userIdList), "userId", userIdList);
        queryWrapper.in(ObjUtil.isNotEmpty(userIdList), "id", postIdList);
        String sortField = userPostQueryRequest.getSortField();
        String sortOrder = userPostQueryRequest.getSortOrder();
        queryWrapper.orderBy(StrUtil.isNotEmpty(sortField), sortOrder.equals("ascend"), sortField);
        return queryWrapper;
    }
}
