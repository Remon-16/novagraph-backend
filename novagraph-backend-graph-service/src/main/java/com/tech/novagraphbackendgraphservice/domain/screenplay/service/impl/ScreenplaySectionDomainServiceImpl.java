package com.tech.novagraphbackendgraphservice.domain.screenplay.service.impl;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tech.novagraphbackendcommon.exception.ErrorCode;
import com.tech.novagraphbackendcommon.exception.ThrowUtils;
import com.tech.novagraphbackendgraphservice.domain.screenplay.repository.ScreenplaySectionRepository;
import com.tech.novagraphbackendgraphservice.domain.screenplay.service.ScreenplayDomainService;
import com.tech.novagraphbackendgraphservice.domain.screenplay.service.ScreenplaySectionDomainService;
import com.tech.novagraphbackendgraphservice.infrastructure.mapper.ScreenplaySectionMapper;
import com.tech.novagraphbackendmodel.dto.graph.ScreenplaySectionAddRequest;
import com.tech.novagraphbackendmodel.dto.graph.ScreenplaySectionQueryRequest;
import com.tech.novagraphbackendmodel.dto.graph.ScreenplaySectionUpdateRequest;
import com.tech.novagraphbackendmodel.graph.entity.ScreenplaySection;
import com.tech.novagraphbackendmodel.vo.graph.ScreenplayContentVO;
import com.tech.novagraphbackendmodel.vo.graph.ScreenplaySectionVO;
import com.tech.novagraphbackendmodel.vo.graph.ScreenplayVO;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ScreenplaySectionDomainServiceImpl extends ServiceImpl<ScreenplaySectionMapper, ScreenplaySection>
        implements ScreenplaySectionDomainService {

    @Resource
    private ScreenplaySectionRepository screenplaySectionRepository;

    @Resource
    private ScreenplayDomainService screenplayDomainService;

    @Override
    public ScreenplaySection addScreenplaySection(ScreenplaySectionAddRequest screenplaySectionAddRequest) {
        ThrowUtils.throwIf(screenplaySectionAddRequest == null, ErrorCode.PARAMS_ERROR, "screenplaySection 为空");
        ScreenplaySection screenplaySection = ScreenplaySectionAddRequest.dtoToObj(screenplaySectionAddRequest);
        this.save(screenplaySection);
        return screenplaySection;
    }

    @Override
    public ScreenplaySection updateScreenplaySection(ScreenplaySectionUpdateRequest screenplaySectionUpdateRequest) {
        ThrowUtils.throwIf(screenplaySectionUpdateRequest == null, ErrorCode.PARAMS_ERROR,
                "screenplaySectionUpdateRequest 为空");
        ThrowUtils.throwIf(screenplaySectionUpdateRequest.getId() == null, ErrorCode.PARAMS_ERROR,
                "screenplaySectionUpdateRequest id 为空");

        ScreenplaySection screenplaySection = new ScreenplaySection();
        BeanUtils.copyProperties(screenplaySectionUpdateRequest, screenplaySection);
        screenplaySectionRepository.updateById(screenplaySection);
        return screenplaySection;
    }

    @Override
    public ScreenplaySectionVO queryScreenplaySectionById(Long id) {
        ThrowUtils.throwIf(id == null, ErrorCode.PARAMS_ERROR,
                "id 为空");
        ScreenplaySection screenplaySection = this.getById(id);
        return ScreenplaySectionVO.ObjToVo(screenplaySection);
    }

    @Override
    public ScreenplayContentVO queryScreenplayContent(Long id) {
        ThrowUtils.throwIf(id == null, ErrorCode.PARAMS_ERROR,
                "id 为空");
        ScreenplayVO screenplayVO = screenplayDomainService.queryScreenplayById(id);
        ScreenplayContentVO screenplayContentVO = new ScreenplayContentVO();
        BeanUtils.copyProperties(screenplayVO, screenplayContentVO);
        ScreenplaySectionQueryRequest screenplaySectionQueryRequest = new ScreenplaySectionQueryRequest();
        screenplaySectionQueryRequest.setScreenplayId(id);
        screenplaySectionQueryRequest.setSortOrder("ascend");
        List<ScreenplaySectionVO> screenplaySectionVOList = this.queryScreenplaySectionByList(screenplaySectionQueryRequest);
        screenplayContentVO.setSectionVOList(screenplaySectionVOList);
        return screenplayContentVO;
    }

    private QueryWrapper<ScreenplaySection> getQueryWrapper(ScreenplaySectionQueryRequest screenplaySectionQueryRequest){
        QueryWrapper<ScreenplaySection> queryWrapper = new QueryWrapper<>();
        if (screenplaySectionQueryRequest == null) {
            return queryWrapper;
        }
        Long userId = screenplaySectionQueryRequest.getUserId();
        Long screenplayId = screenplaySectionQueryRequest.getScreenplayId();
        String sectionName = screenplaySectionQueryRequest.getSectionName();
        String sortField = screenplaySectionQueryRequest.getSortField();
        String sortOrder = screenplaySectionQueryRequest.getSortOrder();

        queryWrapper.eq(ObjUtil.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq(ObjUtil.isNotEmpty(screenplayId), "screenplayId", screenplayId);
        queryWrapper.eq(ObjUtil.isNotEmpty(sectionName), "sectionName", sectionName);
        // 排序
        queryWrapper.orderBy(StrUtil.isNotEmpty(sortField), sortOrder.equals("ascend"), sortField);
        return queryWrapper;
    }

    @Override
    public Page<ScreenplaySectionVO> queryScreenplaySectionByPage(ScreenplaySectionQueryRequest screenplaySectionQueryRequest) {
        ThrowUtils.throwIf(screenplaySectionQueryRequest == null, ErrorCode.PARAMS_ERROR, "screenplaySectionQueryRequest 为空");
        int current = screenplaySectionQueryRequest.getCurrent();
        int pageSize = screenplaySectionQueryRequest.getPageSize();
        Page<ScreenplaySection> screenplaySectionPage = this.page(new Page<>(current, pageSize),
                this.getQueryWrapper(screenplaySectionQueryRequest));
        List<ScreenplaySection> screenplaySections = screenplaySectionPage.getRecords();
        List<ScreenplaySectionVO> screenplaySectionVOS =  screenplaySections.stream().map(ScreenplaySectionVO::ObjToVo).toList();
        Page<ScreenplaySectionVO> screenplaySectionVOPage = new Page<>();
        screenplaySectionVOPage.setRecords(screenplaySectionVOS);
        screenplaySectionVOPage.setCurrent(current);
        screenplaySectionVOPage.setSize(pageSize);
        screenplaySectionVOPage.setTotal(screenplaySectionPage.getTotal());

        return screenplaySectionVOPage;
    }

    @Override
    public List<ScreenplaySectionVO> queryScreenplaySectionByList(ScreenplaySectionQueryRequest screenplaySectionQueryRequest) {
        ThrowUtils.throwIf(screenplaySectionQueryRequest == null, ErrorCode.PARAMS_ERROR, "screenplaySectionQueryRequest 为空");
        List<ScreenplaySection> screenplaySections = this.list(this.getQueryWrapper(screenplaySectionQueryRequest));
        return screenplaySections.stream().map(ScreenplaySectionVO::ObjToVo).toList();
    }
}