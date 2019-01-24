package com.example.eslianjietest1.vo;



public class IndexBuilderDataSourceEo {

	/**
	 * 驱动
	 */
	private String driver;

	/**
	 * 主表索引
	 */
	private String dbName;

	/**
	 * 数据库账号
	 */
	private String dbUser;

	/**
	 * 数据库密码
	 */
	private String dbPasswd;

	/**
	 * 数据库连接
	 */
	private String dbUrl;

	/**
	 * 状态
	 */
	private Integer status;

	public String getDriver() {
		return driver;
	}

	public void setDriver(String driver) {
		this.driver = driver;
	}

	public String getDbName() {
		return dbName;
	}

	public void setDbName(String dbName) {
		this.dbName = dbName;
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

	public String getDbUrl() {
		return dbUrl;
	}

	public void setDbUrl(String dbUrl) {
		this.dbUrl = dbUrl;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}
}
