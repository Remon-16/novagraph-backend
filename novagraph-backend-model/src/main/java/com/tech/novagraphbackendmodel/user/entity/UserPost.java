package com.tech.novagraphbackendmodel.user.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 用户动态表
 * @TableName user_post
 */
@TableName(value ="user_post")
@Data
public class UserPost implements Serializable {
    /**
     * 
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 用户 id
     */
    private Long userId;

    /**
     * 文本内容
     */
    private String content;

    /**
     * 类型：text-文字，screenplay-剧本，post-动态...
     */
    private String postType;

    /**
     * 引用 id
     */
    private Long quotedId;

    /**
     * 可见性：1-公开，2-私密...
     */
    private Integer visibility;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 编辑时间
     */
    private Date editTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 是否删除
     */
    @TableLogic
    private Integer isDelete;

    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}