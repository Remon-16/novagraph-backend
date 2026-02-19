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
     * 动态 id
     */
    private List<Long> postIdList;

    private Long loginUserId;

}
