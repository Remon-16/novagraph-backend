package com.tech.novagraphbackendgraphservice.domain.picture.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.TypeReference;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tech.novagraphbackendcommon.cache.CacheManager;
import com.tech.novagraphbackendcommon.exception.BusinessException;
import com.tech.novagraphbackendcommon.exception.ErrorCode;
import com.tech.novagraphbackendcommon.exception.ThrowUtils;
import com.tech.novagraphbackendcommon.utils.CacheUtils;
import com.tech.novagraphbackendgraphservice.domain.picture.repository.PictureRepository;
import com.tech.novagraphbackendgraphservice.domain.picture.service.PictureDomainService;
import com.tech.novagraphbackendgraphservice.infrastructure.manager.upload.FilePictureUpload;
import com.tech.novagraphbackendgraphservice.infrastructure.manager.upload.PictureUploadTemplate;
import com.tech.novagraphbackendgraphservice.infrastructure.manager.upload.UrlPictureUpload;
import com.tech.novagraphbackendgraphservice.infrastructure.mapper.PictureMapper;
import com.tech.novagraphbackendmodel.dto.graph.PictureQueryRequest;
import com.tech.novagraphbackendmodel.dto.graph.PictureUploadRequest;
import com.tech.novagraphbackendmodel.graph.constant.PictureCacheConstant;
import com.tech.novagraphbackendmodel.graph.entity.Picture;
import com.tech.novagraphbackendmodel.user.entity.User;
import com.tech.novagraphbackendmodel.vo.graph.PictureVO;
import com.tech.novagraphbackendmodel.vo.user.UserListVO;
import com.tech.novagraphbackendserviceclient.UserFeignClient;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class PictureDomainServiceImpl extends ServiceImpl<PictureMapper, Picture>
        implements PictureDomainService {

    @Resource
    private PictureRepository pictureRepository;

    @Resource
    private FilePictureUpload filePictureUpload;

    @Resource
    private UrlPictureUpload urlPictureUpload;

    @Resource
    private UserFeignClient userFeignClient;

    @Resource
    private CacheManager cacheManager;

    private final Map<String, Object> lockMap = new ConcurrentHashMap<>();

    @Override
    public PictureVO uploadPicture(Object inputSource, PictureUploadRequest pictureUploadRequest, User loginUser) {
        // 校验参数
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);
        // 判断是新增还是删除
        Long pictureId = null;
        if (pictureUploadRequest != null) {
            pictureId = pictureUploadRequest.getId();
        }
        // 如果是更新，判断图片是否存在
        if (pictureId != null) {
            Picture oldPicture = pictureRepository.getById(pictureId);
            ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR, "图片不存在");
        }
        // 上传图片，得到图片信息
        // 按照用户 id 划分目录
        String uploadPathPrefix = String.format("NovaGraph/%s", loginUser.getId());;
        // 根据 inputSource 的类型区分上传方式
        PictureUploadTemplate pictureUploadTemplate = filePictureUpload;
        if (inputSource instanceof String) {
            pictureUploadTemplate = urlPictureUpload;
        }
        com.tech.imagecorebackendpictureservice.infrastructure.manager.upload.model.dto.file.UploadPictureResult uploadPictureResult = pictureUploadTemplate.uploadPicture(inputSource, uploadPathPrefix);
        // 构造要入库的图片信息
        Picture picture = new Picture();
        picture.setUrl(uploadPictureResult.getUrl());
        picture.setThumbnailUrl(uploadPictureResult.getThumbnailUrl());
        // 支持外层传递图片名称
        String picName = uploadPictureResult.getPicName();
        if (pictureUploadRequest != null && StrUtil.isNotBlank(pictureUploadRequest.getPicName())) {
            picName = pictureUploadRequest.getPicName();
        }
        picture.setName(picName);
        picture.setPicSize(uploadPictureResult.getPicSize());
        picture.setPicWidth(uploadPictureResult.getPicWidth());
        picture.setPicHeight(uploadPictureResult.getPicHeight());
        picture.setPicScale(uploadPictureResult.getPicScale());
        picture.setPicFormat(uploadPictureResult.getPicFormat());
        picture.setUserId(loginUser.getId());
        // 操作数据库
        // 如果 pictureId 不为空，表示更新，否则是新增
        if (pictureId != null) {
            // 如果是更新，需要补充 id 和编辑时间
            picture.setId(pictureId);
            picture.setEditTime(new Date());
        }

        // 插入数据
        boolean result = pictureRepository.saveOrUpdate(picture);
        return PictureVO.objToVo(picture);
    }

    @Override
    public Page<PictureVO> listPictureVOByPage(PictureQueryRequest pictureQueryRequest, HttpServletRequest request) {
        long current = pictureQueryRequest.getCurrent();
        long size = pictureQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);

        User loginUser;
        try{
            loginUser = userFeignClient.getUserFromRequest(request);
        }catch (Exception e){
            loginUser = null;
        }

        Page<PictureVO> pictureVOPage = queryCachePage(pictureQueryRequest, request);

        if (pictureVOPage != null) {
            // 返回结果
            return pictureVOPage;
        }else{
            List<PictureVO> pictureVOList = null;
            String lockStr = CacheUtils.getHexLockString(pictureQueryRequest);
            Object lock = lockMap.computeIfAbsent(lockStr, key -> new Object());
            synchronized (lock) {
                pictureVOPage = queryCachePage(pictureQueryRequest, request);
                if (pictureVOPage == null) {
                    try {
                        // 2.1 如果缓存没有命中，直接从数据库里查询
                        Page<Picture> picturePage = page(new Page<>(current, size),
                                getQueryWrapper(pictureQueryRequest));
                        pictureVOPage = getPictureVOPage(picturePage);
                        String queryKey = PictureCacheConstant.getPictureQueryCacheKey(pictureQueryRequest);
                        // 2.2 写入 Redis
                        String cacheValue = JSONUtil.toJsonStr(pictureVOPage);
                        cacheManager.putValueToCache(queryKey, cacheValue);
                    } catch (Exception e) {
                        throw new BusinessException(ErrorCode.PARAMS_ERROR);
                    } finally {
                        // 防止内存泄漏
                        lockMap.remove(lockStr);
                    }
                }else {
                    // 防止内存泄漏
                    lockMap.remove(lockStr);
                }
                return pictureVOPage;
            }
        }
    }

    private Page<PictureVO> queryCachePage(PictureQueryRequest pictureQueryRequest, HttpServletRequest request){
        // 1 先从缓存中查图片的数据
        String queryKey = PictureCacheConstant.getPictureQueryCacheKey(pictureQueryRequest);
        Object queryValue = cacheManager.getValueCache(queryKey);
        if(queryValue == null){
            return null;
        }
        Page<PictureVO> pictureVOPage = null;
        List<PictureVO> pictureVOList = null;
        // 1.2 如果缓存命中，根据图片的数据，获取图片点赞数量
        pictureVOPage = JSONUtil.toBean(
                (String) queryValue,
                new TypeReference<Page<PictureVO>>() {}, // 指定完整泛型结构
                false // 是否忽略转换错误
        );
        pictureVOPage.setRecords(pictureVOList);
        // 1.3 返回结果
        return pictureVOPage;
    }

    private QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest) {
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        if (pictureQueryRequest == null) {
            return queryWrapper;
        }
        // 从对象中取值
        Long id = pictureQueryRequest.getId();
        String name = pictureQueryRequest.getName();
        String category = pictureQueryRequest.getCategory();
        List<String> tags = pictureQueryRequest.getTags();
        Long picSize = pictureQueryRequest.getPicSize();
        Integer picWidth = pictureQueryRequest.getPicWidth();
        Integer picHeight = pictureQueryRequest.getPicHeight();
        Double picScale = pictureQueryRequest.getPicScale();
        String picFormat = pictureQueryRequest.getPicFormat();
        String searchText = pictureQueryRequest.getSearchText();
        Long userId = pictureQueryRequest.getUserId();
        String sortField = pictureQueryRequest.getSortField();
        String sortOrder = pictureQueryRequest.getSortOrder();
        if (StrUtil.isNotBlank(searchText)) {
            queryWrapper.and(
                    qw -> qw.like("name", searchText)
            );
        }
        queryWrapper.eq(ObjUtil.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjUtil.isNotEmpty(userId), "userId", userId);
        queryWrapper.like(StrUtil.isNotBlank(name), "name", name);
        queryWrapper.like(StrUtil.isNotBlank(picFormat), "picFormat", picFormat);
        queryWrapper.eq(StrUtil.isNotBlank(category), "category", category);
        queryWrapper.eq(ObjUtil.isNotEmpty(picWidth), "picWidth", picWidth);
        queryWrapper.eq(ObjUtil.isNotEmpty(picHeight), "picHeight", picHeight);
        queryWrapper.eq(ObjUtil.isNotEmpty(picSize), "picSize", picSize);
        queryWrapper.eq(ObjUtil.isNotEmpty(picScale), "picScale", picScale);
        // JSON 数组查询
        if (CollUtil.isNotEmpty(tags)) {
            /* and (tag like "%\"Java\"%" and like "%\"Python\"%") */
            for (String tag : tags) {
                queryWrapper.like("tags", "\"" + tag + "\"");
            }
        }
        // 排序
        queryWrapper.orderBy(StrUtil.isNotEmpty(sortField), sortOrder.equals("ascend"), sortField);
        return queryWrapper;
    }

    private Page<PictureVO> getPictureVOPage(Page<Picture> picturePage) {
        List<Picture> pictureList = picturePage.getRecords();
        Page<PictureVO> pictureVOPage = new Page<>(picturePage.getCurrent(), picturePage.getSize(), picturePage.getTotal());
        if (CollUtil.isEmpty(pictureList)) {
            return pictureVOPage;
        }
        // 对象列表 => 封装对象列表
        List<PictureVO> pictureVOList = pictureList.stream()
                .map(PictureVO::objToVo)
                .collect(Collectors.toList());
        // 1. 关联查询用户信息
        // 1,2,3,4
        Set<Long> userIdSet = pictureList.stream().map(Picture::getUserId).collect(Collectors.toSet());
        // 1 => user1, 2 => user2
        UserListVO userListVO = userFeignClient.listByIds(userIdSet);
        List<User> userList = userListVO.getUserList(userListVO.getUserListJson());
        Map<Long, List<User>> userIdUserListMap = userList.stream()
                .collect(Collectors.groupingBy(User::getId));
        // 2. 填充信息
        pictureVOList.forEach(pictureVO -> {
            Long userId = pictureVO.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).getFirst();
            }
            pictureVO.setUser(userFeignClient.getUserVO(user));
        });
        pictureVOPage.setRecords(pictureVOList);
        return pictureVOPage;
    }
}
