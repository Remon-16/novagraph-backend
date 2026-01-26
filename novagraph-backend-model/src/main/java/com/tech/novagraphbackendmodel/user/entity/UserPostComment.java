package com.tech.novagraphbackendmodel.user.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 动态评论表
 * @TableName user_post_comment
 */
@TableName(value ="user_post_comment")
@Data
public class UserPostComment implements Serializable {
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
     * 动态 id
     */
    private Long postId;

    /**
     * 目标 id 为空代表是直接评论在剧本上，不为空说明是多级评论
     */
    private Long targetId;

    /**
     * 二级目标评论Id
     */
    private Long secondTargetId;

    /**
     * 评论内容
     */
    private String content;

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