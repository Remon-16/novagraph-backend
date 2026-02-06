package com.tech.novagraphbackenduserservice.infrastructure.job;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.tech.novagraphbackendcommon.cache.RedisSyncTemplate;
import com.tech.novagraphbackenduserservice.infrastructure.manager.handler.UserFollowHandler;
import jakarta.annotation.Resource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class SyncUserFollow2DBJob {

    @Resource
    private RedisSyncTemplate redisSyncTemplate;

    @Resource
    private UserFollowHandler userFollowHandler;

    @Scheduled(fixedRate = 10000)
    public void run() {
        DateTime nowDate = DateUtil.date();
        String date = DateUtil.format(nowDate, "HH:mm:") + (DateUtil.second(nowDate) / 10 - 1) * 10;
        redisSyncTemplate.executeSync(userFollowHandler, date);
    }
}
