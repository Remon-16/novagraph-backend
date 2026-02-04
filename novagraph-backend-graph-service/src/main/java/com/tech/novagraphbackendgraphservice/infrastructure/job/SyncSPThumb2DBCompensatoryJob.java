package com.tech.novagraphbackendgraphservice.infrastructure.job;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import com.tech.novagraphbackendcommon.cache.RedisSyncTemplate;
import com.tech.novagraphbackendgraphservice.infrastructure.manager.handler.ThumbSPHandler;
import com.tech.novagraphbackendmodel.graph.constant.ScreenplayCacheConstant;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Slf4j
@Component
public class SyncSPThumb2DBCompensatoryJob {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private RedisSyncTemplate redisSyncTemplate;

    @Resource
    private ThumbSPHandler thumbSPHandler;

    @Scheduled(cron = "0 0 2 * * *")
    public void run() {
        log.info("开始补偿数据");
        Set<String> thumbKeys = redisTemplate.keys(
                ScreenplayCacheConstant.buildRedisKey(ScreenplayCacheConstant.getTempThumbKey("")) + "*");
        Set<String> needHandleDataSet = new HashSet<>();
        thumbKeys.stream().filter(ObjUtil::isNotNull).forEach(thumbKey -> needHandleDataSet.add(
                thumbKey.replace(ScreenplayCacheConstant.TEMP_THUMB_KEY_PREFIX, "")));

        if (CollUtil.isEmpty(needHandleDataSet)) {
            log.info("没有需要补偿的临时数据");
            return;
        }
        // 补偿数据
        for (String date : needHandleDataSet) {
            redisSyncTemplate.executeSync(thumbSPHandler, date);
        }
        log.info("临时数据补偿完成");
    }

}
