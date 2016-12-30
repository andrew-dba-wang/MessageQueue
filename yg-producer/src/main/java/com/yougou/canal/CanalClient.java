package com.yougou.canal;

import com.alibaba.fastjson.JSONException;
import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.client.CanalConnectors;
import com.alibaba.otter.canal.protocol.CanalEntry.*;
import com.alibaba.otter.canal.protocol.Message;
import org.codehaus.jettison.json.JSONObject;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created by liuti on 2016/12/29.
 */
public class CanalClient {
    public static void main(String args[]) throws  Exception{
        // 创建链接
        CanalConnector connector =CanalConnectors.newClusterConnector(Constants.zk_Cluster,"example", "", "");
        //CanalConnector connector = CanalConnectors.newSingleConnector(new InetSocketAddress("10.10.10.122",10010), "example", "", "");

        int batchSize = 1000;
        int emptyCount = 0;
        try {
            connector.connect();
            connector.subscribe(".*\\..*");
            connector.rollback();
            int totalEmtryCount = 120;
            while (emptyCount < totalEmtryCount) {
                Message message = connector.getWithoutAck(batchSize); // 获取指定数量的数据
                long batchId = message.getId();
                int size = message.getEntries().size();
                if (batchId == -1 || size == 0) {
                    emptyCount++;
                    System.out.println("empty count : " + emptyCount);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                    }
                } else {
                    emptyCount = 0;
                    printEntry(message.getEntries());
                }
                connector.ack(batchId); // 提交确认
                //connector.rollback(batchId); // 处理失败, 回滚数据
            }
            System.out.println("empty too many times, exit");
        }
        finally {
            connector.disconnect();
        }
    }

    private static void printEntry(List<Entry> entrys)  throws  Exception{
        for (Entry entry : entrys) {
            if (entry.getEntryType() == EntryType.TRANSACTIONBEGIN || entry.getEntryType() == EntryType.TRANSACTIONEND) {
                continue;
            }
            RowChange rowChage = null;
            try {
                rowChage = RowChange.parseFrom(entry.getStoreValue());
            } catch (Exception e) {
                throw new RuntimeException("ERROR ## parser of eromanga-event has an error , data:" + entry.toString(),
                        e);
            }
            EventType eventType = rowChage.getEventType();
            System.out.println(String.format("================> binlog[%s:%s] , name[%s,%s] , eventType : %s",
                    entry.getHeader().getLogfileName(), entry.getHeader().getLogfileOffset(),
                    entry.getHeader().getSchemaName(), entry.getHeader().getTableName(),
                    eventType));

            Map<String,String> headerMap=new HashMap<String,String>();
            headerMap.put("LogfileName",entry.getHeader().getLogfileName());
            headerMap.put("LogfileOffset",String.valueOf(entry.getHeader().getLogfileOffset()));
            headerMap.put("SchemaName",entry.getHeader().getSchemaName());
            headerMap.put("TableName",entry.getHeader().getTableName());
            headerMap.put("eventType",eventType.toString());

            for (RowData rowData : rowChage.getRowDatasList()) {
                if (eventType == EventType.DELETE) {
                    printColumn(rowData.getBeforeColumnsList(),headerMap);
                } else if (eventType == EventType.INSERT) {
                    printColumn(rowData.getAfterColumnsList(),headerMap);
                } else {
                    System.out.println("-------> before");
                    printColumn(rowData.getBeforeColumnsList(),headerMap);
                    System.out.println("-------> after");
                    printColumn(rowData.getAfterColumnsList(),headerMap);
                }
            }
        }
    }

    private static void printColumn(List<Column> columns) throws Exception {
        JSONObject  jsonObject = new JSONObject();
        for (Column column : columns) {
            System.out.println(column.getName() + " : " + column.getValue());
            jsonObject.put(column.getName(),column.getValue());
        }
        KafkaProducer.sendMsg(Constants.topics, UUID.randomUUID().toString(), jsonObject.toString());
    }

    private static void printColumn(List<Column> columns,Map<String,String> headerMap) throws Exception {
        JSONObject  jsonObject = new JSONObject();
        for(Map.Entry<String,String> entry : headerMap.entrySet()){
            jsonObject.put(entry.getKey(),entry.getValue());
        }
        for (Column column : columns) {
            System.out.println(column.getName() + " : " + column.getValue());
            jsonObject.put(column.getName(),column.getValue());
        }
        KafkaProducer.sendMsg(Constants.topics, UUID.randomUUID().toString(), jsonObject.toString());
    }
}
