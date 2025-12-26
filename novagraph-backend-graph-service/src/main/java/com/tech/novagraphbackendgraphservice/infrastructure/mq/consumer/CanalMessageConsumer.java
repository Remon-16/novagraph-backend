package com.tech.novagraphbackendgraphservice.infrastructure.mq.consumer;

import cn.hutool.json.JSONUtil;
import com.alibaba.otter.canal.client.CanalMessageDeserializer;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.alibaba.otter.canal.protocol.Message;
import com.tech.novagraphbackendcommon.common.CanalHandleVO;
import com.tech.novagraphbackendcommon.exception.BusinessException;
import com.tech.novagraphbackendcommon.exception.ErrorCode;
import com.tech.novagraphbackendgraphservice.domain.screenplay.service.ScreenplayCommentDomainService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RocketMQMessageListener(topic = "canalNGTopic", consumerGroup = "canalNGroup")
public class CanalMessageConsumer implements RocketMQListener<MessageExt> {

    private static final String SCREENPLAY_COMMENT = "screenplay_comment";

    @Resource
    private ScreenplayCommentDomainService screenplayCommentDomainService;

    @Override
    public void onMessage(MessageExt message){
        Message msg = CanalMessageDeserializer.deserializer(message.getBody());
        List<CanalEntry.Entry> entries = msg.getEntries();
        Map<String, List<CanalHandleVO>> canalHandleMap = this.handleEntryList(entries);
        this.cacheHandle(canalHandleMap);
    }

    /**
     * 根据变动结果处理缓存
     */
    private void cacheHandle(Map<String, List<CanalHandleVO>> canalHandleMap){
        canalHandleMap.forEach((tableName, canalHandleVOList) -> {
            if(SCREENPLAY_COMMENT.equals(tableName)){
                screenplayCommentDomainService.canalHandleScreenplayComment(canalHandleVOList);
            }else{
                log.error("未监听表 tableName: {}", tableName);
            }
        });
    }

    /**
     * 处理变动的实体
     */
    private Map<String, List<CanalHandleVO>> handleEntryList(List<CanalEntry.Entry> entryList){
        Map<String, List<CanalHandleVO>> resMap = new HashMap<>();
        for (CanalEntry.Entry entry : entryList) {
            if (entry.getEntryType() == CanalEntry.EntryType.TRANSACTIONBEGIN || entry.getEntryType() == CanalEntry.EntryType.TRANSACTIONEND) {
                continue;
            }

            CanalEntry.RowChange rowChage = null;
            try {
                rowChage = CanalEntry.RowChange.parseFrom(entry.getStoreValue());
            } catch (Exception e) {
                log.error(e.fillInStackTrace().toString());
                throw new BusinessException(ErrorCode.SYSTEM_ERROR,
                        "ERROR ## parser of eromanga-event has an error , data:" + entry.toString());
            }

            CanalEntry.EventType eventType = rowChage.getEventType();
            String tableName = entry.getHeader().getTableName();
            List<CanalHandleVO> canalHandleVOList = resMap.getOrDefault(tableName, new ArrayList<>());
            String jsonStr = this.handleRow(rowChage.getRowDatasList(), eventType);
            CanalHandleVO canalHandleVO = new CanalHandleVO();
            canalHandleVO.setEventType(eventType);
            canalHandleVO.setTableName(entry.getHeader().getTableName());
            canalHandleVO.setJsonDataStr(jsonStr);
            canalHandleVOList.add(canalHandleVO);
            resMap.put(tableName, canalHandleVOList);
        }
        return resMap;
    }

    /**
     * 处理实体里的数据行
     */
    private String handleRow(List<CanalEntry.RowData> rowDatasList, CanalEntry.EventType eventType){
        Map<String, String> map = new HashMap<>();
        for (CanalEntry.RowData rowData : rowDatasList) {
            if (eventType == CanalEntry.EventType.DELETE) {
                this.handleColumn(rowData.getBeforeColumnsList(), map);
            } else if (eventType == CanalEntry.EventType.INSERT) {
                this.handleColumn(rowData.getAfterColumnsList(), map);
            } else {
                this.handleColumn(rowData.getAfterColumnsList(), map);
            }
        }
        return JSONUtil.toJsonStr(map);
    }

    /**
     * 处理数据行中的列
     */
    private void handleColumn(List<CanalEntry.Column> columns, Map<String, String> map){
        for (CanalEntry.Column column : columns) {
            if(!column.getValue().isEmpty()){
                map.put(column.getName(), column.getValue());
            }
        }
    }


    private void printEntry(List<CanalEntry.Entry> entrys) {
        for (CanalEntry.Entry entry : entrys) {
            if (entry.getEntryType() == CanalEntry.EntryType.TRANSACTIONBEGIN || entry.getEntryType() == CanalEntry.EntryType.TRANSACTIONEND) {
                continue;
            }

            CanalEntry.RowChange rowChage = null;
            try {
                rowChage = CanalEntry.RowChange.parseFrom(entry.getStoreValue());
            } catch (Exception e) {
                throw new RuntimeException("ERROR ## parser of eromanga-event has an error , data:" + entry.toString(),
                        e);
            }

            CanalEntry.EventType eventType = rowChage.getEventType();
            System.out.println(String.format("================&gt; binlog[%s:%s] , name[%s,%s] , eventType : %s",
                    entry.getHeader().getLogfileName(), entry.getHeader().getLogfileOffset(),
                    entry.getHeader().getSchemaName(), entry.getHeader().getTableName(),
                    eventType));

            for (CanalEntry.RowData rowData : rowChage.getRowDatasList()) {
                if (eventType == CanalEntry.EventType.DELETE) {
                    this.printColumn(rowData.getBeforeColumnsList());
                } else if (eventType == CanalEntry.EventType.INSERT) {
                    this.printColumn(rowData.getAfterColumnsList());
                } else {
                    System.out.println("-------&gt; before");
                    List<CanalEntry.Column> beforeColumnsList = rowData.getBeforeColumnsList();
                    this.printColumn(beforeColumnsList);
                    System.out.println("-------&gt; after");
                    this.printColumn(rowData.getAfterColumnsList());
                }
            }
        }
    }

    private void printColumn(List<CanalEntry.Column> columns) {
        for (CanalEntry.Column column : columns) {
            System.out.println(column.getName() + " : " + column.getValue() + "    update=" + column.getUpdated());
        }
    }
}