package com.tech.novagraphbackendgraphservice.infrastructure.repository;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tech.novagraphbackendmodel.graph.entity.Screenplay;
import com.tech.novagraphbackendgraphservice.domain.screenplay.repository.ScreenplayRepository;
import com.tech.novagraphbackendgraphservice.infrastructure.mapper.ScreenplayMapper;
import org.springframework.stereotype.Service;

/**
* @author Remon
* @description 针对表【screenplay(剧本表)】的数据库操作Service实现
* @createDate 2025-12-11 16:46:54
*/
@Service
public class ScreenplayRepositoryImpl extends ServiceImpl<ScreenplayMapper, Screenplay>
    implements ScreenplayRepository {

}




