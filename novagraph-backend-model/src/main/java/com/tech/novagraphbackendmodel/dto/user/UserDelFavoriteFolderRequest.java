package com.tech.novagraphbackendmodel.dto.user;

import lombok.Data;

@Data
public class UserDelFavoriteFolderRequest {
    private Long userId;

    private Long folderId;
}
