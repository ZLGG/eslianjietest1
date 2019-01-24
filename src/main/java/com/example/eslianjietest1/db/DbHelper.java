package com.example.eslianjietest1.db;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidPooledConnection;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
public class DbHelper {

    private static Logger logger = LoggerFactory.getLogger(DbHelper.class);

    @Resource
    private DbManager dbManager;

    public static final int PAGE_SIZE = 8000;

    public boolean checkRowExists(String dbName, String tableName, String primaryKey, String value) throws Exception {
        DruidDataSource druidDataSource = dbManager.getDatasource(dbName);
        DruidPooledConnection connection = druidDataSource.getConnection();
        StringBuilder sql = new StringBuilder("select * from ").append(tableName).append(" where ");
        sql.append(primaryKey).append("='").append(value).append("' where dr=0");
        logger.info("SQL:{}", sql.toString());

        PreparedStatement pstate = null;
        try {
            pstate = connection.prepareStatement(sql.toString());
            ResultSet result = pstate.executeQuery();
            return StringUtils.isNotBlank(result.getString(primaryKey));
        } catch (Exception e) {
            logger.error("", e);
        } finally {
            dbManager.closeConnection(connection, pstate);
        }
        return false;
    }

    public Long getTableCount(String dbName, String table) throws Exception {
        DruidDataSource druidDataSource = dbManager.getDatasource(dbName);
        DruidPooledConnection connection = druidDataSource.getConnection();
        StringBuilder sqlCount = new StringBuilder("select count(*) as count from ").append(table).append(" where dr=0;");
        logger.info("查询同步数据条数sql:{}",sqlCount);
        PreparedStatement pstate = null;
        try {
            pstate = connection.prepareStatement(sqlCount.toString());
            ResultSet results = pstate.executeQuery();
            while (results.next()) {
                return results.getLong("count");
            }
        } catch (Exception e) {
            logger.error("查询同步数据条数出现异常：{}", e);
        } finally {
            dbManager.closeConnection(connection, pstate);
        }
        return 0L;
    }

    public static Long getPages(Long count, int pageSize) {
        Long pages = count / pageSize;
        Long mod = count % pageSize;
        pages = mod != 0 ? pages + 1 : pages;
        return pages;
    }

    public List<Map<String, Object>> getPageRows(Long maxId, String dbName, String tableName) throws Exception {
        StringBuilder sql = new StringBuilder("select * from " + tableName);
        sql.append(" where id>").append(maxId).append(" and dr=0 order by id").append(" limit ").append(PAGE_SIZE);
        logger.info("获取{}->{} SQL:{}", dbName, tableName, sql.toString());
        return getRows(dbName, sql.toString());
    }

    public List<Map<String, Object>> getRecentRows(Long maxId, String dbName, String tableName) throws Exception {
        StringBuilder sql = new StringBuilder("select * from " + tableName);
        sql.append(" where id>").append(maxId).append(" and update_time>='").append(new DateTime().minusMinutes(20).toString("yyyy-MM-dd HH:mm:ss")).append("' and dr=0 order by id").append(" limit ").append(PAGE_SIZE);
        logger.info("获取{}->{} SQL:{}", dbName, tableName, sql.toString());
        return getRows(dbName, sql.toString());
    }


    public List<Map<String, Object>> getRows(String dbName, String sql) throws Exception {
        List<Map<String, Object>> list = new ArrayList<>();
        DruidDataSource druidDataSource = dbManager.getDatasource(dbName);
        DruidPooledConnection connection = druidDataSource.getConnection();
        logger.info("SQL:{}", sql);
        PreparedStatement pstate = null;
        try {
            pstate = connection.prepareStatement(sql);
            ResultSet results = pstate.executeQuery();
            // 获取键名
            ResultSetMetaData md = results.getMetaData();
            while (results.next()) {
                // 声明Map
                Map<String, Object> rowData = new HashMap<>();
                Integer counts = md.getColumnCount();
                for (int i = 1; i <= counts; i++) {
                    // 获取键名及值
                    rowData.put(md.getColumnName(i), packageObject(results.getObject(i)));
                }
                list.add(rowData);
            }
        } catch (Exception e) {
            logger.error("", e);
        } finally {

            dbManager.closeConnection(connection, pstate);
        }
        return list;

    }

    public Long getMaxId(String dbName, String tableName) throws Exception {
        DruidDataSource druidDataSource = dbManager.getDatasource(dbName);
        DruidPooledConnection connection = druidDataSource.getConnection();
        StringBuilder sql = new StringBuilder("select max(id) as maxId from " + tableName).append(" where dr=0");
        logger.info("SQL:{}", sql.toString());
        PreparedStatement pstate = null;
        try {
            pstate = connection.prepareStatement(sql.toString());
            ResultSet results = pstate.executeQuery();
            while (results.next()) {
                Long maxId = results.getLong("maxId");
                logger.info("当前需要同步的{}->{}最大ID是:{}", dbName, tableName, maxId);
                return maxId;
            }
        } catch (Exception e) {
            logger.error("", e);
        } finally {
            dbManager.closeConnection(connection, pstate);
        }
        return 0L;
    }

    /**
     * 用于解决数据库中tinyint类型被映射成boolean类型导致数据插入失败问题
     *
     * @param object
     * @return
     */
    public Object packageObject(Object object) {
        if (object == null)
            return object;
        if (object.equals(false)) {
            return "0";
        }
        if (object.equals(true)) {
            return "1";
        }
        return object;
    }

    /**
     * 根据id获取数据
     *
     * @param dbName
     * @param tableName
     * @param id
     * @return
     * @throws Exception
     */
    public Map<String, Object> getDataById(String dbName, String tableName, Long id) throws Exception {
        StringBuilder sql = new StringBuilder("select * from " + tableName);
        sql.append(" where id=").append(id);
        logger.info("根据ID获取{}->{} SQL:{}", dbName, tableName, sql.toString());
        List<Map<String, Object>> list = getRows(dbName, sql.toString());
        if(CollectionUtils.isEmpty(list)){
            return null;
        }
        return list.get(0);
    }

}
