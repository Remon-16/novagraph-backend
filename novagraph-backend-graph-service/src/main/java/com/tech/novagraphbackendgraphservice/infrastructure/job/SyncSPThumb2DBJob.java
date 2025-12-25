package com.tech.novagraphbackendgraphservice.infrastructure.job;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.text.StrPool;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tech.novagraphbackendgraphservice.domain.screenplay.service.ScreenplayThumbDomainService;
import com.tech.novagraphbackendgraphservice.infrastructure.mapper.ScreenplayMapper;
import com.tech.novagraphbackendmodel.graph.constant.ScreenplayCacheConstant;
import com.tech.novagraphbackendmodel.graph.entity.ScreenplayThumb;
import com.tech.novagraphbackendmodel.graph.valueobject.ThumbTypeEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class SyncSPThumb2DBJob {
    @Resource
    private ScreenplayThumbDomainService screenplayThumbDomainService;

    @Resource
    private ScreenplayMapper screenplayMapper;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Scheduled(fixedRate = 10000)
    @Transactional(rollbackFor = Exception.class)
    public void run() {
        log.info("开始执行");
        DateTime nowDate = DateUtil.date();
        String date = DateUtil.format(nowDate, "HH:mm:") + (DateUtil.second(nowDate) / 10 - 1) * 10;
        syncThumb2DBByDate(date);
        log.info("临时数据同步完成");
    }

    public void syncThumb2DBByDate(String date) {
        // 获取到临时点赞和取消点赞数据
        String tempThumbKey = ScreenplayCacheConstant.buildRedisKey(ScreenplayCacheConstant.getTempThumbKey(date));
        Map<Object, Object> allTempThumbMap = redisTemplate.opsForHash().entries(tempThumbKey);
        boolean thumbMapEmpty = CollUtil.isEmpty(allTempThumbMap);

        // 同步 点赞 到数据库
        // 构建插入列表并收集screenplayId
        Map<Long, Long> screenplayThumbCountMap = new HashMap<>();
        if (thumbMapEmpty) {
            return;
        }
        ArrayList<ScreenplayThumb> thumbList = new ArrayList<>();
        LambdaQueryWrapper<ScreenplayThumb> wrapper = new LambdaQueryWrapper<>();
        boolean needRemove = false;
        for (Object userIdScreenplayIdObj : allTempThumbMap.keySet()) {
            String userIdScreenplayId = (String) userIdScreenplayIdObj;
            String[] userIdAndScreenplayId = userIdScreenplayId.split(StrPool.COLON);
            Long userId = Long.valueOf(userIdAndScreenplayId[0]);
            Long screenplayId = Long.valueOf(userIdAndScreenplayId[1]);
            // -1 取消点赞，1 点赞
            Integer thumbType = Integer.valueOf(allTempThumbMap.get(userIdScreenplayId).toString());
            if (thumbType == ThumbTypeEnum.INCR.getValue()) {
                ScreenplayThumb thumb = new ScreenplayThumb();
                thumb.setUserId(userId);
                thumb.setScreenplayId(screenplayId);
                thumbList.add(thumb);
            } else if (thumbType == ThumbTypeEnum.DECR.getValue()) {
                // 拼接查询条件，批量删除
                needRemove = true;
                wrapper.or().eq(ScreenplayThumb::getUserId, userId).eq(ScreenplayThumb::getScreenplayId, screenplayId);
            } else {
                if (thumbType != ThumbTypeEnum.NON.getValue()) {
                    log.warn("数据异常：{}", userId + "," + screenplayId + "," + thumbType);
                }
                continue;
            }
            // 计算点赞增量
            screenplayThumbCountMap.put(screenplayId, screenplayThumbCountMap.getOrDefault(screenplayId, 0L) + thumbType);

        }
        // 批量插入
        screenplayThumbDomainService.saveBatch(thumbList);
        // 批量删除
        if (needRemove) {
            screenplayThumbDomainService.remove(wrapper);
        }
        // 批量更新图片点赞量，增加被点赞的用户积分
        if (!screenplayThumbCountMap.isEmpty()) {
            screenplayMapper.batchUpdateThumbCount(screenplayThumbCountMap);
        }

        // 使用虚拟线程异步删除
        Thread.startVirtualThread(() -> {
            redisTemplate.delete(tempThumbKey);
        });
    }

}
