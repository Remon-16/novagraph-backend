package com.tech.novagraphbackendgraphservice.application.picture;

import org.springframework.web.multipart.MultipartFile;

public interface PictureApplicationService {

    String uploadUserAvatar(MultipartFile multipartFile, String uploadPathPrefix);
}
