package com.tech.novagraphbackendmodel.graph.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 剧本统计数据
 * @TableName screenplay_statistics
 */
@TableName(value ="screenplay_statistics")
@Data
public class ScreenplayStatistics implements Serializable {
    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 剧本 id
     */
    private Long screenplayId;

    /**
     * 播放数量
     */
    private Long playCount;

    /**
     * 点赞数量
     */
    private Long thumbCount;

    /**
     * 收藏数量
     */
    private Long favoriteCount;

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