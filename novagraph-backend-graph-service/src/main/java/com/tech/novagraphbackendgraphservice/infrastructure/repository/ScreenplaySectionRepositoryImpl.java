package com.tech.novagraphbackendgraphservice.infrastructure.repository;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tech.novagraphbackendmodel.graph.entity.ScreenplaySection;
import com.tech.novagraphbackendgraphservice.domain.screenplay.repository.ScreenplaySectionRepository;
import com.tech.novagraphbackendgraphservice.infrastructure.mapper.ScreenplaySectionMapper;
import org.springframework.stereotype.Service;

/**
* @author Remon
* @description 针对表【screenplay_section(剧本章节)】的数据库操作Service实现
* @createDate 2025-12-11 16:47:13
*/
@Service
public class ScreenplaySectionRepositoryImpl extends ServiceImpl<ScreenplaySectionMapper, ScreenplaySection>
    implements ScreenplaySectionRepository {

}




