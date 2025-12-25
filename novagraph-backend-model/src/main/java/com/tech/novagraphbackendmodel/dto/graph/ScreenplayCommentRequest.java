package com.tech.novagraphbackendmodel.dto.graph;

import lombok.Data;

@Data
public class ScreenplayCommentRequest {
    /**
     * id 不为空是编辑
     */
    private Long id;

    /**
     * 用户 id
     */
    private Long userId;

    /**
     * 图片 id
     */
    private Long screenplayId;

    /**
     * 目标 id 为空代表是直接评论在剧本上，不为空说明是多级评论
     */
    private Long targetId;

    /**
     * 二级目标 id 为空代表是二级评论，不为空说明是三级评论
     */
    private Long secondTargetId;

    /**
     * 评论内容
     */
    private String content;
}
