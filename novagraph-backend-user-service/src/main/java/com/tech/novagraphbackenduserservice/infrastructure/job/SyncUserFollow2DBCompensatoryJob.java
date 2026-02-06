package com.tech.novagraphbackenduserservice.infrastructure.job;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import com.tech.novagraphbackendcommon.cache.RedisSyncTemplate;
import com.tech.novagraphbackendmodel.user.constant.UserCacheConstant;
import com.tech.novagraphbackenduserservice.infrastructure.manager.handler.UserFollowHandler;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Slf4j
@Component
public class SyncUserFollow2DBCompensatoryJob {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private RedisSyncTemplate redisSyncTemplate;

    @Resource
    private UserFollowHandler userFollowHandler;

    @Scheduled(cron = "0 0 2 * * *")
    public void run() {
        log.info("UserFollowHandler 开始补偿数据");
        Set<String> thumbKeys = redisTemplate.keys(
                UserCacheConstant.buildRedisKey(UserCacheConstant.getUserFollowTempKey("")) + "*");
        Set<String> needHandleDataSet = new HashSet<>();
        thumbKeys.stream().filter(ObjUtil::isNotNull).forEach(thumbKey -> needHandleDataSet.add(
                thumbKey.replace(UserCacheConstant.USER_FOLLOW_TEMP_KEY_PREFIX, "")));

        if (CollUtil.isEmpty(needHandleDataSet)) {
            log.info("UserFollowHandler 没有需要补偿的临时数据");
            return;
        }
        // 补偿数据
        for (String date : needHandleDataSet) {
            redisSyncTemplate.executeSync(userFollowHandler, date);
        }
        log.info("UserFollowHandler 临时数据补偿完成");
    }
}
