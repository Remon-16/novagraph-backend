package com.tech.novagraphbackendmodel.dto.graph;

import com.tech.novagraphbackendcommon.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=true)
public class ScreenplaySectionQueryRequest extends PageRequest {

    private Long userId;

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
