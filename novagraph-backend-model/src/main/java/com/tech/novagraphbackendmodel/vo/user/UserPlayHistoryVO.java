package com.tech.novagraphbackendmodel.vo.user;

import lombok.Data;

import java.util.Date;

@Data
public class UserPlayHistoryVO {
    private Long id;

    /**
     * 用户 id
     */
    private Long userId;

    /**
     * 剧本 id
     */
    private Long screenplayId;

    /**
     * 创建时间
     */
    private Date createTime;
}
