package com.tech.novagraphbackendmodel.vo.graph;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class ScreenplayContentVO {
    /**
     * 剧本 id
     */
    private Long screenplayId;

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
     * 标签（JSON 数组）
     */
    private List<String> tags;

    /**
     * 剧本封面连接
     */
    private String cover;

    /**
     * 用户 id
     */
    private Long userId;

    /**
     * 编辑时间
     */
    private Date editTime;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 剧本章节
     */
    private List<ScreenplaySectionVO> sectionVOList;
}
