package com.example.eslianjietest1.es;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.action.admin.indices.exists.types.TypesExistsRequest;
import org.elasticsearch.action.admin.indices.exists.types.TypesExistsResponse;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingResponse;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.script.Script;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.Aggregations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ExecutionException;

@Slf4j
@Service
public class ESTemplate {
    //private Long totalSize;
    @Autowired
    private TransportClient client;

    public ESTemplate(TransportClient client) {
        this.client = client;
    }


    public TransportClient getClient() {
        return this.client;
    }

    public IndexResponse indexDoc(String index, String type, String docId, Map<String, Object> source) {
        return (IndexResponse) this.client.prepareIndex(index, type, docId).setRouting(docId).setSource(source).get();
    }

    public IndexResponse indexDocWithRouting(String index, String type, String docId, String parentId, Map<String, Object> source) {
        return (IndexResponse) this.client.prepareIndex(index, type, docId).setParent(parentId).setRouting(parentId).setSource(source).get();
    }

    public UpdateResponse updateDoc(String index, String type, String docId, Map<String, Object> source) throws InterruptedException, ExecutionException {
        UpdateRequest updateRequest = new UpdateRequest(index, type, docId);
        updateRequest.doc(source);
        return (UpdateResponse) this.client.update(updateRequest).get();
    }

    public UpdateResponse updateDocWithRouting(String index, String type, String docId, String parentId, Map<String, Object> source) throws InterruptedException, ExecutionException {
        UpdateRequest updateRequest = new UpdateRequest(index, type, docId);
        updateRequest.parent(parentId);
        updateRequest.routing(parentId);
        updateRequest.doc(source);
        return (UpdateResponse) this.client.update(updateRequest).get();
    }

    public DeleteResponse deleteDoc(String index, String type, String docId) {
        return (DeleteResponse) this.client.prepareDelete(index, type, docId).get();
    }

    public DeleteResponse deleteDoc(String index, String type, String docId, String parentId) {
        return (DeleteResponse) this.client.prepareDelete(index, type, docId).setRouting(parentId).get();
    }

    public CreateIndexResponse createIndex(String index) {
        return (CreateIndexResponse) this.client.admin().indices().prepareCreate(index).get();
    }

    public PutMappingResponse createIndexMapping(String index, String type, String json) {
        return (PutMappingResponse) this.client.admin().indices().preparePutMapping(new String[]{index}).setType(type).setSource(json, XContentType.JSON).get();
    }

    public PutMappingResponse createIndexMapping(String index, String json) {
        return (PutMappingResponse) this.client.admin().indices().preparePutMapping(new String[]{index}).setSource(json, XContentType.JSON).get();
    }

    public boolean isExistsIndex(String indexName) {
        IndicesExistsResponse response = (IndicesExistsResponse) client.admin().indices().exists((new IndicesExistsRequest()).indices(new String[]{indexName})).actionGet();
        return response.isExists();
    }

    public boolean isExistsType(String indexName, String indexType) {
        TypesExistsResponse response = (TypesExistsResponse) client.admin().indices().typesExists(new TypesExistsRequest(new String[]{indexName}, new String[]{indexType})).actionGet();
        return response.isExists();
    }

    /*public void setTotalSize(SearchResponse response) {
        this.totalSize = response.getHits().getTotalHits();
    }*/

    public void updateIndex(String index, String type, String docId, Map<String, Object> source) {
        UpdateRequest updateRequest = new UpdateRequest();
        updateRequest.index(index);
        updateRequest.type(type);
        updateRequest.id(docId);
        updateRequest.doc(source);

        try {
            this.client.update(updateRequest).get();
        } catch (ExecutionException | InterruptedException var7) {
            log.error("updateIndex failure!", var7);
        }

    }

    public void updateIndex(String index, String type, String docId, Script script) {
        UpdateRequest updateRequest = (new UpdateRequest(index, type, docId)).script(script);

        try {
            this.client.update(updateRequest).get();
        } catch (ExecutionException | InterruptedException var7) {
            log.error("updateIndex failure!", var7);
        }

    }

    public Long count(String index, String type, QueryBuilder queryBuilder) {
        return ((SearchResponse) this.client.prepareSearch(new String[]{index}).setTypes(new String[]{type}).setSize(0).setQuery(queryBuilder).get()).getHits().getTotalHits();
    }

    public Aggregations aggregate(String index, String type, QueryBuilder queryBuilder, AggregationBuilder aggregationBuilder) {
        log.debug("Enter into pagingAndSortQuery method. index={}, type={}, queryBuilder={}, aggregationBuilder={}", new Object[]{index, type, JSON.toJSONString(queryBuilder), JSON.toJSONString(aggregationBuilder)});
        SearchRequestBuilder searchRequestBuilder = this.client.prepareSearch(new String[]{index}).setTypes(new String[]{type}).setSearchType(SearchType.QUERY_THEN_FETCH);
        if (queryBuilder != null) {
            searchRequestBuilder.setQuery(queryBuilder);
        }

        if (aggregationBuilder != null) {
            searchRequestBuilder.addAggregation(aggregationBuilder);
        }

        searchRequestBuilder.setSize(0);
        searchRequestBuilder.setFetchSource(false);
        log.debug("searchRequestBuilder={}", searchRequestBuilder);
        SearchResponse searchResponse = (SearchResponse) searchRequestBuilder.get();
        log.debug("searchResponse={}", searchResponse);
        return searchResponse.getAggregations();
    }

    public void bulkIndex(BulkRequestBuilder bulkRequestBuilder) {
        BulkResponse bulkResponse = (BulkResponse) bulkRequestBuilder.get();
        if (bulkResponse.hasFailures()) {
            log.error("批量插入有誤！");
        }

    }

    /*public Long getTotalSize() {
        return this.totalSize;
    }*/

    public void setClient(TransportClient client) {
        this.client = client;
    }
}
