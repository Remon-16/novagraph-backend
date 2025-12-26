package com.tech.novagraphbackendcommon.common;

import com.alibaba.otter.canal.protocol.CanalEntry;
import lombok.Data;

@Data
public class CanalHandleVO {
    private String tableName;
    private CanalEntry.EventType eventType;
    private String jsonDataStr;
}

