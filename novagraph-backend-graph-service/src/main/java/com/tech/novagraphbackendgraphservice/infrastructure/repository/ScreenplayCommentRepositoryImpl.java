package com.tech.novagraphbackendgraphservice.infrastructure.repository;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tech.novagraphbackendmodel.graph.entity.ScreenplayComment;
import com.tech.novagraphbackendgraphservice.domain.screenplay.repository.ScreenplayCommentRepository;
import com.tech.novagraphbackendgraphservice.infrastructure.mapper.ScreenplayCommentMapper;
import org.springframework.stereotype.Service;

/**
* @author Remon
* @description 针对表【screenplay_comment(剧本评论表)】的数据库操作Service实现
* @createDate 2025-12-23 15:44:25
*/
@Service
public class ScreenplayCommentRepositoryImpl extends ServiceImpl<ScreenplayCommentMapper, ScreenplayComment>
    implements ScreenplayCommentRepository {

}




