package com.tech.novagraphbackenduserservice.domain.user.service.impl;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tech.novagraphbackendcommon.cache.CacheManager;
import com.tech.novagraphbackendcommon.cache.SingleValueCacheTemplate;
import com.tech.novagraphbackendcommon.cache.ZSetPageCacheTemplate;
import com.tech.novagraphbackendcommon.cache.bean.ValueQueryBean;
import com.tech.novagraphbackendcommon.cache.bean.ZSetQueryBean;
import com.tech.novagraphbackendcommon.cache.bean.ZSetSaveBean;
import com.tech.novagraphbackendcommon.common.CanalHandleVO;
import com.tech.novagraphbackendcommon.exception.ErrorCode;
import com.tech.novagraphbackendcommon.exception.ThrowUtils;
import com.tech.novagraphbackendmodel.dto.user.UserPostAddRequest;
import com.tech.novagraphbackendmodel.dto.user.UserPostQueryRequest;
import com.tech.novagraphbackendmodel.user.constant.UserCacheConstant;
import com.tech.novagraphbackendmodel.user.entity.UserFollow;
import com.tech.novagraphbackendmodel.user.entity.UserPost;
import com.tech.novagraphbackendmodel.user.entity.UserPostStatistics;
import com.tech.novagraphbackendmodel.user.entity.UserPostWithStats;
import com.tech.novagraphbackendmodel.user.valueobject.UserPostEnum;
import com.tech.novagraphbackendmodel.vo.graph.ScreenplayVO;
import com.tech.novagraphbackendmodel.vo.user.UserPostVO;
import com.tech.novagraphbackendserviceclient.GraphFeignClient;
import com.tech.novagraphbackenduserservice.domain.user.repository.UserPostRepository;
import com.tech.novagraphbackenduserservice.domain.user.repository.UserPostStatisticsRepository;
import com.tech.novagraphbackenduserservice.domain.user.service.UserFollowDomainService;
import com.tech.novagraphbackenduserservice.domain.user.service.UserPostDomainService;
import com.tech.novagraphbackenduserservice.domain.user.service.UserPostThumbDomainService;
import com.tech.novagraphbackenduserservice.infrastructure.mapper.UserPostMapper;
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

    @Resource
    private UserPostMapper userPostMapper;

    @Resource
    private UserPostThumbDomainService userPostThumbDomainService;

    @Resource
    private UserPostStatisticsRepository UserPostStatisticsRepository;

    @Resource
    private SingleValueCacheTemplate singleValueCacheTemplate;

    @Resource
    private CacheManager cacheManager;

    @Resource
    private UserFollowDomainService userFollowDomainService;

    @Override
    public UserPost saveOrUpdate(UserPostAddRequest userPostAddRequest) {
        ThrowUtils.throwIf(userPostAddRequest == null, ErrorCode.PARAMS_ERROR);
        UserPost userPost = new UserPost();
        BeanUtils.copyProperties(userPostAddRequest, userPost);
        if (userPostAddRequest.getId() == null) {
            userPostRepository.save(userPost);
            UserPostStatistics userPostStatistics = new UserPostStatistics();
            userPostStatistics.setPostId(userPost.getId());
            UserPostStatisticsRepository.save(userPostStatistics);
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
                () -> {
                    Page<UserPostWithStats> page = new Page<>(current, size);
                    return userPostMapper.selectUserPostWithStatsPage(page, userPostQueryRequest);
                },
                (userPostList) -> this.entityToVO(userPostList, userPostQueryRequest.getLoginUserId())
                );
    }

    @Override
    public UserPostVO getUserPostVOById(Long userPostId, Long userId) {
        String detailKey = UserCacheConstant.getUserPostKey(userPostId);
        ValueQueryBean valueQueryBean = new ValueQueryBean();
        valueQueryBean.setCacheKey(detailKey);
        valueQueryBean.setVOClass(UserPostVO.class);

        return singleValueCacheTemplate.valueQuery(userPostId, valueQueryBean,
                () -> userPostMapper.selectUserPostWithStatsById(userPostId),
                (userPostWithStats) -> this.entityToVO(userPostWithStats, userId)
                );
    }

    @Override
    public void canalHandleUserPost(List<CanalHandleVO> canalHandleVoList) {
        canalHandleVoList.forEach(canalHandleVo -> {
            CanalEntry.EventType eventType = canalHandleVo.getEventType();
            UserPost userPost = JSONUtil.toBean(canalHandleVo.getJsonDataStr(), UserPost.class);
            if (eventType == CanalEntry.EventType.DELETE || cacheManager.getEntry_DELETE_FLAG().equals(userPost.getIsDelete())) {
                canalDeleteHandle(userPost);
            }else if (eventType == CanalEntry.EventType.UPDATE) {
                canalUpdateHandle(userPost);
            }else if (eventType == CanalEntry.EventType.INSERT) {
                canalInsertHandle(userPost);
            }
        });
    }

    private void canalInsertHandle(UserPost userPost) {
        String detailKey = UserCacheConstant.getUserPostKey(userPost.getId());
        UserPostVO userPostVO = this.getUserPostVOById(userPost.getId(), null);
        String valueStr = JSONUtil.toJsonStr(userPostVO);
        cacheManager.putValueToCache(detailKey, valueStr);

        // 更新所有用户的ZSet
        List<UserFollow> userFollowList = userFollowDomainService.getFollowerList(userPost.getUserId());
        userFollowList.forEach(userFollow -> {
            Long followerId = userFollow.getUserId();
            String sortedKey = UserCacheConstant.getUserPostSortedCacheKey(followerId);
            String sortedTotalKey = UserCacheConstant.getUserPostSortedTotalCacheKey(followerId);

            Double score = (double) userPost.getCreateTime().getTime();
            cacheManager.zSetAdd(sortedKey, userPost.getId(), score);
            Object v = cacheManager.getValueCache(sortedTotalKey);
            Long totalScore = (Long) v;
            totalScore += 1;
            cacheManager.putValueToCache(sortedTotalKey, totalScore.toString());
        });
    }

    private void canalUpdateHandle(UserPost userPost) {
        String key = UserCacheConstant.getUserPostKey(userPost.getId());
        Object value = cacheManager.getValueCache(key);
        if (value == null) {
            return;
        }
        UserPostVO oldUserPost = JSONUtil.toBean(value.toString(), UserPostVO.class);
        this.singleUpdateUserPost(oldUserPost, userPost);
        String valueStr = JSONUtil.toJsonStr(oldUserPost);
        cacheManager.putValueToCache(key, valueStr);
    }

    private void canalDeleteHandle(UserPost userPost) {
        String detailKey = UserCacheConstant.getUserPostKey(userPost.getId());
        cacheManager.removeValueCache(detailKey);

        // 更新所有用户的ZSet
        List<UserFollow> userFollowList = userFollowDomainService.getFollowerList(userPost.getUserId());
        userFollowList.forEach(userFollow -> {
            Long followerId = userFollow.getUserId();
            String sortedKey = UserCacheConstant.getUserPostSortedCacheKey(followerId);
            String sortedTotalKey = UserCacheConstant.getUserPostSortedTotalCacheKey(followerId);

            cacheManager.zSetRemove(sortedKey, userPost.getId());
            Object v = cacheManager.getValueCache(sortedTotalKey);
            Long totalScore = (Long) v;
            totalScore -= 1;
            cacheManager.putValueToCache(sortedTotalKey, totalScore.toString());
        });
    }

    private void singleUpdateUserPost(UserPostVO oldUserPost, UserPost newUserPost) {
        oldUserPost.setPostType(newUserPost.getPostType());
        oldUserPost.setContent(newUserPost.getContent());
        oldUserPost.setVisibility(newUserPost.getVisibility());
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

    private List<UserPostVO> entityToVO(List<UserPostWithStats> userPostList, Long loginUserId) {
        List<UserPostVO> userPostVOList = new ArrayList<>();
        for (UserPostWithStats userPost : userPostList) {
            UserPostVO userPostVO = this.entityToVO(userPost, loginUserId);
            userPostVOList.add(userPostVO);
        }
        return userPostVOList;
    }

    private UserPostVO entityToVO(UserPostWithStats userPost, Long loginUserId){
        UserPostVO userPostVO = new UserPostVO();
        BeanUtils.copyProperties(userPost, userPostVO);
        if(UserPostEnum.SCREENPLAY.getValue().equals(userPost.getPostType())){
            ScreenplayVO screenplayVO = graphFeignClient.getScreenplayById(userPost.getQuotedId());
            userPostVO.setScreenplayVO(screenplayVO);
        }
        if(loginUserId != null){
            Boolean hasThumb = userPostThumbDomainService.hasThumb(userPost.getId(), loginUserId);
            userPostVO.setHasThumb(hasThumb);
        }
        return userPostVO;
    }
}
