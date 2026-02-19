package com.tech.novagraphbackendmodel.dto.user;

import lombok.Data;

@Data
public class UserPostAddRequest {
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
}
