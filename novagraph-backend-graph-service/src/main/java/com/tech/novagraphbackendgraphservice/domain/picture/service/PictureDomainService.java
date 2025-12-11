package com.tech.novagraphbackendgraphservice.domain.picture.service;

import com.tech.novagraphbackendmodel.dto.graph.PictureUploadRequest;
import com.tech.novagraphbackendmodel.user.entity.User;
import com.tech.novagraphbackendmodel.vo.graph.PictureVO;

public interface PictureDomainService {

    /**
     * 上传图片
     *
     * @param inputSource          文件输入源
     * @param pictureUploadRequest
     * @param loginUser
     * @return
     */
    PictureVO uploadPicture(Object inputSource,
                            PictureUploadRequest pictureUploadRequest,
                            User loginUser);
}
