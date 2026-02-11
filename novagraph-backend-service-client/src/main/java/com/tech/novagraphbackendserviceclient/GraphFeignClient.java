package com.tech.novagraphbackendserviceclient;

import com.tech.novagraphbackendmodel.graph.entity.ScreenplayComment;
import com.tech.novagraphbackendmodel.graph.entity.ScreenplayThumb;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@FeignClient(name = "novagraph-backend-graph-service", path = "/api/graph/inner")
public interface GraphFeignClient {

    /**
     * 用户上传头像
     */
    @PostMapping(value = "/upload_user_avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    String uploadUserAvatar(@RequestPart("file") MultipartFile multipartFile, @RequestParam("uploadPathPrefix") String uploadPathPrefix);

    @GetMapping("/thumb/get/id")
    ScreenplayThumb getThumbById(Long commentId);

    @GetMapping("/screenplayComment/get/id")
    ScreenplayComment getScreenplayCommentById(Long commentId);

    @PostMapping("sPStatistics/batchUpdatePlayCount")
    void batchUpdatePlayCount(@RequestBody Map<Long, Long> countMap);
    
    @PostMapping("sPStatistics/batchUpdateFavourites")
    void batchUpdateFavourites(@RequestBody Map<Long, Long> countMap);
}
