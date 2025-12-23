package com.tech.novagraphbackendgraphservice.infrastructure.repository;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tech.novagraphbackendmodel.graph.entity.ScreenplayThumb;
import com.tech.novagraphbackendgraphservice.domain.screenplay.repository.ScreenplayThumbRepository;
import com.tech.novagraphbackendgraphservice.infrastructure.mapper.ScreenplayThumbMapper;
import org.springframework.stereotype.Service;

/**
* @author Remon
* @description 针对表【screenplay_thumb(剧本点赞记录表)】的数据库操作Service实现
* @createDate 2025-12-23 15:44:15
*/
@Service
public class ScreenplayThumbRepositoryImpl extends ServiceImpl<ScreenplayThumbMapper, ScreenplayThumb>
    implements ScreenplayThumbRepository {

}




