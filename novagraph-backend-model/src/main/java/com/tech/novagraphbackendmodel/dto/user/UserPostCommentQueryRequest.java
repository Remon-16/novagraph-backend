package com.tech.novagraphbackendmodel.dto.user;

import com.tech.novagraphbackendcommon.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class UserPostCommentQueryRequest extends PageRequest {
    /**
     * 评论 ID
     */
    Long postId;
    /**
     * 根评论的 ID
     */
    Long targetId;
}
