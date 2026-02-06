package com.tech.novagraphbackenduserservice.infrastructure.job;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import com.tech.novagraphbackendcommon.cache.RedisSyncTemplate;
import com.tech.novagraphbackendmodel.graph.constant.ScreenplayCacheConstant;
import com.tech.novagraphbackendmodel.user.constant.UserCacheConstant;
import com.tech.novagraphbackenduserservice.infrastructure.manager.handler.ThumbPTHandler;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Slf4j
@Component
public class SyncPTThumb2DBCompensatoryJob {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private RedisSyncTemplate redisSyncTemplate;

    @Resource
    private ThumbPTHandler thumbPTHandler;

    @Scheduled(cron = "0 0 2 * * *")
    public void run() {
        log.info("ThumbPTHandler 开始补偿数据");
        Set<String> thumbKeys = redisTemplate.keys(
                UserCacheConstant.buildRedisKey(UserCacheConstant.getTempThumbKey("")) + "*");
        Set<String> needHandleDataSet = new HashSet<>();
        thumbKeys.stream().filter(ObjUtil::isNotNull).forEach(thumbKey -> needHandleDataSet.add(
                thumbKey.replace(UserCacheConstant.TEMP_THUMB_KEY_PREFIX, "")));

        if (CollUtil.isEmpty(needHandleDataSet)) {
            log.info("ThumbPTHandler 没有需要补偿的临时数据");
            return;
        }
        // 补偿数据
        for (String date : needHandleDataSet) {
            redisSyncTemplate.executeSync(thumbPTHandler, date);
        }
        log.info("ThumbPTHandler 临时数据补偿完成");
    }
}
