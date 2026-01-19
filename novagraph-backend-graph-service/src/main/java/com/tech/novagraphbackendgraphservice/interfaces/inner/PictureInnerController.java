package com.tech.novagraphbackendgraphservice.interfaces.inner;

import com.tech.novagraphbackendgraphservice.application.picture.PictureApplicationService;
import com.tech.novagraphbackendserviceclient.PictureFeignClient;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/picture/inner")
public class PictureInnerController implements PictureFeignClient {

    @Resource
    private PictureApplicationService pictureApplicationService;
    /**
     * 用户上传头像
     * @return
     */
    @Override
    @PostMapping("/upload_user_avatar")
    public String uploadUserAvatar(MultipartFile multipartFile, String uploadPathPrefix) {
        return pictureApplicationService.uploadUserAvatar(multipartFile, uploadPathPrefix);
    }
}
