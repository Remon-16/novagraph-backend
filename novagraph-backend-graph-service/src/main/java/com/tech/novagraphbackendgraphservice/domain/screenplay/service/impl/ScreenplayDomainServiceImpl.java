package com.tech.novagraphbackendgraphservice.domain.screenplay.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.TypeReference;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tech.novagraphbackendcommon.cache.CacheManager;
import com.tech.novagraphbackendcommon.cache.SingleValueCacheTemplate;
import com.tech.novagraphbackendcommon.cache.ValuePageCacheTemplate;
import com.tech.novagraphbackendcommon.cache.bean.ValueQueryBean;
import com.tech.novagraphbackendcommon.common.CanalHandleVO;
import com.tech.novagraphbackendcommon.exception.BusinessException;
import com.tech.novagraphbackendcommon.exception.ErrorCode;
import com.tech.novagraphbackendcommon.exception.ThrowUtils;
import com.tech.novagraphbackendcommon.utils.CacheUtils;
import com.tech.novagraphbackendgraphservice.domain.screenplay.repository.ScreenplayRepository;
import com.tech.novagraphbackendgraphservice.domain.screenplay.service.ScreenplayDomainService;
import com.tech.novagraphbackendgraphservice.domain.screenplay.service.ScreenplayStatisticsDomainService;
import com.tech.novagraphbackendgraphservice.infrastructure.mapper.ScreenplayMapper;
import com.tech.novagraphbackendmodel.dto.graph.ScreenplayAddRequest;
import com.tech.novagraphbackendmodel.dto.graph.ScreenplayQueryRequest;
import com.tech.novagraphbackendmodel.dto.graph.ScreenplayReviewRequest;
import com.tech.novagraphbackendmodel.dto.graph.ScreenplayUpdateRequest;
import com.tech.novagraphbackendmodel.graph.constant.ScreenplayCacheConstant;
import com.tech.novagraphbackendmodel.graph.entity.Screenplay;
import com.tech.novagraphbackendmodel.graph.entity.ScreenplayStatistics;
import com.tech.novagraphbackendmodel.graph.entity.ScreenplayWithStats;
import com.tech.novagraphbackendmodel.graph.valueobject.ScreenplayReviewStatusEnum;
import com.tech.novagraphbackendmodel.user.entity.User;
import com.tech.novagraphbackendmodel.vo.graph.ScreenplayVO;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Set;

