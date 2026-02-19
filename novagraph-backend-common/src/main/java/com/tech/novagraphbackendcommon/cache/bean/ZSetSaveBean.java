package com.tech.novagraphbackendcommon.cache.bean;

import lombok.Data;

@Data
public class ZSetSaveBean {
    private String valueKeyHead;
    private String sortedKey;
    private String sortedTotalKey;
    private Long total;
}
