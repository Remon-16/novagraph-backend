package com.tech.novagraphbackendmodel.dto.graph;

import lombok.Data;

@Data
public class ScreenplaySectionUpdateRequest {

    private Long userId;

    private Long id;

    /**
     * 章节名称
     */
    private String sectionName;

    /**
     * 内容内容
     */
    private String content;

    /**
     * 剧本Id
     */
    private Long screenplayId;
}
