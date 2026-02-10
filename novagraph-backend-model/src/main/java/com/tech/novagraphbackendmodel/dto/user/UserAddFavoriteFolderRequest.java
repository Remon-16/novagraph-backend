package com.tech.novagraphbackendmodel.dto.user;

import lombok.Data;

@Data
public class UserAddFavoriteFolderRequest {
    private Long userId;

    private String folderName;
}
