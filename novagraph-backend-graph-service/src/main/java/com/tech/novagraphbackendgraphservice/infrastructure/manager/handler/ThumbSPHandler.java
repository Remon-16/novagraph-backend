package com.tech.novagraphbackendgraphservice.infrastructure.manager.handler;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tech.novagraphbackendcommon.handler.IUserActionSyncHandler;
import com.tech.novagraphbackendgraphservice.infrastructure.mapper.ScreenplayStatisticsMapper;
import com.tech.novagraphbackendgraphservice.infrastructure.mapper.ScreenplayThumbMapper;
import com.tech.novagraphbackendmodel.graph.constant.ScreenplayCacheConstant;
import com.tech.novagraphbackendmodel.graph.entity.ScreenplayThumb;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class ThumbSPHandler implements IUserActionSyncHandler<ScreenplayThumb> {

    @Resource
    private ScreenplayThumbMapper screenplayThumbMapper;

    @Resource
    private ScreenplayStatisticsMapper screenplayStatisticsMapper;

    @Override
    public String getRedisKeyPrefix() {
        return ScreenplayCacheConstant.buildRedisKey(ScreenplayCacheConstant.TEMP_THUMB_KEY_PREFIX);
    }

    @Override
    public ScreenplayThumb buildEntity(Long userId, Long targetId) {
        ScreenplayThumb thumb = new ScreenplayThumb();
        thumb.setUserId(userId);
        thumb.setScreenplayId(targetId);
        return thumb;
    }

    @Override
    public void batchInsert(List<ScreenplayThumb> entityList) {
        screenplayThumbMapper.batchIgnoreInsert(entityList);
    }

    @Override
    public void singleInsert(ScreenplayThumb entity) {
        screenplayThumbMapper.insert(entity);
    }

    @Override
    public void batchRemove(List<Long> userIds, List<Long> targetIds) {
        LambdaQueryWrapper<ScreenplayThumb> wrapper = new LambdaQueryWrapper<>();
        for (int i = 0; i<userIds.size(); i++) {
            wrapper.or().eq(ScreenplayThumb::getUserId, userIds.get(i))
                    .eq(ScreenplayThumb::getScreenplayId, targetIds.get(i));
        }
        screenplayThumbMapper.delete(wrapper);
    }

    @Override
    public void singleDelete(Long userId, Long targetId) {
        LambdaQueryWrapper<ScreenplayThumb> wrapper = new LambdaQueryWrapper<>();
        wrapper.or().eq(ScreenplayThumb::getUserId, userId).eq(ScreenplayThumb::getScreenplayId, targetId);
        screenplayThumbMapper.delete(wrapper);
    }

    @Override
    public void updateStatistics(Map<Long, Long> countMap) {
        screenplayStatisticsMapper.batchUpdateThumbCount(countMap);
    }

    @Override
    public Long getTargetOwnerId(Long targetId) {
        return 0L;
    }

    @Override
    public void handleSideEffects(List<ScreenplayThumb> insertedList, Set<Long> targetIds) {

    }
}
