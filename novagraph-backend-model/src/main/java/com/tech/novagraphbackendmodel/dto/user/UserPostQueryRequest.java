package com.tech.novagraphbackendmodel.dto.user;

import com.tech.novagraphbackendcommon.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class UserPostQueryRequest extends PageRequest {
    /**
     * 用户 id
     */
    private List<Long> userIdList;

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
