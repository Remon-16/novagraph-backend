package com.tech.novagraphbackendgraphservice.application.picture.impl;

import com.tech.novagraphbackendgraphservice.application.picture.PictureApplicationService;
import com.tech.novagraphbackendgraphservice.domain.picture.service.PictureDomainService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class PictureApplicationServiceImpl implements PictureApplicationService {

    @Resource
    PictureDomainService pictureDomainService;

    @Override
    public String uploadUserAvatar(MultipartFile multipartFile, String uploadPathPrefix) {
        return pictureDomainService.uploadUserAvatar(multipartFile, uploadPathPrefix);
    }
}
