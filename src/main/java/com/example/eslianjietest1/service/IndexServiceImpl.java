package com.example.eslianjietest1.service;

import com.alibaba.fastjson.JSON;
import com.example.eslianjietest1.es.ESTemplate;
import com.example.eslianjietest1.vo.BatchDto;
import com.example.eslianjietest1.vo.IndexBuilderConfigEo;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequestBuilder;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;


@Service("indexService")
public class IndexServiceImpl implements IIndexService {

    private static Logger logger = LoggerFactory.getLogger(IndexServiceImpl.class);

    @Autowired
    private ESTemplate esTemplate;

    @Override
    public void insertDoc(IndexBuilderConfigEo eo, String id, String parentId, Map<String, Object> source) {
        logger.info("配置索引. {}, 索引ID：{}", JSON.toJSONString(eo), id);
        if (IndexBuilderConfigEo.PARENT_ID.equals(eo.getParentId()) || StringUtils.isEmpty(parentId)) {
            logger.info("构建主表索引: index is {}, type is {}, docId is {}, data is {}", eo.getIndexName(),
                    eo.getSourceTable(), id, source.toString());
            esTemplate.indexDoc(eo.getIndexName(), eo.getSourceTable(), convertToString(id),
                    source);
        } else {
            logger.info("构建子表索引: index is {}, type is {}, docId is {}, parentId is {}, data is {}",
                    eo.getIndexName(), eo.getSourceTable(), id, parentId, JSON.toJSONString(source));
            esTemplate.indexDocWithRouting(eo.getIndexName(), eo.getSourceTable(),
                    convertToString(id), convertToString(parentId), source);
        }
    }

    @Override
    public void deleteDoc(IndexBuilderConfigEo eo, String id, String parentId, Map<String, Object> source) {
        logger.info("配置索引.. {}, 索引ID：{}", JSON.toJSONString(eo), id);
        if (IndexBuilderConfigEo.PARENT_ID.equals(eo.getParentId())) {
            logger.info("删除主表索引. index is {}, type is {}, docId is {}, data is {}", eo.getIndexName(),
                    eo.getSourceTable(), id, source.toString());
            //esTemplate.indexDoc(eo.getIndexName(), eo.getSourceTable(), id, source);
            esTemplate.deleteDoc(eo.getIndexName(), eo.getSourceTable(), id);
        } else {
            if (StringUtils.isEmpty(parentId)) {
                logger.warn("父文档Id为空！数据无效。{}.{} id:{}", eo.getSourceDb(), eo.getSourceTable(), id);
                return;
            }
            logger.info("删除子表索引. index is {}, type is {}, docId is {}, parentId is {}, data is {}",
                    eo.getIndexName(), eo.getSourceTable(), id, parentId, JSON.toJSONString(source));
            esTemplate.deleteDoc(eo.getIndexName(), eo.getSourceTable(), id, parentId);
        }
    }

    @Override
    public void updateDoc(IndexBuilderConfigEo eo, String id, String parentId, Map<String, Object> source) {
        logger.info("配置索引... {}, 索引ID：{}", JSON.toJSONString(eo), id);
        if (IndexBuilderConfigEo.PARENT_ID.equals(eo.getParentId())) {
            logger.info("构建主表索引. index is {}, type is {}, docId is {}, data is {}", eo.getIndexName(),
                    eo.getSourceTable(), id, source.toString());
            esTemplate.indexDoc(eo.getIndexName(), eo.getSourceTable(), id, source);
        } else {
            //FIXME 此处如果parentId为空的话直接忽略掉了，不行进任何处理，这样数据会不会有问题！！！
            if (StringUtils.isEmpty(parentId)) {
                handleEmptyParentId(eo, id);
                logger.warn("父文档Id为空！数据无效。{}.{} id:{}", eo.getSourceDb(), eo.getSourceTable(), id);
                return;
            }
            logger.info("构建子表索引:: index is {}, type is {}, docId is {}, parentId is {}, data is {}",
                    eo.getIndexName(), eo.getSourceTable(), id, parentId, JSON.toJSONString(source));
            esTemplate.indexDocWithRouting(eo.getIndexName(), eo.getSourceTable(), id, parentId, source);
        }
    }
    public static String convertToString(Object param) {
        return param == null ? null : param.toString().trim();
    }

    public void batchInsertDoc(IndexBuilderConfigEo eo, List<Map<String, Object>> list) {
        logger.info("批量更新索引,idex：{},type:{},size:{}", eo.getIndexName(), eo.getSourceTable(),list.size());
        BulkRequestBuilder bulkRequest = esTemplate.getClient().prepareBulk();
        for (Map<String, Object> source : list) {
            String id = convertToString(source.get(eo.getPrimaryKey()));
            String parentId = convertToString(source.get(eo.getParentPrimaryKey()));

            IndexRequest indexRequest = new IndexRequest(eo.getIndexName(), eo.getSourceTable(), id)
                    .source(source);
            UpdateRequestBuilder updateRequest = esTemplate.getClient().prepareUpdate(eo.getIndexName(), eo.getSourceTable(), id)
                    .setDoc(source).setUpsert(indexRequest);
            if (StringUtils.isNotEmpty(parentId) && !"0".equals(parentId)) {
                updateRequest.setRouting(parentId).setParent(parentId);
            }
            bulkRequest.add(updateRequest);
        }
        BulkResponse bulkResponse = bulkRequest.get();
        if (bulkResponse.hasFailures()) {
            logger.info("batch update fail ..");
            // process failures by iterating through each bulk response item
        }
    }


