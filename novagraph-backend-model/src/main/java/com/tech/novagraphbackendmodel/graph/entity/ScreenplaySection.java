package com.tech.novagraphbackendmodel.graph.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 剧本章节
 * @TableName screenplay_section
 */
@TableName(value ="screenplay_section")
@Data
public class ScreenplaySection {
    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 章节名称
     */
    private String sectionName;

    /**
     * 内容
     */
    private String content;

    /**
     * 剧本Id
     */
    private Long screenplayId;

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
     * 更新时间
     */
    private Date updateTime;

    /**
     * 是否删除
     */
    private Integer isDelete;
}