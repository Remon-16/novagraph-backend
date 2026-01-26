package com.tech.novagraphbackendgraphservice.infrastructure.repository;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tech.novagraphbackendmodel.graph.entity.ScreenplayStatistics;
import com.tech.novagraphbackendgraphservice.domain.screenplay.repository.ScreenplayStatisticsRepository;
import com.tech.novagraphbackendgraphservice.infrastructure.mapper.ScreenplayStatisticsMapper;
import org.springframework.stereotype.Service;

/**
* @author Remon
* @description 针对表【screenplay_statistics(剧本统计数据)】的数据库操作Service实现
* @createDate 2026-01-26 17:27:13
*/
@Service
public class ScreenplayStatisticsRepositoryImpl extends ServiceImpl<ScreenplayStatisticsMapper, ScreenplayStatistics>
    implements ScreenplayStatisticsRepository {

}




