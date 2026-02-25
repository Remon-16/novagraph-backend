package com.tech.novagraphbackendgraphservice.application.picture.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tech.novagraphbackendcommon.common.CanalHandleVO;
import com.tech.novagraphbackendgraphservice.application.picture.PictureApplicationService;
import com.tech.novagraphbackendgraphservice.domain.picture.service.PictureDomainService;
import com.tech.novagraphbackendmodel.dto.graph.PictureQueryRequest;
import com.tech.novagraphbackendmodel.dto.graph.PictureUploadRequest;
import com.tech.novagraphbackendmodel.user.entity.User;
import com.tech.novagraphbackendmodel.vo.graph.PictureVO;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class PictureApplicationServiceImpl implements PictureApplicationService {

    @Resource
    PictureDomainService pictureDomainService;

    @Override
    public PictureVO uploadPicture(Object inputSource, PictureUploadRequest pictureUploadRequest, User loginUser) {
        return pictureDomainService.uploadPicture(inputSource, pictureUploadRequest, loginUser);
    }

    @Override
    public Page<PictureVO> listPictureVOByPage(PictureQueryRequest pictureQueryRequest, HttpServletRequest request) {
        return pictureDomainService.listPictureVOByPage(pictureQueryRequest, request);
    }

    @Override
    public String uploadUserAvatar(MultipartFile multipartFile, String uploadPathPrefix) {
        return pictureDomainService.uploadUserAvatar(multipartFile, uploadPathPrefix);
    }

    @Override
    public void canalHandlePicture(List<CanalHandleVO> canalHandleVoList) {
        pictureDomainService.canalHandlePicture(canalHandleVoList);
    }
}
