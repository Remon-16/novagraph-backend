package com.tech.novagraphbackendgraphservice.infrastructure.job;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.tech.novagraphbackendcommon.cache.RedisSyncTemplate;
import com.tech.novagraphbackendgraphservice.infrastructure.manager.handler.ThumbSPHandler;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SyncSPThumb2DBJob {
    @Resource
    private RedisSyncTemplate redisSyncTemplate;

    @Resource
    private ThumbSPHandler thumbSPHandler;

    @Scheduled(fixedRate = 10000)
    public void run() {
        DateTime nowDate = DateUtil.date();
        String date = DateUtil.format(nowDate, "HH:mm:") + (DateUtil.second(nowDate) / 10 - 1) * 10;
        redisSyncTemplate.executeSync(thumbSPHandler, date);
    }

}
