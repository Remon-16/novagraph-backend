package com.tech.novagraphbackendgraphservice.domain.screenplay.service.impl;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tech.novagraphbackendcommon.cache.CacheManager;
import com.tech.novagraphbackendcommon.common.CanalHandleVO;
import com.tech.novagraphbackendcommon.common.SortedCacheResult;
import com.tech.novagraphbackendcommon.exception.BusinessException;
import com.tech.novagraphbackendcommon.exception.ErrorCode;
import com.tech.novagraphbackendcommon.exception.ThrowUtils;
import com.tech.novagraphbackendcommon.utils.CacheUtils;
import com.tech.novagraphbackendgraphservice.domain.screenplay.service.ScreenplayCommentDomainService;
import com.tech.novagraphbackendgraphservice.infrastructure.mapper.ScreenplayCommentMapper;
import com.tech.novagraphbackendmodel.dto.graph.ScreenplayCommentQueryRequest;
import com.tech.novagraphbackendmodel.dto.graph.ScreenplayCommentRequest;
import com.tech.novagraphbackendmodel.graph.constant.ScreenplayCacheConstant;
import com.tech.novagraphbackendmodel.graph.entity.ScreenplayComment;
import com.tech.novagraphbackendmodel.user.entity.User;
import com.tech.novagraphbackendmodel.vo.graph.ScreenplayCommentRootVO;
import com.tech.novagraphbackendmodel.vo.graph.ScreenplayCommentVO;
import com.tech.novagraphbackendmodel.vo.user.UserListVO;
import com.tech.novagraphbackendmodel.vo.user.UserVO;
import com.tech.novagraphbackendserviceclient.UserFeignClient;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class ScreenplayCommentDomainServiceImpl extends ServiceImpl<ScreenplayCommentMapper, ScreenplayComment>
        implements ScreenplayCommentDomainService {

    @Resource
    private CacheManager cacheManager;

    @Resource
    private UserFeignClient userFeignClient;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    private final Map<String, Object> lockMap = new ConcurrentHashMap<>();

    @Override
    public ScreenplayComment saveOrUpdateScreenplayComment(ScreenplayCommentRequest screenplayCommentRequest) {
        ThrowUtils.throwIf(screenplayCommentRequest == null,
                ErrorCode.PARAMS_ERROR, "screenplayCommentRequest为空");
        ScreenplayComment screenplayComment = new ScreenplayComment();
        BeanUtils.copyProperties(screenplayCommentRequest, screenplayComment);
        if (screenplayComment.getId() == null) {
            this.save(screenplayComment);
        }else {
            this.updateById(screenplayComment);
        }
        return screenplayComment;
    }

    @Override
    public Boolean deleteScreenplayComment(ScreenplayCommentRequest screenplayCommentRequest) {
        return this.removeById(screenplayCommentRequest.getId());
    }

    @Override
    public QueryWrapper<ScreenplayComment> getQueryWrapper(ScreenplayCommentQueryRequest screenplayCommentQueryRequest) {
        QueryWrapper<ScreenplayComment> queryWrapper = new QueryWrapper<>();
        if (screenplayCommentQueryRequest == null) {
            return queryWrapper;
        }
        Long screenplayId = screenplayCommentQueryRequest.getScreenplayId();
        Long targetId = screenplayCommentQueryRequest.getTargetId();
        String sortField = screenplayCommentQueryRequest.getSortField();
        String sortOrder = screenplayCommentQueryRequest.getSortOrder();
        queryWrapper.eq(ObjUtil.isNotEmpty(screenplayId), "screenplayId", screenplayId);
        if(ObjUtil.isNotEmpty(targetId)){
            queryWrapper.eq("targetId", targetId);
        }else{
            queryWrapper.isNull("targetId");
        }
        queryWrapper.orderBy(StrUtil.isNotEmpty(sortField), sortOrder.equals("ascend"), sortField);
        return queryWrapper;
    }

    @Override
    public Page<ScreenplayCommentRootVO> getScreenplayCommentRootVo(ScreenplayCommentQueryRequest screenplayCommentQueryRequest) {
        ThrowUtils.throwIf(screenplayCommentQueryRequest == null,
                ErrorCode.PARAMS_ERROR, "screenplayCommentQueryRequest为空");
        ThrowUtils.throwIf(screenplayCommentQueryRequest.getScreenplayId() == null,
                ErrorCode.PARAMS_ERROR, "screenplayId为空");
        long current = screenplayCommentQueryRequest.getCurrent();
        long size = screenplayCommentQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 30, ErrorCode.PARAMS_ERROR);
        // 先查直接评论在剧本上的 targetId == null
        screenplayCommentQueryRequest.setTargetId(null);
        Page<ScreenplayCommentVO> screenplayCommentVoPage = queryScreenplayCommentVo(screenplayCommentQueryRequest, current, size);
        // 接着通过评论的id，去查前10个子评论
        List<ScreenplayCommentRootVO> screenplayCommentRootVOList = new ArrayList<>();
        for(ScreenplayCommentVO screenplayCommentVo : screenplayCommentVoPage.getRecords()){
            ScreenplayCommentQueryRequest screenplayCommentQueryRequest1 = new ScreenplayCommentQueryRequest();
            screenplayCommentQueryRequest1.setCurrent(1);
            screenplayCommentQueryRequest1.setPageSize(10);
            screenplayCommentQueryRequest1.setTargetId(screenplayCommentVo.getId());
            screenplayCommentQueryRequest1.setScreenplayId(screenplayCommentVo.getScreenplayId());
            screenplayCommentQueryRequest1.setSortOrder(screenplayCommentQueryRequest.getSortOrder());
            Page<ScreenplayCommentVO> screenplayCommentVoPage2 = null;

            screenplayCommentVoPage2 = this.getScreenplayCommentVo(screenplayCommentQueryRequest1);
            ScreenplayCommentRootVO screenplayCommentRootVo = new ScreenplayCommentRootVO();
            BeanUtils.copyProperties(screenplayCommentVo, screenplayCommentRootVo);
            screenplayCommentRootVo.setScreenplayCommentVoPage(screenplayCommentVoPage2);
            screenplayCommentRootVOList.add(screenplayCommentRootVo);
        }

        Page<ScreenplayCommentRootVO> screenplayCommentRootVoPage = new Page<>();
        screenplayCommentRootVoPage.setRecords(screenplayCommentRootVOList);
        screenplayCommentRootVoPage.setCurrent(screenplayCommentVoPage.getCurrent());
        screenplayCommentRootVoPage.setTotal(screenplayCommentVoPage.getTotal());
        screenplayCommentRootVoPage.setSize(screenplayCommentVoPage.getSize());
        return screenplayCommentRootVoPage;
    }

    @Override
    public Page<ScreenplayCommentVO> getScreenplayCommentVo(ScreenplayCommentQueryRequest screenplayCommentQueryRequest) {
        ThrowUtils.throwIf(screenplayCommentQueryRequest == null,
                ErrorCode.PARAMS_ERROR, "screenplayCommentQueryRequest为空");
        ThrowUtils.throwIf(screenplayCommentQueryRequest.getScreenplayId() == null,
                ErrorCode.PARAMS_ERROR, "screenplayId为空");
        ThrowUtils.throwIf(screenplayCommentQueryRequest.getTargetId() == null,
                ErrorCode.PARAMS_ERROR, "targetId为空");
        long current = screenplayCommentQueryRequest.getCurrent();
        long size = screenplayCommentQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 30, ErrorCode.PARAMS_ERROR);
        return queryScreenplayCommentVo(screenplayCommentQueryRequest, current, size);
    }

    @Override
    public ScreenplayComment getById(Long ScreenplayCommentId) {
        return super.getById(ScreenplayCommentId);
    }

    @Override
    public void canalHandleScreenplayComment(List<CanalHandleVO> canalHandleVOList) {
        canalHandleVOList.forEach(canalHandleVO -> {
            CanalEntry.EventType eventType = canalHandleVO.getEventType();
            ScreenplayComment screenplayComment = JSONUtil.toBean(canalHandleVO.getJsonDataStr(), ScreenplayComment.class);
            if (eventType == CanalEntry.EventType.DELETE || cacheManager.getEntry_DELETE_FLAG().equals(screenplayComment.getIsDelete())) {
                canalDeleteHandle(canalHandleVO);
            }else if (eventType == CanalEntry.EventType.UPDATE) {
                canalUpdateHandle(canalHandleVO);
            }else {
                canalInsertHandle(canalHandleVO);
            }
        });
    }

    private void canalInsertHandle(CanalHandleVO canalHandleVO){
        ScreenplayComment screenplayComment = JSONUtil.toBean(canalHandleVO.getJsonDataStr(), ScreenplayComment.class);
        String ascSortedKey = getSortedKey(CacheUtils.ASC, screenplayComment.getScreenplayId(), screenplayComment.getTargetId());
        String descSortedKey = getSortedKey(CacheUtils.DESC, screenplayComment.getScreenplayId(), screenplayComment.getTargetId());
        String totalKey = getSortedTotalKey(screenplayComment.getScreenplayId(), screenplayComment.getTargetId());

        // 更新缓存
        Double score = (double) screenplayComment.getCreateTime().getTime();
        String valueKey = ScreenplayCacheConstant.getScreenplayCommentCacheKey(screenplayComment.getId().toString());
        List<ScreenplayComment> screenplayCommentList = new ArrayList<>();
        screenplayCommentList.add(screenplayComment);
        List<ScreenplayCommentVO> screenplayCommentVOList = this.getScreenplayCommentVo(screenplayCommentList);
        String valueStr = JSONUtil.toJsonStr(screenplayCommentVOList.getFirst());
        // 更新 total
        Long total = cacheManager.getTotal(totalKey);
        if(total == null){
            return;
        }

        cacheManager.putValueToCache(totalKey, total + 1, cacheManager.getRedisZSetExpireTime());
        cacheManager.insertSortedValue(ascSortedKey, screenplayComment.getId(), score, valueKey, valueStr);
        cacheManager.insertSortedValue(descSortedKey, screenplayComment.getId(), score, valueKey, valueStr);
    }

    private void canalUpdateHandle(CanalHandleVO canalHandleVO){
        ScreenplayComment screenplayComment = JSONUtil.toBean(canalHandleVO.getJsonDataStr(), ScreenplayComment.class);
        String valueKey = ScreenplayCacheConstant.getScreenplayCommentCacheKey(screenplayComment.getId().toString());
        if(!redisTemplate.hasKey(ScreenplayCacheConstant.buildRedisKey(valueKey))){
            return;
        }
        Object Value = cacheManager.getValueCache(valueKey);
        ScreenplayCommentVO screenplayCommentVo = JSONUtil.toBean((String) Value, ScreenplayCommentVO.class);
        screenplayCommentVo.setContent(screenplayComment.getContent());
        cacheManager.putValueToCache(valueKey, JSONUtil.toJsonStr(screenplayCommentVo), cacheManager.getRedisZSetExpireTime());
    }

    private void canalDeleteHandle(CanalHandleVO canalHandleVO){
        ScreenplayComment screenplayComment = JSONUtil.toBean(canalHandleVO.getJsonDataStr(), ScreenplayComment.class);
        String ascSortedKey = getSortedKey(CacheUtils.ASC, screenplayComment.getScreenplayId(), screenplayComment.getTargetId());
        String descSortedKey = getSortedKey(CacheUtils.DESC, screenplayComment.getScreenplayId(), screenplayComment.getTargetId());
        String valueKey = ScreenplayCacheConstant.getScreenplayCommentCacheKey(screenplayComment.getId().toString());

        cacheManager.zSetRemove(ascSortedKey, screenplayComment.getId());
        cacheManager.zSetRemove(descSortedKey, screenplayComment.getId());
        cacheManager.removeValueCache(valueKey);

        String totalKey = getSortedTotalKey(screenplayComment.getScreenplayId(), screenplayComment.getTargetId());
        // 更新 total
        Long total = cacheManager.getTotal(totalKey);
        if(total == null){
            return;
        }

        cacheManager.putValueToCache(totalKey, total - 1, cacheManager.getRedisZSetExpireTime());
    }

    private Page<ScreenplayCommentVO> queryScreenplayCommentVo(ScreenplayCommentQueryRequest screenplayCommentQueryRequest,
                                                               Long current, Long size) {
        String order = screenplayCommentQueryRequest.getSortOrder();
        Long screenplayId = screenplayCommentQueryRequest.getScreenplayId();
        Long targetId = screenplayCommentQueryRequest.getTargetId();
        String sortedKey = getSortedKey(order, screenplayId, targetId);
        String sortedTotalKey = getSortedTotalKey(screenplayId, targetId);
        // 1 先查评论的缓存
        Page<ScreenplayCommentVO> screenplayCommentVoPage = queryCache(sortedKey, sortedTotalKey,
                ScreenplayCacheConstant.SCREENPLAY_COMMENT_CACHE_PREFIX, order, current, size);
        if(screenplayCommentVoPage == null){
            String lockStr = CacheUtils.getHexLockString(screenplayCommentQueryRequest);
            Object lock = lockMap.computeIfAbsent(lockStr, key -> new Object());
            synchronized (lock) {
                // 2 再次查询缓存
                screenplayCommentVoPage = queryCache(sortedKey, sortedTotalKey,
                        ScreenplayCacheConstant.SCREENPLAY_COMMENT_CACHE_PREFIX, order, current, size);
                if(screenplayCommentVoPage == null){
                    try {
                        // 3 缓存没中，查数据库
                        Page<ScreenplayComment> screenplayCommentPage = page(new Page<>(current, size),
                                getQueryWrapper(screenplayCommentQueryRequest));
                        List<ScreenplayComment> screenplayCommentList = screenplayCommentPage.getRecords();
                        List<ScreenplayCommentVO> screenplayCommentVOList = this.getScreenplayCommentVo(screenplayCommentList);
                        screenplayCommentVoPage = new Page<>();
                        screenplayCommentVoPage.setRecords(screenplayCommentVOList);
                        screenplayCommentVoPage.setCurrent(screenplayCommentPage.getCurrent());
                        screenplayCommentVoPage.setSize(size);
                        screenplayCommentVoPage.setTotal(screenplayCommentPage.getTotal());
                        // 4 写入缓存
                        screenplayCommentVOList.forEach(screenplayCommentVO -> {
                            Double score = (double) screenplayCommentVO.getCreateTime().getTime();
                            cacheManager.zSetAdd(sortedKey, screenplayCommentVO.getId(), score);
                            cacheManager.putValueToCache(ScreenplayCacheConstant.getScreenplayCommentCacheKey(screenplayCommentVO.getId().toString()),
                                    JSONUtil.toJsonStr(screenplayCommentVO), cacheManager.getRedisZSetExpireTime());
                        });
                        cacheManager.putValueToCache(sortedTotalKey, screenplayCommentPage.getTotal(), cacheManager.getRedisZSetExpireTime());
                    } catch (Exception e) {
                        throw new BusinessException(ErrorCode.PARAMS_ERROR);
                    } finally {
                        // 防止内存泄漏
                        lockMap.remove(lockStr);
                    }
                }else {
                    // 防止内存泄漏
                    lockMap.remove(lockStr);
                }
            }
        }
        return screenplayCommentVoPage;
    }

    private String getSortedKey(String order, Long screenplayId, Long targetId){
        if(targetId == null){
            return ScreenplayCacheConstant.getScreenplaySortedCommentCacheKey(order, screenplayId);
        }else{
            return ScreenplayCacheConstant.getScreenplaySecondCommentSortedCache(order, targetId);
        }
    }

    private String getSortedTotalKey(Long screenplayId, Long targetId){
        if(targetId == null){
            return ScreenplayCacheConstant.getScreenplayCommentSortedTotalCache(screenplayId);
        }else{
            return ScreenplayCacheConstant.getScreenplaySecondCommentSortedTotalCache(targetId);
        }
    }

    private Page<ScreenplayCommentVO> queryCache(String sortedKey, String sortedTotalKey, String keyHead, String order, Long page, Long size){
        SortedCacheResult sortedCacheResult = cacheManager.querySortedValues(sortedKey, sortedTotalKey, keyHead, order, page, size);
        if(sortedCacheResult == null){
            return null;
        }
        Map<Object, Object> queryValueMap = sortedCacheResult.getValueMap();
        List<ScreenplayCommentVO> screenplayCommentVOList = new ArrayList<>();
        for(Object value : queryValueMap.values()){
            ScreenplayCommentVO screenplayCommentVo = JSONUtil.toBean((String)value, ScreenplayCommentVO.class);
            screenplayCommentVOList.add(screenplayCommentVo);
        }
        Page<ScreenplayCommentVO> screenplayCommentVoPage = new Page<>();
        screenplayCommentVoPage.setRecords(screenplayCommentVOList);
        screenplayCommentVoPage.setCurrent(page);
        screenplayCommentVoPage.setSize(size);
        screenplayCommentVoPage.setTotal(sortedCacheResult.getTotal());
        return screenplayCommentVoPage;
    }

    private void getCommentUserInfo(ScreenplayCommentVO screenplayCommentVo, Map<Long, UserVO> commentUserMap, Long targetId) {
        // 先查这个集合里有没有，如果没有再去数据库查
        if(commentUserMap.containsKey(targetId)){
            UserVO user = commentUserMap.get(targetId);
            screenplayCommentVo.setTargetUserId(user.getId());
            screenplayCommentVo.setTargetUserName(user.getUserName());
        }else {
            ScreenplayComment screenplayComment = this.getById(targetId);
            User targetUser = userFeignClient.getUserById(screenplayComment.getUserId());
            screenplayCommentVo.setTargetUserId(targetUser.getId());
            screenplayCommentVo.setTargetUserName(targetUser.getUserName());
            commentUserMap.put(targetId, userFeignClient.getUserVO(targetUser));
        }
    }

    private List<ScreenplayCommentVO> getScreenplayCommentVo(List<ScreenplayComment> screenplayCommentList){
        if(screenplayCommentList == null || screenplayCommentList.isEmpty()){
            return new ArrayList<>();
        }
        Set<Long> userIdSet = screenplayCommentList.stream().map(ScreenplayComment::getUserId).collect(Collectors.toSet());
        UserListVO userListVO = userFeignClient.listByIds(userIdSet);
        List<User> userList = userListVO.getUserList(userListVO.getUserListJson());
        Map<Long, List<User>> userIdUserListMap = userList.stream()
                .collect(Collectors.groupingBy(User::getId));
        List<ScreenplayCommentVO> screenplayCommentVOList = screenplayCommentList.stream().map(ScreenplayCommentVO::objToVo).toList();

        Map<Long, UserVO> commentUserMap = new HashMap<>();
        screenplayCommentVOList.forEach(screenplayCommentVO -> {
            Long userId = screenplayCommentVO.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).getFirst();
            }
            screenplayCommentVO.setUser(userFeignClient.getUserVO(user));
            commentUserMap.put(screenplayCommentVO.getId(), screenplayCommentVO.getUser());
        });

        // set target的用户相关信息
        screenplayCommentVOList.forEach(screenplayCommentVO -> {
            Long targetId = screenplayCommentVO.getTargetId();
            Long secondTargetId = screenplayCommentVO.getSecondTargetId();
            if(targetId != null && secondTargetId == null){
                this.getCommentUserInfo(screenplayCommentVO, commentUserMap, targetId);
            }
            if (targetId != null && secondTargetId != null) {
                this.getCommentUserInfo(screenplayCommentVO, commentUserMap, secondTargetId);
            }
        });

        return screenplayCommentVOList;
    }
}