    public void batchInsertDoc(String index, String type, List<BatchDto> batchDtoList) {
        logger.info("batchInsert ,index:{},type:{}", index, type);
        BulkRequestBuilder bulkRequest = esTemplate.getClient().prepareBulk();
        for (BatchDto batchDto : batchDtoList) {
            IndexRequestBuilder indexRequest = esTemplate.getClient().prepareIndex(index, type, batchDto.getDocId())
                    .setSource(batchDto.getSource());
            if (StringUtils.isNotEmpty(batchDto.getParentId()) && !"0".equals(batchDto.getParentId())) {
                indexRequest.setRouting(batchDto.getRoutingId()).setParent(batchDto.getParentId());
            }
            bulkRequest.add(indexRequest);
        }
        BulkResponse bulkResponse = bulkRequest.get();
        if (bulkResponse.hasFailures()) {
            logger.info("batch insert fail ..");
            // process failures by iterating through each bulk response item
        }
    }
    public void batchUpateDoc(String index, String type, List<BatchDto> batchDtoList) {
        logger.info("batchUpate ,index:{},type:{}", index, type);
        BulkRequestBuilder bulkRequest = esTemplate.getClient().prepareBulk();
        for (BatchDto batchDto : batchDtoList) {
            UpdateRequestBuilder updateRequest = esTemplate.getClient().prepareUpdate(index, type, batchDto.getDocId())
                    .setDoc(batchDto.getSource());
            if (StringUtils.isNotEmpty(batchDto.getParentId()) && !"0".equals(batchDto.getParentId())) {
                updateRequest.setRouting(batchDto.getRoutingId()).setParent(batchDto.getParentId());
            }
            bulkRequest.add(updateRequest);
        }
        BulkResponse bulkResponse = bulkRequest.get();
        if (bulkResponse.hasFailures()) {
            logger.info("batch update fail ..");
            // process failures by iterating through each bulk response item
        }
    }

    public void batchUpsertDoc(String index, String type, List<BatchDto> batchDtoList) {
        logger.info("batchUpsert ,index:{},type:{}", index, type);
        BulkRequestBuilder bulkRequest = esTemplate.getClient().prepareBulk();
        for (BatchDto batchDto : batchDtoList) {
            IndexRequest indexRequest = new IndexRequest(index, type, batchDto.getDocId())
                    .source(batchDto.getSource());
            UpdateRequestBuilder updateRequest = esTemplate.getClient().prepareUpdate(index, type, batchDto.getDocId())
                    .setDoc(batchDto.getSource()).setUpsert(indexRequest);
            if (StringUtils.isNotEmpty(batchDto.getParentId()) && !"0".equals(batchDto.getParentId())) {
                updateRequest.setRouting(batchDto.getRoutingId()).setParent(batchDto.getParentId());
            }
            bulkRequest.add(updateRequest);
        }
        BulkResponse bulkResponse = bulkRequest.get();
        if (bulkResponse.hasFailures()) {
            System.out.println(bulkResponse.buildFailureMessage());
            logger.info("batch upsert fail ..");
            // process failures by iterating through each bulk response item
        }
    }
/*
    *//**
     * 特殊处理无父文档id的数据，后期规整
     */
    public void handleEmptyParentId(IndexBuilderConfigEo eo, String id){
        try {
            if(!"mc_ageloc_me_account".equals(eo.getSourceTable())){
                return;
            }
            SearchRequestBuilder  searchRequestBuilder = esTemplate.getClient().prepareSearch(eo.getIndexName()).setTypes(eo.getSourceTable());

            BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery().filter(QueryBuilders.termQuery("id",id));
            searchRequestBuilder.setQuery(queryBuilder);
            SearchResponse searchResponse = searchRequestBuilder.get();
            SearchHit[] hits = searchResponse.getHits().getHits();
            //logger.info("hits size : {}", hits.length);
            for (SearchHit searchHit : hits) {
                Map<String, Object> source = searchHit.getSource();
                String parentId = source.get(eo.getParentPrimaryKey()).toString();
                //logger.info("parentId:{}", parentId);
                if(StringUtils.isNotBlank(parentId)){
                    logger.info("删除无parentId文档，indexName:{}, sourceTable:{}, id:{}, parentId:{}", eo.getIndexName(), eo.getSourceTable(), id, parentId);
                    esTemplate.deleteDoc(eo.getIndexName(), eo.getSourceTable(), id, parentId);
                }
            }
        }catch (Exception e){
            logger.error("删除无父文档数据出现异常：{}", e);
        }
    }

}
