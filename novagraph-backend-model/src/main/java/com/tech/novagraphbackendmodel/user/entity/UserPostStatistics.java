package com.tech.novagraphbackendmodel.user.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 动态统计数据
 * @TableName user_post_statistics
 */
@TableName(value ="user_post_statistics")
@Data
public class UserPostStatistics implements Serializable {
    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 动态 id
     */
    private Long postId;

    /**
     * 点赞数量
     */
    private Long thumbCount;

    /**
     * 评论数量
     */
    private Long commentCount;

    /**
     * 分享数量
     */
    private Long shareCount;

    /**
     * 引用数量
     */
    private Long quotedCount;

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
    @TableLogic
    private Integer isDelete;

    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}