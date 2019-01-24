package com.example.eslianjietest1.vo;

/**
 * 数据库配置表
 */
public class IndexBuilderConfigEo {

	public static final String PARENT_ID = "0";

	/**
	 * 主表索引
	 */
	private String parentId;

	/**
	 * 索引名词
	 */
	private String indexName;

	/**
	 * 源数据库
	 */
	private String sourceDb;

	/**
	 * 源数据表
	 */
	private String sourceTable;

	/**
	 * 主键字段
	 */
	private String primaryKey;

	/**
	 * 主表组建字段
	 */
	private String parentPrimaryKey;

	public String getParentId() {
		return parentId;
	}

	public void setParentId(String parentId) {
		this.parentId = parentId;
	}

	public String getIndexName() {
		return indexName;
	}

	public void setIndexName(String indexName) {
		this.indexName = indexName;
	}

	public String getSourceDb() {
		return sourceDb;
	}

	public void setSourceDb(String sourceDb) {
		this.sourceDb = sourceDb;
	}

	public String getSourceTable() {
		return sourceTable;
	}

	public void setSourceTable(String sourceTable) {
		this.sourceTable = sourceTable;
	}

	public String getPrimaryKey() {
		return primaryKey;
	}

	public void setPrimaryKey(String primaryKey) {
		this.primaryKey = primaryKey;
	}

	public String getParentPrimaryKey() {
		return parentPrimaryKey;
	}

	public void setParentPrimaryKey(String parentPrimaryKey) {
		this.parentPrimaryKey = parentPrimaryKey;
	}

	@Override
	public String toString() {
		return "IndexBuilderConfigEo{" +
				"parentId='" + parentId + '\'' +
				", indexName='" + indexName + '\'' +
				", sourceDb='" + sourceDb + '\'' +
				", sourceTable='" + sourceTable + '\'' +
				", primaryKey='" + primaryKey + '\'' +
				", parentPrimaryKey='" + parentPrimaryKey + '\'' +
				'}';
	}
}
