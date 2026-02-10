package com.tech.novagraphbackendmodel.dto.user;

import com.tech.novagraphbackendcommon.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class FavoriteFolderQueryRequest extends PageRequest {
    private Long userId;
}
