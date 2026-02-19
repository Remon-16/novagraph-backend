package com.tech.novagraphbackenduserservice.domain.user.service.impl;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tech.novagraphbackendcommon.cache.CacheManager;
import com.tech.novagraphbackendcommon.cache.ZSetPageCacheTemplate;
import com.tech.novagraphbackendcommon.cache.bean.ZSetQueryBean;
import com.tech.novagraphbackendcommon.cache.bean.ZSetSaveBean;
import com.tech.novagraphbackendcommon.common.CanalHandleVO;

import com.tech.novagraphbackendcommon.exception.ErrorCode;
import com.tech.novagraphbackendcommon.exception.ThrowUtils;

import com.tech.novagraphbackendcommon.utils.CacheUtils;

import com.tech.novagraphbackendmodel.dto.user.UserPostCommentQueryRequest;
import com.tech.novagraphbackendmodel.dto.user.UserPostRequest;

import com.tech.novagraphbackendmodel.user.constant.UserCacheConstant;
import com.tech.novagraphbackendmodel.user.entity.User;
import com.tech.novagraphbackendmodel.user.entity.UserPostComment;

import com.tech.novagraphbackendmodel.vo.user.UserListVO;
import com.tech.novagraphbackendmodel.vo.user.UserPostZSetRootVO;
import com.tech.novagraphbackendmodel.vo.user.UserPostZSetVO;
import com.tech.novagraphbackendmodel.vo.user.UserVO;
import com.tech.novagraphbackendserviceclient.UserFeignClient;
import com.tech.novagraphbackenduserservice.domain.user.repository.UserPostCommentRepository;
import com.tech.novagraphbackenduserservice.domain.user.service.UserPostCommentDomainService;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserPostCommentDomainServiceImpl implements UserPostCommentDomainService {

    @Resource
    private CacheManager cacheManager;

    @Resource
    private UserFeignClient userFeignClient;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private ZSetPageCacheTemplate ZSetPageCacheTemplate;

    @Resource
    private UserPostCommentRepository postCommentRepository;

    @Override
    public UserPostComment saveOrUpdateUserPostComment(UserPostRequest userPostRequest) {
        ThrowUtils.throwIf(userPostRequest == null,
                ErrorCode.PARAMS_ERROR, "userPostRequest");
        UserPostComment userPostComment = new UserPostComment();
        BeanUtils.copyProperties(userPostRequest, userPostComment);
        if (userPostComment.getId() == null) {
            postCommentRepository.save(userPostComment);
        }else {
            postCommentRepository.updateById(userPostComment);
        }
        return userPostComment;
    }

    @Override
    public Boolean deleteUserPostComment(UserPostRequest userPostRequest) {
        return postCommentRepository.removeById(userPostRequest.getId());
    }

    @Override
    public QueryWrapper<UserPostComment> getQueryWrapper(UserPostCommentQueryRequest userPostCommentQueryRequest) {
        QueryWrapper<UserPostComment> queryWrapper = new QueryWrapper<>();
        if (userPostCommentQueryRequest == null) {
            return queryWrapper;
        }
        Long postId = userPostCommentQueryRequest.getPostId();
        Long targetId = userPostCommentQueryRequest.getTargetId();
        String sortField = userPostCommentQueryRequest.getSortField();
        String sortOrder = userPostCommentQueryRequest.getSortOrder();
        queryWrapper.eq(ObjUtil.isNotEmpty(postId), "postId", postId);
        if(ObjUtil.isNotEmpty(targetId)){
            queryWrapper.eq("targetId", targetId);
        }else{
            queryWrapper.isNull("targetId");
        }
        queryWrapper.orderBy(StrUtil.isNotEmpty(sortField), sortOrder.equals("ascend"), sortField);
        return queryWrapper;
    }

    @Override
    public Page<UserPostZSetRootVO> getUserPostCommentRootVo(UserPostCommentQueryRequest userPostCommentQueryRequest) {
        ThrowUtils.throwIf(userPostCommentQueryRequest == null,
                ErrorCode.PARAMS_ERROR, "userPostCommentQueryRequest");
        ThrowUtils.throwIf(userPostCommentQueryRequest.getPostId() == null,
                ErrorCode.PARAMS_ERROR, "postId为空");
        long current = userPostCommentQueryRequest.getCurrent();
        long size = userPostCommentQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 30, ErrorCode.PARAMS_ERROR);
        // 先查直接评论在剧本上的 targetId == null
        userPostCommentQueryRequest.setTargetId(null);
        Page<UserPostZSetVO> userPostCommentVoPage = queryUserPostCommentVo(userPostCommentQueryRequest, current, size);
        // 接着通过评论的id，去查前10个子评论
        List<UserPostZSetRootVO> userPostCommentRootVOList = new ArrayList<>();
        for(UserPostZSetVO userPostCommentVo : userPostCommentVoPage.getRecords()){
            UserPostCommentQueryRequest userPostCommentQueryRequest1 = new UserPostCommentQueryRequest();
            userPostCommentQueryRequest1.setCurrent(1);
            userPostCommentQueryRequest1.setPageSize(10);
            userPostCommentQueryRequest1.setTargetId(userPostCommentVo.getId());
            userPostCommentQueryRequest1.setPostId(userPostCommentVo.getPostId());
            userPostCommentQueryRequest1.setSortOrder(userPostCommentQueryRequest.getSortOrder());
            Page<UserPostZSetVO> userPostCommentVoPage2 = null;

            userPostCommentVoPage2 = this.getUserPostCommentVo(userPostCommentQueryRequest1);
            UserPostZSetRootVO userPostCommentRootVo = new UserPostZSetRootVO();
            BeanUtils.copyProperties(userPostCommentVo, userPostCommentRootVo);
            userPostCommentRootVo.setUserPostCommentVOPage(userPostCommentVoPage2);
            userPostCommentRootVOList.add(userPostCommentRootVo);
        }

        Page<UserPostZSetRootVO> userPostCommentRootVoPage = new Page<>();
        userPostCommentRootVoPage.setRecords(userPostCommentRootVOList);
        userPostCommentRootVoPage.setCurrent(userPostCommentVoPage.getCurrent());
        userPostCommentRootVoPage.setTotal(userPostCommentVoPage.getTotal());
        userPostCommentRootVoPage.setSize(userPostCommentVoPage.getSize());
        return userPostCommentRootVoPage;
    }

    @Override
    public Page<UserPostZSetVO> getUserPostCommentVo(UserPostCommentQueryRequest userPostCommentQueryRequest) {
        ThrowUtils.throwIf(userPostCommentQueryRequest == null,
                ErrorCode.PARAMS_ERROR, "userPostCommentQueryRequest");
        ThrowUtils.throwIf(userPostCommentQueryRequest.getPostId() == null,
                ErrorCode.PARAMS_ERROR, "postId为空");
        ThrowUtils.throwIf(userPostCommentQueryRequest.getTargetId() == null,
                ErrorCode.PARAMS_ERROR, "targetId为空");
        long current = userPostCommentQueryRequest.getCurrent();
        long size = userPostCommentQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 30, ErrorCode.PARAMS_ERROR);
        return queryUserPostCommentVo(userPostCommentQueryRequest, current, size);
    }

    @Override
    public void canalHandleUserPostComment(List<CanalHandleVO> canalHandleVOList) {
        canalHandleVOList.forEach(canalHandleVO -> {
            CanalEntry.EventType eventType = canalHandleVO.getEventType();
            UserPostComment userPostComment = JSONUtil.toBean(canalHandleVO.getJsonDataStr(), UserPostComment.class);
            if (eventType == CanalEntry.EventType.DELETE || cacheManager.getEntry_DELETE_FLAG().equals(userPostComment.getIsDelete())) {
                canalDeleteHandle(canalHandleVO);
            }else if (eventType == CanalEntry.EventType.UPDATE) {
                canalUpdateHandle(canalHandleVO);
            }else {
                canalInsertHandle(canalHandleVO);
            }
        });
    }

    private void canalInsertHandle(CanalHandleVO canalHandleVO){
        UserPostComment userPostComment = JSONUtil.toBean(canalHandleVO.getJsonDataStr(), UserPostComment.class);
        String ascSortedKey = getSortedKey(CacheUtils.ASC, userPostComment.getPostId(), userPostComment.getTargetId());
        String descSortedKey = getSortedKey(CacheUtils.DESC, userPostComment.getPostId(), userPostComment.getTargetId());
        String totalKey = getSortedTotalKey(userPostComment.getPostId(), userPostComment.getTargetId());

        // 更新缓存
        Double score = (double) userPostComment.getCreateTime().getTime();
        String valueKey = UserCacheConstant.getUserPostCommentCacheKey(userPostComment.getId());
        List<UserPostComment> userPostCommentList = new ArrayList<>();
        userPostCommentList.add(userPostComment);
        List<UserPostZSetVO> userPostCommentVOList = this.getUserPostCommentVo(userPostCommentList);
        String valueStr = JSONUtil.toJsonStr(userPostCommentVOList.getFirst());
        // 更新 total
        Long total = cacheManager.getTotal(totalKey);
        if(total == null){
            return;
        }

        cacheManager.putValueToCache(totalKey, total + 1, cacheManager.getRedisZSetExpireTime());
        cacheManager.insertSortedValue(ascSortedKey, userPostComment.getId(), score, valueKey, valueStr);
        cacheManager.insertSortedValue(descSortedKey, userPostComment.getId(), score, valueKey, valueStr);
    }

    private void canalUpdateHandle(CanalHandleVO canalHandleVO){
        UserPostComment userPostComment = JSONUtil.toBean(canalHandleVO.getJsonDataStr(), UserPostComment.class);
        String valueKey = UserCacheConstant.getUserPostCommentCacheKey(userPostComment.getId());
        if(!redisTemplate.hasKey(UserCacheConstant.buildRedisKey(valueKey))){
            return;
        }
        Object Value = cacheManager.getValueCache(valueKey);
        UserPostZSetVO userPostCommentVo = JSONUtil.toBean((String) Value, UserPostZSetVO.class);
        userPostCommentVo.setContent(userPostComment.getContent());
        cacheManager.putValueToCache(valueKey, JSONUtil.toJsonStr(userPostCommentVo), cacheManager.getRedisZSetExpireTime());
    }

    private void canalDeleteHandle(CanalHandleVO canalHandleVO){
        UserPostComment userPostComment = JSONUtil.toBean(canalHandleVO.getJsonDataStr(), UserPostComment.class);
        String ascSortedKey = getSortedKey(CacheUtils.ASC, userPostComment.getPostId(), userPostComment.getTargetId());
        String descSortedKey = getSortedKey(CacheUtils.DESC, userPostComment.getPostId(), userPostComment.getTargetId());
        String valueKey = UserCacheConstant.getUserPostCommentCacheKey(userPostComment.getId());

        cacheManager.zSetRemove(ascSortedKey, userPostComment.getId());
        cacheManager.zSetRemove(descSortedKey, userPostComment.getId());
        cacheManager.removeValueCache(valueKey);

        String totalKey = getSortedTotalKey(userPostComment.getPostId(), userPostComment.getTargetId());
        // 更新 total
        Long total = cacheManager.getTotal(totalKey);
        if(total == null){
            return;
        }

        cacheManager.putValueToCache(totalKey, total - 1, cacheManager.getRedisZSetExpireTime());
    }

    @Override
    public UserPostComment getById(Long postCommentId) {
        return postCommentRepository.getById(postCommentId);
    }

    private Page<UserPostZSetVO> queryUserPostCommentVo(UserPostCommentQueryRequest userPostCommentQueryRequest,
                                                        Long current, Long size) {
        String order = userPostCommentQueryRequest.getSortOrder();
        Long postId = userPostCommentQueryRequest.getPostId();
        Long targetId = userPostCommentQueryRequest.getTargetId();
        String sortedKey = getSortedKey(order, postId, targetId);
        String sortedTotalKey = getSortedTotalKey(postId, targetId);

        ZSetQueryBean ZSetQueryBean = new ZSetQueryBean();
        ZSetSaveBean zsetSaveBean = new ZSetSaveBean();

        ZSetQueryBean.setSortedKey(sortedKey);
        ZSetQueryBean.setPage(current);
        ZSetQueryBean.setSize(size);
        ZSetQueryBean.setVOClass(UserPostZSetVO.class);
        ZSetQueryBean.setSortedTotalKey(sortedTotalKey);
        ZSetQueryBean.setOrder(order);
        ZSetQueryBean.setValueKeyHead(UserCacheConstant.USER_POST_COMMENT_CACHE_PREFIX);

        zsetSaveBean.setValueKeyHead(UserCacheConstant.USER_POST_COMMENT_CACHE_PREFIX);
        zsetSaveBean.setSortedKey(sortedKey);
        zsetSaveBean.setSortedTotalKey(sortedTotalKey);
        return ZSetPageCacheTemplate.zSetQuery(userPostCommentQueryRequest, ZSetQueryBean, zsetSaveBean,
                () -> postCommentRepository.page(new Page<>(current, size), this.getQueryWrapper(userPostCommentQueryRequest)),
                this::getUserPostCommentVo
        );
    }

    private String getSortedKey(String order, Long postId, Long targetId){
        if(targetId == null){
            return UserCacheConstant.getUserPostCommentSortedCacheKey(order, postId);
        }else{
            return UserCacheConstant.getUserPostSecondCommentSortedCacheKey(order, targetId);
        }
    }

    private String getSortedTotalKey(Long postId, Long targetId){
        if(targetId == null){
            return UserCacheConstant.getUserPostCommentSortedTotalCacheKey(postId);
        }else{
            return UserCacheConstant.getUserPostSecondCommentSortedTotalCacheKey(targetId);
        }
    }

    private List<UserPostZSetVO> getUserPostCommentVo(List<UserPostComment> userPostCommentList){
        if(userPostCommentList == null || userPostCommentList.isEmpty()){
            return new ArrayList<>();
        }
        Set<Long> userIdSet = userPostCommentList.stream().map(UserPostComment::getUserId).collect(Collectors.toSet());
        UserListVO userListVO = userFeignClient.listByIds(userIdSet);
        List<User> userList = userListVO.getUserList(userListVO.getUserListJson());
        Map<Long, List<User>> userIdUserListMap = userList.stream()
                .collect(Collectors.groupingBy(User::getId));
        List<UserPostZSetVO> userPostCommentVOList = userPostCommentList.stream().map(UserPostZSetVO::objToVo).toList();

        Map<Long, UserVO> commentUserMap = new HashMap<>();
        userPostCommentVOList.forEach(userPostCommentVO -> {
            Long userId = userPostCommentVO.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).getFirst();
            }
            userPostCommentVO.setUser(userFeignClient.getUserVO(user));
            commentUserMap.put(userPostCommentVO.getId(), userPostCommentVO.getUser());
        });

        // set target的用户相关信息
        userPostCommentVOList.forEach(userPostCommentVO -> {
            Long targetId = userPostCommentVO.getTargetId();
            Long secondTargetId = userPostCommentVO.getSecondTargetId();
            if(targetId != null && secondTargetId == null){
                this.getCommentUserInfo(userPostCommentVO, commentUserMap, targetId);
            }
            if (targetId != null && secondTargetId != null) {
                this.getCommentUserInfo(userPostCommentVO, commentUserMap, secondTargetId);
            }
        });

        return userPostCommentVOList;
    }

    private void getCommentUserInfo(UserPostZSetVO userPostCommentVo, Map<Long, UserVO> commentUserMap, Long targetId) {
        // 先查这个集合里有没有，如果没有再去数据库查
        if(commentUserMap.containsKey(targetId)){
            UserVO user = commentUserMap.get(targetId);
            userPostCommentVo.setTargetUserId(user.getId());
            userPostCommentVo.setTargetUserName(user.getUserName());
        }else {
            UserPostComment userPostComment = postCommentRepository.getById(targetId);
            User targetUser = userFeignClient.getUserById(userPostComment.getUserId());
            userPostCommentVo.setTargetUserId(targetUser.getId());
            userPostCommentVo.setTargetUserName(targetUser.getUserName());
            commentUserMap.put(targetId, userFeignClient.getUserVO(targetUser));
        }
    }
}
