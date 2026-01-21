package com.tech.novagraphbackendcommon.cache.bean;

import lombok.Data;

@Data
public class CommentQueryBean {
    private String sortedKey;
    private String sortedTotalKey;
    private String keyHead;
    private String order;
    private Long page;
    private Long size;
    private Class VOClass;
}
