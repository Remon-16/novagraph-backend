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

    /*
     * 开始编辑时间
     */
    private Date startEditTime;

    /*
     * 结束编辑时间
     */
    private Date endEditTime;

}
