package com.tech.novagraphbackendgraphservice.interfaces.inner;

import com.tech.novagraphbackendgraphservice.application.picture.PictureApplicationService;
import com.tech.novagraphbackendgraphservice.application.screenplay.ScreenplayApplicationService;
import com.tech.novagraphbackendgraphservice.application.screenplay.ScreenplayCommentApplicationService;
import com.tech.novagraphbackendgraphservice.application.screenplay.ScreenplayThumbApplicationService;
import com.tech.novagraphbackendmodel.graph.entity.ScreenplayComment;
import com.tech.novagraphbackendmodel.graph.entity.ScreenplayThumb;
import com.tech.novagraphbackendserviceclient.GraphFeignClient;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/picture/inner")
public class GraphInnerController implements GraphFeignClient {

    @Resource
    private PictureApplicationService pictureApplicationService;

    @Resource
    private ScreenplayThumbApplicationService screenplayThumbApplicationService;

    @Resource
    private ScreenplayCommentApplicationService screenplayCommentApplicationService;

    /**
     * 用户上传头像
     * @return
     */
    @Override
    @PostMapping("/upload_user_avatar")
    public String uploadUserAvatar(MultipartFile multipartFile, String uploadPathPrefix) {
        return pictureApplicationService.uploadUserAvatar(multipartFile, uploadPathPrefix);
    }

    @Override
    @GetMapping("/thumb/get/id")
    public ScreenplayThumb getThumbById(Long commentId) {
        return screenplayThumbApplicationService.getThumbById(commentId);
    }

    @Override
    @GetMapping("/screenplayComment/get/id")
    public ScreenplayComment getScreenplayCommentById(Long commentId) {
        return screenplayCommentApplicationService.getById(commentId);
    }
}