@Service
public class ScreenplayDomainServiceImpl extends ServiceImpl<ScreenplayMapper, Screenplay>
        implements ScreenplayDomainService {

    @Resource
    private ScreenplayRepository screenplayRepository;

    @Resource
    private ValuePageCacheTemplate valuePageCacheTemplate;

    @Resource
    private SingleValueCacheTemplate singleValueCacheTemplate;

    @Resource
    private ScreenplayMapper screenplayMapper;

    @Resource
    private ScreenplayStatisticsDomainService screenplayStatisticsDomainService;

    @Resource
    private CacheManager cacheManager;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public Screenplay addScreenplay(ScreenplayAddRequest screenplayAddRequest) {
        ThrowUtils.throwIf(screenplayAddRequest == null, ErrorCode.PARAMS_ERROR, "剧本为空");
        ThrowUtils.throwIf(StringUtils.isEmpty(screenplayAddRequest.getName()), ErrorCode.PARAMS_ERROR, "剧本名称为空");
        Screenplay screenplay = ScreenplayAddRequest.dtoToObj(screenplayAddRequest);
        screenplayRepository.save(screenplay);

        ScreenplayStatistics screenplayStatistics = new ScreenplayStatistics();
        screenplayStatistics.setScreenplayId(screenplay.getId());
        screenplayStatisticsDomainService.save(screenplayStatistics);
        return screenplay;
    }

    @Override
    public Boolean updateScreenplay(ScreenplayUpdateRequest screenplayUpdateRequest) {
        ThrowUtils.throwIf(screenplayUpdateRequest == null, ErrorCode.PARAMS_ERROR, "screenplayUpdateRequest 为空");
        ThrowUtils.throwIf(screenplayUpdateRequest.getId() == null, ErrorCode.PARAMS_ERROR, "ID不能为空");
        ThrowUtils.throwIf(screenplayUpdateRequest.getUserId() == null, ErrorCode.PARAMS_ERROR, "用户不能为空");
        Screenplay screenplay = ScreenplayUpdateRequest.resToObj(screenplayUpdateRequest);
        return screenplayRepository.updateById(screenplay);
    }

    @Override
    public ScreenplayVO queryScreenplayById(Long id, Long userId) {
        ThrowUtils.throwIf(id == null, ErrorCode.PARAMS_ERROR, "ID不能为空");
        ScreenplayQueryRequest screenplayQueryRequest = new ScreenplayQueryRequest();
        screenplayQueryRequest.setId(id);
        String cacheKey = ScreenplayCacheConstant.getScreenplayCacheKey(id.toString());
        ValueQueryBean valueQueryBean = new ValueQueryBean();
        valueQueryBean.setCacheKey(cacheKey);
        valueQueryBean.setVOClass(ScreenplayVO.class);

        ScreenplayVO screenplayVO = singleValueCacheTemplate.valueQuery(screenplayQueryRequest, valueQueryBean,
                () -> screenplayMapper.selectScreenplayWithStatsById(id),
                ScreenplayVO::objWithStatsToVo);
        if(userId != null) {
            screenplayVO = screenplayStatisticsDomainService.getScreenplayStatistics(screenplayVO, userId);
        }
        return screenplayVO;
    }

    private QueryWrapper<Screenplay> getQueryWrapper(ScreenplayQueryRequest screenplayQueryRequest) {
        QueryWrapper<Screenplay> queryWrapper = new QueryWrapper<>();
        if(screenplayQueryRequest == null){
            return queryWrapper;
        }
        Long userId = screenplayQueryRequest.getUserId();
        String name = screenplayQueryRequest.getName();
        String introduction = screenplayQueryRequest.getIntroduction();
        String category = screenplayQueryRequest.getCategory();
        List<String> tags = screenplayQueryRequest.getTags();
        String sortField = screenplayQueryRequest.getSortField();
        String sortOrder = screenplayQueryRequest.getSortOrder();
        Date startEditTime = screenplayQueryRequest.getStartEditTime();
        Date endEditTime = screenplayQueryRequest.getEndEditTime();

        queryWrapper.eq(ObjUtil.isNotEmpty(userId), "userId", userId);
        queryWrapper.like(StrUtil.isNotBlank(name), "name", name);
        queryWrapper.eq(StrUtil.isNotBlank(category), "category", category);
        queryWrapper.like(StrUtil.isNotBlank(introduction), "introduction", introduction);
        // JSON 数组查询
        if (CollUtil.isNotEmpty(tags)) {
            for (String tag : tags) {
                queryWrapper.like("tags", "\"" + tag + "\"");
            }
        }
        // >= startEditTime
        queryWrapper.ge(ObjUtil.isNotEmpty(startEditTime), "editTime", startEditTime);
        // < endEditTime
        queryWrapper.lt(ObjUtil.isNotEmpty(endEditTime), "editTime", endEditTime);
        // 排序
        queryWrapper.orderBy(StrUtil.isNotEmpty(sortField), sortOrder.equals("ascend"), sortField);

        return queryWrapper;
    }

    @Override
    public Page<ScreenplayVO> queryScreenplayPage(ScreenplayQueryRequest screenplayQueryRequest) {
        int current = screenplayQueryRequest.getCurrent();
        int size = screenplayQueryRequest.getPageSize();
        String cacheKey = ScreenplayCacheConstant.getScreenplayQueryCacheKey(screenplayQueryRequest);
        ValueQueryBean valueQueryBean = new ValueQueryBean();
        valueQueryBean.setCacheKey(cacheKey);
        valueQueryBean.setVOClass(ScreenplayVO.class);
        // 1 查询出 Page<ScreenplayVO>
        Page<ScreenplayVO> resPage = valuePageCacheTemplate.valueQuery(screenplayQueryRequest, valueQueryBean,
                () -> {
                    Page<ScreenplayWithStats> page = new Page<>(current, size);
                    return screenplayMapper.selectScreenplayWithStatsPage(page, screenplayQueryRequest);
                },
                ScreenplayVO::listObjWithStatsToVo);

        // 2 从缓存中查询最新的统计数据（如果缓存没命中，直接把现有的 Put到缓存中）
        List<ScreenplayVO> screenplayVOList = resPage.getRecords();
        resPage.setRecords(screenplayStatisticsDomainService.getScreenplayStatisticsList(screenplayVOList, screenplayQueryRequest.getUserId()));
        return resPage;
    }

    @Override
    public void doScreenplayReview(ScreenplayReviewRequest screenplayReviewRequest, User loginUser) {
        ThrowUtils.throwIf(screenplayReviewRequest == null, ErrorCode.PARAMS_ERROR);
        Long spId = screenplayReviewRequest.getId();
        ScreenplayReviewStatusEnum screenplayReviewStatusEnum = ScreenplayReviewStatusEnum.getEnumByValue(screenplayReviewRequest.getReviewStatus());
        if(spId == null || screenplayReviewStatusEnum == null || ScreenplayReviewStatusEnum.REVIEWING.equals(screenplayReviewStatusEnum)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 判断剧本是否存在
        Screenplay oldScreenplay = screenplayMapper.selectById(spId);
        ThrowUtils.throwIf(oldScreenplay == null, ErrorCode.NOT_FOUND_ERROR);
        if(oldScreenplay.getReviewStatus().equals(screenplayReviewRequest.getReviewStatus())){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请勿重复审核");
        }
        // 更新
        Screenplay screenplay = new Screenplay();
        BeanUtils.copyProperties(screenplayReviewRequest, screenplay);
        screenplay.setReviewerId(loginUser.getId());
        screenplay.setReviewTime(screenplay.getReviewTime());
        boolean result = this.updateById(screenplay);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
    }

    @Override
    public void canalHandleScreenplay(List<CanalHandleVO> canalHandleVoList) {
        canalHandleVoList.forEach(canalHandleVo -> {
            CanalEntry.EventType eventType = canalHandleVo.getEventType();
            Screenplay screenplay = JSONUtil.toBean(canalHandleVo.getJsonDataStr(), Screenplay.class);
            if (eventType == CanalEntry.EventType.DELETE || cacheManager.getEntry_DELETE_FLAG().equals(screenplay.getIsDelete())) {
                canalDeleteHandle(screenplay);
            }else if (eventType == CanalEntry.EventType.UPDATE) {
                canalUpdateHandle(screenplay);
            }
        });
    }

    private void canalDeleteHandle(Screenplay screenplay) {
        this.deleteHomeCache(screenplay);
        this.deleteSingleCache(screenplay);
    }

    private void canalUpdateHandle(Screenplay screenplay) {
        this.updateHomeCache(screenplay);
        this.updateSingleCache(screenplay);
    }

    private void updateHomeCache(Screenplay screenplay) {
        String keyHead = ScreenplayCacheConstant.SCREENPLAY_QUERY_CACHE_PREFIX;
        Set<String> targetKeys = redisTemplate.keys(ScreenplayCacheConstant.buildRedisKey(keyHead + "*"));
        ScreenplayVO newScreenplayVO = ScreenplayVO.objToVo(screenplay);
        for (String targetKey : targetKeys) {
            if(targetKey == null || targetKey.isEmpty()){
                continue;
            }
            Page<ScreenplayVO> page = this.getScreenplayPage(targetKey);
            boolean needUpdate = false;

            for(ScreenplayVO screenplayVO : page.getRecords()){
                if(screenplayVO.getId().equals(newScreenplayVO.getId())){
                    this.updateSingleScreenplay(screenplayVO, newScreenplayVO);
                    needUpdate = true;
                }
            }
            if(needUpdate){
                String cacheValue = JSONUtil.toJsonStr(page);
                cacheManager.putValueToCache(targetKey.replace(CacheUtils.APP_NAME + ":", ""), cacheValue);
            }
        }
    }

    private void updateSingleCache(Screenplay screenplay) {
        String cacheKey = ScreenplayCacheConstant.getScreenplayCacheKey(screenplay.getId().toString());
        Object value = cacheManager.getValueCache(cacheKey);
        if(value == null){
            return;
        }
        ScreenplayVO newScreenplayVO = ScreenplayVO.objToVo(screenplay);
        ScreenplayVO oldScreenplayVO = JSONUtil.toBean((String) value, ScreenplayVO.class);
        this.updateSingleScreenplay(oldScreenplayVO, newScreenplayVO);
        String cacheValue = JSONUtil.toJsonStr(oldScreenplayVO);
        cacheManager.putValueToCache(cacheKey, cacheValue);
    }

    private void deleteHomeCache(Screenplay screenplay) {
        String keyHead = ScreenplayCacheConstant.SCREENPLAY_QUERY_CACHE_PREFIX;
        Set<String> targetKeys = redisTemplate.keys(ScreenplayCacheConstant.buildRedisKey(keyHead + "*"));
        ScreenplayVO newScreenplayVO = ScreenplayVO.objToVo(screenplay);
        for (String targetKey : targetKeys) {
            if(targetKey == null || targetKey.isEmpty()){
                continue;
            }
            Page<ScreenplayVO> page = this.getScreenplayPage(targetKey);
            for(ScreenplayVO screenplayVO : page.getRecords()){
                if(screenplayVO.getId().equals(newScreenplayVO.getId())){
                    String k = targetKey.replace(CacheUtils.APP_NAME + ":", "");
                    cacheManager.removeValueCache(k);
                    break;
                }
            }
        }
    }

    private void deleteSingleCache(Screenplay screenplay) {
        String cacheKey = ScreenplayCacheConstant.getScreenplayCacheKey(screenplay.getId().toString());
        cacheManager.removeValueCache(cacheKey);
    }

    private Page<ScreenplayVO> getScreenplayPage(String targetKey){
        Object value = cacheManager.getValueCache(targetKey);
        return JSONUtil.toBean(
                (String) value,
                new TypeReference<>() {}, // 指定完整泛型结构
                false // 是否忽略转换错误
        );
    }

    private void updateSingleScreenplay(ScreenplayVO sourceScreenplayVO, ScreenplayVO newScreenplayVO) {
        sourceScreenplayVO.setCategory(newScreenplayVO.getCategory());
        sourceScreenplayVO.setName(newScreenplayVO.getName());
        sourceScreenplayVO.setCover(newScreenplayVO.getCover());
        sourceScreenplayVO.setIntroduction(newScreenplayVO.getIntroduction());
        sourceScreenplayVO.setTags(newScreenplayVO.getTags());
        sourceScreenplayVO.setPlotTree(newScreenplayVO.getPlotTree());
    }

}
