package com.example.eslianjietest1.db;

import com.alibaba.druid.pool.DruidDataSource;


public class DbConnection {

    private DruidDataSource dataSource;

    private String dbDriver;
    private String dbUrl;
    private String dbUser;
    private String dbPasswd;

    public DbConnection(String dbDriver, String dbUrl, String dbUser, String dbPasswd) {
        this.dbDriver = dbDriver;
        this.dbUrl = dbUrl;
        this.dbUser = dbUser;
        this.dbPasswd = dbPasswd;
        dataSource = new DruidDataSource();
        dataSource.setDriverClassName(dbDriver);
        dataSource.setUrl(dbUrl);
        dataSource.setUsername(dbUser);
        dataSource.setPassword(dbPasswd);
        dataSource.setInitialSize(10);
        dataSource.setMaxActive(20);
        dataSource.setRemoveAbandonedTimeout(1800); // 连接池归还最晚时间
    }

    public DruidDataSource getDatasource() throws Exception {
        return dataSource;
    }

    public DruidDataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(DruidDataSource dataSource) {
        this.dataSource = dataSource;
    }

    public String getDbDriver() {
        return dbDriver;
    }

    public void setDbDriver(String dbDriver) {
        this.dbDriver = dbDriver;
    }

    public String getDbUrl() {
        return dbUrl;
    }

    public void setDbUrl(String dbUrl) {
        this.dbUrl = dbUrl;
    }

    public String getDbUser() {
        return dbUser;
    }

    public void setDbUser(String dbUser) {
        this.dbUser = dbUser;
    }

    public String getDbPasswd() {
        return dbPasswd;
    }

    public void setDbPasswd(String dbPasswd) {
        this.dbPasswd = dbPasswd;
    }
}
