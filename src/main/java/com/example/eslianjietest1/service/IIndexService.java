package com.example.eslianjietest1.service;


import com.example.eslianjietest1.vo.BatchDto;
import com.example.eslianjietest1.vo.IndexBuilderConfigEo;

import java.util.List;
import java.util.Map;

public interface IIndexService {

    /**
     * 新建文档
     */
    void insertDoc(IndexBuilderConfigEo eo, String id, String parentId, Map<String, Object> source);

    /**
     * 删除文档
     */
    void deleteDoc(IndexBuilderConfigEo eo, String id, String parentId, Map<String, Object> source);

    /**
     * 更新文档
     */
    void updateDoc(IndexBuilderConfigEo eo, String id, String parentId, Map<String, Object> source);

    /**
     * 更新文档
     */
    void batchInsertDoc(IndexBuilderConfigEo eo, List<Map<String, Object>> list);


    void batchInsertDoc(String index, String type, List<BatchDto> batchDtoList);


    void batchUpateDoc(String index, String type, List<BatchDto> batchDtoList);


    void batchUpsertDoc(String index, String type, List<BatchDto> batchDtoList);
}
