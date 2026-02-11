package com.tech.novagraphbackenduserservice.infrastructure.job;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.tech.novagraphbackenduserservice.infrastructure.manager.handler.UserFavoriteHandler;
import jakarta.annotation.Resource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class SyncFA2DBJob {

    @Resource
    private UserFavoriteHandler userFavoriteHandler;

    @Scheduled(fixedRate = 10000)
    public void run() {
        DateTime nowDate = DateUtil.date();
        String date = DateUtil.format(nowDate, "HH:mm:") + (DateUtil.second(nowDate) / 10 - 1) * 10;
        userFavoriteHandler.executeSync(date);
    }
}
