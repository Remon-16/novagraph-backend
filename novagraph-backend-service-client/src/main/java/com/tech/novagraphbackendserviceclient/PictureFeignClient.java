package com.tech.novagraphbackendserviceclient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

@FeignClient(name = "novagraph-backend-graph-service", path = "/api/picture/inner")
public interface PictureFeignClient {

    /**
     * 用户上传头像
     * @return
     */
    @PostMapping(value = "/upload_user_avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    String uploadUserAvatar(@RequestPart("file") MultipartFile multipartFile, @RequestParam("uploadPathPrefix") String uploadPathPrefix);
}
