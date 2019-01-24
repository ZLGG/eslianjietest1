/**
 * @(#) DbManager.java 1.0 2018-01-19
 * Copyright (c) 2018, YUNXI. All rights reserved.
 * YUNXI PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.example.eslianjietest1.db;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidPooledConnection;
import com.example.eslianjietest1.mapper.IndexBuilderDataSourceMapper;
import com.example.eslianjietest1.vo.IndexBuilderDataSourceEo;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.sql.PreparedStatement;
import java.util.Map;

/**
 * @author 风行
 * @since 0.5.0
 */
@Service
public class DbManager {
    private static Logger logger = LoggerFactory.getLogger(DbManager.class);


    @Resource
    private IndexBuilderDataSourceMapper indexBuilderDataSourceMapper;

    private static Map<String, DruidDataSource> dbConnectionMap = Maps.newHashMap();

    public DruidDataSource getDatasource(String dbName) throws Exception {
        DruidDataSource connection = null;
        if (dbConnectionMap.containsKey(dbName)) {
            connection = dbConnectionMap.get(dbName);
        }

        if (null == connection) {
            IndexBuilderDataSourceEo eo = indexBuilderDataSourceMapper.selectDataSourceEo(dbName);
            if (null != eo) {
                DbConnection dbConnection = new DbConnection(eo.getDriver(),
                        eo.getDbUrl(),
                        eo.getDbUser(),
                        eo.getDbPasswd());
                connection = dbConnection.getDatasource();
                dbConnectionMap.put(dbName, connection);
            }
        }

        if (null == connection) {
            throw new RuntimeException("获取数据连接异常");
        }
        return connection;
    }

    public void closeConnection(DruidPooledConnection connection, PreparedStatement pstate ) {
        try {
            if(null!=pstate){
                pstate.close();
            }
            if(null!=connection) {
                connection.close();
            }
        } catch (Exception e) {
            logger.error("", e);
        }
    }
}
