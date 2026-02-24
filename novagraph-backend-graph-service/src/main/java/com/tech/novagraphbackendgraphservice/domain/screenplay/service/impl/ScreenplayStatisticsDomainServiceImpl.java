package com.tech.novagraphbackendgraphservice.domain.screenplay.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tech.novagraphbackendcommon.cache.CacheManager;
import com.tech.novagraphbackendgraphservice.domain.screenplay.service.ScreenplayStatisticsDomainService;
import com.tech.novagraphbackendgraphservice.domain.screenplay.service.ScreenplayThumbDomainService;
import com.tech.novagraphbackendgraphservice.infrastructure.mapper.ScreenplayStatisticsMapper;
import com.tech.novagraphbackendmodel.graph.constant.ScreenplayCacheConstant;
import com.tech.novagraphbackendmodel.graph.entity.ScreenplayStatistics;
import com.tech.novagraphbackendmodel.user.entity.User;
import com.tech.novagraphbackendmodel.vo.graph.ScreenplayVO;
import com.tech.novagraphbackendserviceclient.UserFeignClient;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ScreenplayStatisticsDomainServiceImpl extends ServiceImpl<ScreenplayStatisticsMapper, ScreenplayStatistics>
        implements ScreenplayStatisticsDomainService {

    @Resource
    private ScreenplayStatisticsMapper screenplayStatisticsMapper;

    @Resource
    private CacheManager cacheManager;

    @Resource
    private ScreenplayThumbDomainService screenplayThumbDomainService;

    @Resource
    private UserFeignClient userFeignClient;

    @Override
    public void playCountAdd(Map<Long, Long> countMap) {
        screenplayStatisticsMapper.batchUpdatePlayCount(countMap);
    }

    @Override
    public void favouriteCountUpdate(Map<Long, Long> countMap) {
        screenplayStatisticsMapper.batchUpdateFavouriteCount(countMap);
    }

    @Override
    public ScreenplayVO getScreenplayStatistics(ScreenplayVO screenplayVO, Long userId) {
        // 点赞
        String screenplayKey = ScreenplayCacheConstant.getScreenplayCacheKey(screenplayVO.getId().toString());
        Object thumbValue = cacheManager.getValueCache(screenplayKey);
        if (thumbValue != null) {
            screenplayVO.setThumbCount((Long) thumbValue);
        }else {
            cacheManager.putValueToCache(screenplayKey, screenplayVO.getThumbCount());
        }
        if (userId != null) {
            screenplayThumbDomainService.hasThumb(screenplayVO.getId(), userId);
        }
        // 收藏
        Long faCount = userFeignClient.getUserFavoriteCount(screenplayVO.getId());
        if (faCount != null) {
            screenplayVO.setFavoriteCount(faCount);
        }
        if (userId != null) {
            screenplayVO.setHasFavorite(userFeignClient.userHasFavorite(screenplayVO.getId(), userId) != null);
        }
        // 播放量
        Long playCount = userFeignClient.getUserPlayHistoryCount(screenplayVO.getId());
        if (playCount != null) {
            screenplayVO.setPlayCount(playCount);
        }
        return screenplayVO;
    }

    @Override
    public List<ScreenplayVO> getScreenplayStatisticsList(List<ScreenplayVO> screenplayVOList, Long userId) {
        return screenplayVOList.stream()
                .map(screenplayVO -> this.getScreenplayStatistics(screenplayVO, userId))
                .collect(Collectors.toList());
    }
}
