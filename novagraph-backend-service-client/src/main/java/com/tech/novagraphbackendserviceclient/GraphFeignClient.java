package com.tech.novagraphbackendserviceclient;

import com.tech.novagraphbackendmodel.graph.entity.ScreenplayComment;
import com.tech.novagraphbackendmodel.graph.entity.ScreenplayThumb;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

@FeignClient(name = "novagraph-backend-graph-service", path = "/api/graph/inner")
public interface GraphFeignClient {

    /**
     * 用户上传头像
     * @return
     */
    @PostMapping(value = "/upload_user_avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    String uploadUserAvatar(@RequestPart("file") MultipartFile multipartFile, @RequestParam("uploadPathPrefix") String uploadPathPrefix);

    @GetMapping("/thumb/get/id")
    ScreenplayThumb getThumbById(Long commentId);

    @GetMapping("/screenplayComment/get/id")
    ScreenplayComment getScreenplayCommentById(Long commentId);
}
