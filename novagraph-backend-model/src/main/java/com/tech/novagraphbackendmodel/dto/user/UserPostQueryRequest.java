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
    /**
     * 目标用户 id 比如 查询某一个用户动态主页
     */
    private Long targetUserId;
    /**
     * 当前登录用户 id 查询的是关注用户的动态
     */
    private Long loginUserId;

}
