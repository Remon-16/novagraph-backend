package com.tech.novagraphbackendcommon.cache.bean;

import lombok.Data;

@Data
public class CommentSaveBean {
    private String commentKeyHead;
    private String sortedKey;
    private String sortedTotalKey;
    private Long total;
}
