package com.tech.novagraphbackendcommon.common;

import lombok.Data;

import java.util.Map;

@Data
public class SortedCacheResult {
    private Long total;
    Map<Object, Object> valueMap;
}