package com.tech.novagraphbackendgraphservice.infrastructure.repository;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tech.novagraphbackendmodel.graph.entity.Picture;
import com.tech.novagraphbackendgraphservice.domain.picture.repository.PictureRepository;
import com.tech.novagraphbackendgraphservice.infrastructure.mapper.PictureMapper;
import org.springframework.stereotype.Service;

/**
* @author Remon
* @description 针对表【picture(图片)】的数据库操作Service实现
* @createDate 2025-12-09 15:52:06
*/
@Service
public class PictureRepositoryImpl extends ServiceImpl<PictureMapper, Picture>
    implements PictureRepository {

}




