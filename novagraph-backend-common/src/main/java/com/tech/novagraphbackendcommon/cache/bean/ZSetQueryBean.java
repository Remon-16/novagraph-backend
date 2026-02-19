package com.tech.novagraphbackendcommon.cache.bean;

import lombok.Data;

@Data
public class ZSetQueryBean {
    private String sortedKey;
    private String sortedTotalKey;
    private String valueKeyHead;
    private String order;
    private Long page;
    private Long size;
    private Class VOClass;
}
