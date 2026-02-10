package com.tech.novagraphbackendmodel.dto.user;

import com.tech.novagraphbackendcommon.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class UserFavoriteQueryRequest extends PageRequest {

    private Long favoriteId;
    /**
     * 用户 id
     */
    private Long userId;

    /**
     * 收藏夹 id
     */
    private Long folderId;

    /**
     * 剧本 id
     */
    private Long screenplayId;
}
