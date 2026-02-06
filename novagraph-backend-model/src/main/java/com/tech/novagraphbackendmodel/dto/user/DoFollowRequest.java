package com.tech.novagraphbackendmodel.dto.user;

import lombok.Data;

@Data
public class DoFollowRequest {
    private Long targetUserId;
}
