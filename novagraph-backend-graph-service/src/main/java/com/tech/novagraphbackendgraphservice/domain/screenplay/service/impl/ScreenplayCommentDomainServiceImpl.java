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
import com.tech.novagraphbackendmodel.vo.graph.ScreenplayCommentRootVo;
import com.tech.novagraphbackendmodel.vo.graph.ScreenplayCommentVo;
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
    public Page<ScreenplayCommentRootVo> getScreenplayCommentRootVo(ScreenplayCommentQueryRequest screenplayCommentQueryRequest) {
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
        Page<ScreenplayCommentVo> screenplayCommentVoPage = queryScreenplayCommentVo(screenplayCommentQueryRequest, current, size);
        // 接着通过评论的id，去查前10个子评论
        List<ScreenplayCommentRootVo> screenplayCommentRootVoList = new ArrayList<>();
        for(ScreenplayCommentVo screenplayCommentVo : screenplayCommentVoPage.getRecords()){
            ScreenplayCommentQueryRequest screenplayCommentQueryRequest1 = new ScreenplayCommentQueryRequest();
            screenplayCommentQueryRequest1.setCurrent(1);
            screenplayCommentQueryRequest1.setPageSize(10);
            screenplayCommentQueryRequest1.setTargetId(screenplayCommentVo.getId());
            screenplayCommentQueryRequest1.setScreenplayId(screenplayCommentVo.getScreenplayId());
            screenplayCommentQueryRequest1.setSortOrder(screenplayCommentQueryRequest.getSortOrder());
            Page<ScreenplayCommentVo> screenplayCommentVoPage2 = null;

            screenplayCommentVoPage2 = this.getScreenplayCommentVo(screenplayCommentQueryRequest1);
            ScreenplayCommentRootVo screenplayCommentRootVo = new ScreenplayCommentRootVo();
            BeanUtils.copyProperties(screenplayCommentVo, screenplayCommentRootVo);
            screenplayCommentRootVo.setScreenplayCommentVoPage(screenplayCommentVoPage2);
            screenplayCommentRootVoList.add(screenplayCommentRootVo);
        }

        Page<ScreenplayCommentRootVo> screenplayCommentRootVoPage = new Page<>();
        screenplayCommentRootVoPage.setRecords(screenplayCommentRootVoList);
        screenplayCommentRootVoPage.setCurrent(screenplayCommentVoPage.getCurrent());
        screenplayCommentRootVoPage.setTotal(screenplayCommentVoPage.getTotal());
        screenplayCommentRootVoPage.setSize(screenplayCommentVoPage.getSize());
        return screenplayCommentRootVoPage;
    }

    @Override
    public Page<ScreenplayCommentVo> getScreenplayCommentVo(ScreenplayCommentQueryRequest screenplayCommentQueryRequest) {
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
        List<ScreenplayCommentVo> screenplayCommentVoList = this.getScreenplayCommentVo(screenplayCommentList);
        String valueStr = JSONUtil.toJsonStr(screenplayCommentVoList.getFirst());
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
        ScreenplayCommentVo screenplayCommentVo = JSONUtil.toBean((String) Value, ScreenplayCommentVo.class);
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

    private Page<ScreenplayCommentVo> queryScreenplayCommentVo(ScreenplayCommentQueryRequest screenplayCommentQueryRequest,
                                                               Long current, Long size) {
        String order = screenplayCommentQueryRequest.getSortOrder();
        Long screenplayId = screenplayCommentQueryRequest.getScreenplayId();
        Long targetId = screenplayCommentQueryRequest.getTargetId();
        String sortedKey = getSortedKey(order, screenplayId, targetId);
        String sortedTotalKey = getSortedTotalKey(screenplayId, targetId);
        // 1 先查评论的缓存
        Page<ScreenplayCommentVo> screenplayCommentVoPage = queryCache(sortedKey, sortedTotalKey,
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
                        List<ScreenplayCommentVo> screenplayCommentVoList = this.getScreenplayCommentVo(screenplayCommentList);
                        screenplayCommentVoPage = new Page<>();
                        screenplayCommentVoPage.setRecords(screenplayCommentVoList);
                        screenplayCommentVoPage.setCurrent(screenplayCommentPage.getCurrent());
                        screenplayCommentVoPage.setSize(size);
                        screenplayCommentVoPage.setTotal(screenplayCommentPage.getTotal());
                        // 4 写入缓存
                        screenplayCommentVoList.forEach(screenplayCommentVo -> {
                            Double score = (double)screenplayCommentVo.getCreateTime().getTime();
                            cacheManager.zSetAdd(sortedKey, screenplayCommentVo.getId(), score);
                            cacheManager.putValueToCache(ScreenplayCacheConstant.getScreenplayCommentCacheKey(screenplayCommentVo.getId().toString()),
                                    JSONUtil.toJsonStr(screenplayCommentVo), cacheManager.getRedisZSetExpireTime());
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

    private Page<ScreenplayCommentVo> queryCache(String sortedKey, String sortedTotalKey, String keyHead, String order, Long page, Long size){
        SortedCacheResult sortedCacheResult = cacheManager.querySortedValues(sortedKey, sortedTotalKey, keyHead, order, page, size);
        if(sortedCacheResult == null){
            return null;
        }
        Map<Object, Object> queryValueMap = sortedCacheResult.getValueMap();
        List<ScreenplayCommentVo> screenplayCommentVoList = new ArrayList<>();
        for(Object value : queryValueMap.values()){
            ScreenplayCommentVo screenplayCommentVo = JSONUtil.toBean((String)value, ScreenplayCommentVo.class);
            screenplayCommentVoList.add(screenplayCommentVo);
        }
        Page<ScreenplayCommentVo> screenplayCommentVoPage = new Page<>();
        screenplayCommentVoPage.setRecords(screenplayCommentVoList);
        screenplayCommentVoPage.setCurrent(page);
        screenplayCommentVoPage.setSize(size);
        screenplayCommentVoPage.setTotal(sortedCacheResult.getTotal());
        return screenplayCommentVoPage;
    }

    private void getCommentUserInfo(ScreenplayCommentVo screenplayCommentVo, Map<Long, UserVO> commentUserMap, Long targetId) {
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

    private List<ScreenplayCommentVo> getScreenplayCommentVo(List<ScreenplayComment> screenplayCommentList){
        if(screenplayCommentList == null || screenplayCommentList.isEmpty()){
            return new ArrayList<>();
        }
        Set<Long> userIdSet = screenplayCommentList.stream().map(ScreenplayComment::getUserId).collect(Collectors.toSet());
        UserListVO userListVO = userFeignClient.listByIds(userIdSet);
        List<User> userList = userListVO.getUserList(userListVO.getUserListJson());
        Map<Long, List<User>> userIdUserListMap = userList.stream()
                .collect(Collectors.groupingBy(User::getId));
        List<ScreenplayCommentVo> screenplayCommentVoList = screenplayCommentList.stream().map(ScreenplayCommentVo::objToVo).toList();

        Map<Long, UserVO> commentUserMap = new HashMap<>();
        screenplayCommentVoList.forEach(screenplayCommentVo -> {
            Long userId = screenplayCommentVo.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).getFirst();
            }
            screenplayCommentVo.setUser(userFeignClient.getUserVO(user));
            commentUserMap.put(screenplayCommentVo.getId(), screenplayCommentVo.getUser());
        });

        // set target的用户相关信息
        screenplayCommentVoList.forEach(screenplayCommentVo -> {
            Long targetId = screenplayCommentVo.getTargetId();
            Long secondTargetId = screenplayCommentVo.getSecondTargetId();
            if(targetId != null && secondTargetId == null){
                this.getCommentUserInfo(screenplayCommentVo, commentUserMap, targetId);
            }
            if (targetId != null && secondTargetId != null) {
                this.getCommentUserInfo(screenplayCommentVo, commentUserMap, secondTargetId);
            }
        });

        return screenplayCommentVoList;
    }
}
