package com.tech.novagraphbackendmodel.dto.graph;

import com.tech.novagraphbackendcommon.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class ScreenplayCommentQueryRequest extends PageRequest {
    /**
     * 剧本 ID
     */
    Long screenplayId;
    /**
     * 根评论的 ID
     */
    Long targetId;
}
