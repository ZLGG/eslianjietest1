/**
 * @(#) ISynchroDataService.java 1.0 2018-01-19
 * Copyright (c) 2018, YUNXI. All rights reserved.
 * YUNXI PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.example.eslianjietest1.service;

import com.example.eslianjietest1.vo.IndexBuilderConfigEo;

import java.util.List;
import java.util.Map;

/**
 * @author 风行
 * @since 0.5.0
 */
public interface ISynchroDataService {

    /**
     * 同步数据
     */
    void syncData() throws Exception;

    /**
     * 同步数据
     */
    void syncData(IndexBuilderConfigEo eo) throws Exception;

    void syncData(String indexName, String tableName) throws Exception;

    void syncData(IndexBuilderConfigEo configEo, Boolean syncIndex) throws Exception;

    /**
     * 更新索引
     * @param configEo
     * @param syncIndex
     * @param list
     */
    void updateIndex(IndexBuilderConfigEo configEo, Boolean syncIndex, List<Map<String, Object>> list);

    /**
     * 按index同步数据
     *
     * @param index
     */
    void syncDataByindex(String index);
}