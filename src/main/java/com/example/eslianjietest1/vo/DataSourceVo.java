package com.example.eslianjietest1.vo;

import lombok.Data;

@Data
public class DataSourceVo {
    private String driverClassName;
    private String jdbcUrl;
    private String jdbcUserName;
    private String jdbcUserPassword;
    private String validationQuery;
    private int initialSize;
    private int maxActive;
    private int minIdle;
    private int maxWait;
}
