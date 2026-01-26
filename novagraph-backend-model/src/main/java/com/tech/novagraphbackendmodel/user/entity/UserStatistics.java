package com.tech.novagraphbackendmodel.user.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 用户统计数据
 * @TableName user_statistics
 */
@TableName(value ="user_statistics")
@Data
public class UserStatistics implements Serializable {
    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 用户 id
     */
    private Long userId;

    /**
     * 积分余额
     */
    private Long userScore;

    /**
     * 点赞数量
     */
    private Long thumbCount;

    /**
     * 收藏数量
     */
    private Long favoriteCount;

    /**
     * 关注数量
     */
    private Long followingCount;

    /**
     * 粉丝数量
     */
    private Long followerCount;

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