/**
 * @(#) SynchroData.java 1.0 2018-01-19
 * Copyright (c) 2018, YUNXI. All rights reserved.
 * YUNXI PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.example.eslianjietest1.service;

import com.example.eslianjietest1.db.DbHelper;
import com.example.eslianjietest1.mapper.IndexBuilderConfigMapper;
import com.example.eslianjietest1.vo.BatchDto;
import com.example.eslianjietest1.vo.IndexBuilderConfigEo;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service("synchroDataService")
public class SynchroDataServiceImpl implements ISynchroDataService {

    private static Logger logger = LoggerFactory.getLogger(SynchroDataServiceImpl.class);

    @Resource
    private DbHelper dbHelper;

    /*@Resource
    private IndexBuilderConfigDas indexBuilderConfigDas;*/

    @Resource
    private IIndexService indexService;
    @Resource
    private IndexBuilderConfigMapper indexBuilderConfigMapper;


    @Override
    public void syncData() throws Exception {
    }

    @Override
    @Async
    public void syncData(IndexBuilderConfigEo configEo) throws Exception {
        syncData(configEo, false);
    }

    @Override
    @Async
    public void syncData(IndexBuilderConfigEo configEo, Boolean syncIndex) throws Exception {
        Long count = dbHelper.getTableCount(configEo.getSourceDb(), configEo.getSourceTable());
        logger.info("库：{}，表：{}，共{} 行记录", configEo.getSourceDb(),configEo.getSourceTable(), count);
        Long maxId = 0L;
        Long pages = DbHelper.getPages(count, DbHelper.PAGE_SIZE);
        logger.info("当前任务需要执行总页数为：{}", pages);
        for (long i = 1; i <= pages; i++) {
            logger.info("当前任务需要执行页码为：{}, maxId:{}", i, maxId);
//            maxId = doSync(maxId, configEo, syncIndex);
            maxId = doBatchSync(maxId, configEo);
        }
    }

    private Long doSync(Long maxId, IndexBuilderConfigEo configEo, Boolean syncIndex) throws Exception {
        List<Map<String, Object>> list  = dbHelper.getPageRows(maxId, configEo.getSourceDb(), configEo.getSourceTable());
        if(CollectionUtils.isNotEmpty(list)) {
            maxId = convertToLong(list.get(list.size()-1).get("id"));
            updateIndex(configEo, syncIndex, list);
        }
        return maxId;
    }


    private Long doBatchSync(Long maxId, IndexBuilderConfigEo configEo) throws Exception {
        List<Map<String, Object>> sourceDataList  = dbHelper.getPageRows(maxId, configEo.getSourceDb(), configEo.getSourceTable());
        if(CollectionUtils.isNotEmpty(sourceDataList)) {
            maxId = convertToLong(sourceDataList.get(sourceDataList.size()-1).get("id"));
            List<BatchDto> batchDtos = new ArrayList<>();
            for(Map<String, Object> sourceData:sourceDataList){
                BatchDto batchDto = new BatchDto();
                batchDto.setSource(sourceData);
                String docId = convertToString(sourceData.get(configEo.getPrimaryKey()));
                batchDto.setDocId(docId);
                if(StringUtils.isNotEmpty(configEo.getParentId()) && !"0".equals(configEo.getParentId())){
                    String parentId = convertToString(sourceData.get(configEo.getParentPrimaryKey()));
                    batchDto.setParentId(parentId);
                    batchDto.setRoutingId(parentId);
                }
                batchDtos.add(batchDto);
            }
            indexService.batchUpsertDoc(configEo.getIndexName(),configEo.getSourceTable(),batchDtos);
        }
        return maxId;
    }
    public  String convertToString(Object param) {
        return param == null ? null : param.toString().trim();
    }
    public  Long convertToLong(Object param) throws Exception {
        return (Long)numberConverter(param, Long.class);
    }
    private  Object numberConverter(Object object, Class type) {
        if (object != null && !org.apache.commons.lang3.StringUtils.isBlank(object.toString())) {
            if (Number.class.isAssignableFrom(type)) {
                String param = object.toString();
                if (!NumberUtils.isNumber(param)) {
                    throw new RuntimeException();
                } else {
                    logger.info("param {}",param);
                    return Double.class.isAssignableFrom(type) ? Long.parseLong(param) : Long.parseLong(param);
                }
            } else {
                return object;
            }
        } else {
            return null;
        }
    }

    @Override
    @Async
    public void updateIndex(IndexBuilderConfigEo configEo, Boolean syncIndex, List<Map<String, Object>> list) {
        logger.info("需要更新数据为{}, 总数：{}", configEo.getSourceTable(), list.size());
        for (Map<String, Object> rowData : list) {
            try {
                if (syncIndex) {
                    updateIndex(rowData, configEo);
                } else {
                    updateIndex(rowData, configEo.getSourceTable());
                }
            } catch (Exception e) {
                logger.warn(" table is " + configEo.getSourceTable() + " rowData is " + rowData, e);
            }
        }
    }

    private void updateIndex(Map<String, Object> sourceData, String table) {
        /*List<IndexBuilderConfigEo> eoList = indexBuilderConfigDas.selectBySourceTable(table);*/
        List<IndexBuilderConfigEo> eoList = indexBuilderConfigMapper.selectConfigEo(table);

        if (CollectionUtils.isEmpty(eoList)) {
            logger.info("不需要更新索引");
        }

        for (IndexBuilderConfigEo eo : eoList) {
            updateIndex(sourceData, eo);
        }
    }

    private void updateIndex(Map<String, Object> sourceData, IndexBuilderConfigEo eo) {
        Object id = sourceData.get(eo.getPrimaryKey());
        Object parentId = sourceData.get(eo.getParentPrimaryKey());

        indexService.updateDoc(eo, convertToString(id), convertToString(parentId), sourceData);
    }

    @Override
    public void syncData(String indexName ,String tableName) throws Exception {
        logger.info("按表同步index,indexName:{},tableName:{}",indexName,tableName);
        // deleteData(tableName);
        /*List<IndexBuilderConfigEo> indexBuilderConfigEos = indexBuilderConfigDas.selectByIndexAndTable(indexName,tableName);*/
        List<IndexBuilderConfigEo> indexBuilderConfigEos = indexBuilderConfigMapper.selectConfigEoByIndexAndTable(indexName, tableName);
        if (CollectionUtils.isEmpty(indexBuilderConfigEos)) {
            throw new RuntimeException("该表没有配置到索引同步");
        }
        for(IndexBuilderConfigEo eo:indexBuilderConfigEos){
            syncData(eo, false);
        }
    }

    @Override
    @Async
    public void syncDataByindex(String index) {
        logger.info("------------按index:{} 同步数据-------", index);
        /*IndexBuilderConfigEo queryEo = new IndexBuilderConfigEo();
        queryEo.setIndexName(index);
        List<IndexBuilderConfigEo> indexBuilderConfigEos = indexBuilderConfigDas.select(queryEo);*/
        List<IndexBuilderConfigEo> indexBuilderConfigEos = indexBuilderConfigMapper.selectConfigEoByIndex(index);
        for (IndexBuilderConfigEo eo : indexBuilderConfigEos) {
            try {
                syncData(eo, true);
            } catch (Exception e) {
                logger.error("同步索引出现异常：", e);
            }
        }
    }
}
