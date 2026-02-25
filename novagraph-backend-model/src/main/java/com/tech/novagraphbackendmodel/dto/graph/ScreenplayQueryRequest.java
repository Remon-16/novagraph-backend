package com.tech.novagraphbackendmodel.dto.graph;

import com.tech.novagraphbackendcommon.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper=true)
public class ScreenplayQueryRequest extends PageRequest {

    private Long userId;

    private Long id;
    /**
     * 剧本名称
     */
    private String name;

    /**
     * 简介
     */
    private String introduction;

    /**
     * 分类
     */
    private String category;

    /**
     * 标签
     */
    private List<String> tags;

    /**
     * 可见性：1-公开，2-私密...
     */
    private Integer visibility;

    /**
     * 审核状态：0-待审核; 1-通过; 2-拒绝
     */
    private Integer reviewStatus;

    /*
     * 开始编辑时间
     */
    private Date startEditTime;

    /*
     * 结束编辑时间
     */
    private Date endEditTime;

}
