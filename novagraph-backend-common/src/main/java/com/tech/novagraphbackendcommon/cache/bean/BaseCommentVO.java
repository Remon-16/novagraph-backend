package com.tech.novagraphbackendcommon.cache.bean;

import lombok.Data;

import java.util.Date;

@Data
public class BaseCommentVO {
    /**
     * 评论ID
     */
    private Long id;
    /**
     * 创建时间
     */
    private Date createTime;

}
