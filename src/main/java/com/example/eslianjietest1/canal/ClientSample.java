package com.example.eslianjietest1.canal;

import com.alibaba.otter.canal.client.*;
import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.common.utils.AddressUtils;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.alibaba.otter.canal.protocol.CanalEntry.*;
import com.alibaba.otter.canal.protocol.Message;
import com.example.eslianjietest1.mapper.IndexBuilderConfigMapper;
import com.example.eslianjietest1.service.IndexServiceImpl;
import com.example.eslianjietest1.vo.IndexBuilderConfigEo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ClientSample {
    @Resource
    private IndexBuilderConfigMapper indexBuilderConfigMapper;
    IndexBuilderConfigEo configEo;
    @Autowired
    IndexServiceImpl indexService;

    public void cannal() {

        // 创建链接
        CanalConnector connector = CanalConnectors.newSingleConnector(new InetSocketAddress(AddressUtils.getHostIp(),
                11111), "example", "", "");
        int batchSize = 1000;
        try {
            connector.connect();
            connector.subscribe(".*\\..*");
            connector.rollback();
            while (true) {
                Message message = connector.getWithoutAck(batchSize); // 获取指定数量的数据
                long batchId = message.getId();
                int size = message.getEntries().size();
                if (batchId == -1 || size == 0) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    printEntry(message.getEntries());
                }

                connector.ack(batchId); // 提交确认
                // connector.rollback(batchId); // 处理失败, 回滚数据
            }

        } finally {
            connector.disconnect();
        }
    }

    private void printEntry(List<Entry> entrys) {
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
            if (eventType == EventType.QUERY || rowChage.getIsDdl()) {

            }
            String tableName = entry.getHeader().getTableName();
            List<IndexBuilderConfigEo> indexBuilderConfigEos = indexBuilderConfigMapper.selectConfigEo(tableName);
            configEo = indexBuilderConfigEos.get(0);
            Map<String, Object> map = new HashMap<>();
            String parentId = null;
            String id = null;
            for (CanalEntry.RowData rowData : rowChage.getRowDatasList()) {
                if (eventType == EventType.INSERT) {
                    List<Column> afterColumnsList = rowData.getAfterColumnsList();

                    for (CanalEntry.Column column : afterColumnsList) {
                        String name = column.getName();
                        String value = column.getValue();
                        if (name.equals(configEo.getPrimaryKey())) {
                            id = value;
                        }
                        if (name.equals(configEo.getParentPrimaryKey())) {
                            parentId = value;
                        }
                        int sqlType = column.getSqlType();
                        boolean updated = column.getUpdated();
                        System.out.println(tableName + name + value + sqlType + updated);
                        if (needSetNull(column.getMysqlType()) && "".equals(column.getValue())) {
                            map.put(name, null);
                        } else {
                            map.put(name, value);
                        }
                    }
                    System.out.println(parentId + id + map.toString());
                    indexService.insertDoc(configEo, id, parentId, map);


                }
                if (eventType == EventType.UPDATE) {
                    map = null;
                    List<Column> afterColumnsList = rowData.getAfterColumnsList();
                    for (CanalEntry.Column column : afterColumnsList) {
                        String name = column.getName();
                        String value = column.getValue();
                        if (name.equals(configEo.getPrimaryKey())) {
                            id = null;
                            id = value;
                        }
                        if (name.equals(configEo.getParentPrimaryKey())) {
                            parentId = null;
                            parentId = value;
                        }
                        if (needSetNull(column.getMysqlType()) && "".equals(column.getValue())) {
                            map.put(name, null);
                        } else {
                            map.put(name, value);
                        }
                        int sqlType = column.getSqlType();
                        boolean updated = column.getUpdated();
                        System.out.println("tableName-" + tableName + "--name-" + name + "--value-" + value + "--sqlType-" + sqlType + "--updated-" + updated);
                    }
                    indexService.updateDoc(configEo,id,parentId,map);
                }
                if (eventType == EventType.DELETE) {
                    List<Column> beforeColumnsList = rowData.getBeforeColumnsList();
                    map = null;
                    for (CanalEntry.Column column : beforeColumnsList) {
                        String value = column.getValue();
                        String name = column.getName();
                        if (name.equals(configEo.getPrimaryKey())) {
                            id = null;
                            id = value;
                        }
                        if (name.equals(configEo.getParentPrimaryKey())) {
                            parentId = null;
                            parentId = value;
                        }
                        if (needSetNull(column.getMysqlType()) && "".equals(column.getValue())) {
                            map.put(name, null);
                        } else {
                            map.put(name, value);
                        }
                    }
                    indexService.deleteDoc(configEo,id,parentId,map);
                }
            }

        }
    }

    public static boolean needSetNull(String sqlType) {
        if ("date".equals(sqlType) || "datetime".equals(sqlType)
                || sqlType.indexOf("tinyint") != -1 || sqlType.indexOf("bigint") != -1
                || sqlType.indexOf("float") != -1) {
            return true;
        }
        return false;
    }

}
