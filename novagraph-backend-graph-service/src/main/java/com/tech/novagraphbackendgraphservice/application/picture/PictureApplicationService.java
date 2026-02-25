package com.tech.novagraphbackendgraphservice.application.picture;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tech.novagraphbackendcommon.common.CanalHandleVO;
import com.tech.novagraphbackendmodel.dto.graph.PictureQueryRequest;
import com.tech.novagraphbackendmodel.dto.graph.PictureUploadRequest;
import com.tech.novagraphbackendmodel.user.entity.User;
import com.tech.novagraphbackendmodel.vo.graph.PictureVO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface PictureApplicationService {

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

    /**
     * 查询图片列表
     * @param pictureQueryRequest
     * @param request
     * @return
     */
    Page<PictureVO> listPictureVOByPage(PictureQueryRequest pictureQueryRequest, HttpServletRequest request);

    String uploadUserAvatar(MultipartFile multipartFile, String uploadPathPrefix);

    void canalHandlePicture(List<CanalHandleVO> canalHandleVoList);
}
