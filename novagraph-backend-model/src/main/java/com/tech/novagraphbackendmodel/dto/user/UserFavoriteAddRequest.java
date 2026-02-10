package com.tech.novagraphbackendmodel.dto.user;

import lombok.Data;

@Data
public class UserFavoriteAddRequest {
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
