package com.example.eslianjietest1.config;

import com.alibaba.druid.pool.DruidDataSource;
import com.example.eslianjietest1.vo.DataSourceVo;
import org.apache.ibatis.logging.slf4j.Slf4jImpl;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan(basePackages = {"com.example.eslianjietest1.mapper"},sqlSessionFactoryRef = "sqlSessionFactory")
public class DataSourceConfig {
    @Bean("datasource")
    public DruidDataSource dataSource() {
        DruidDataSource dataSource = new DruidDataSource();

        //String value = ConfigService.getConfig("yundt.smartsales.mall.appmgmt.datasourcevo", group, 3000L);
        //DataSourceVo dataSourceVo = objectMapper.readValue(value, DataSourceVo.class);
        //DataSourceVo dataSourceVo1 = JSON.parseObject(value, dataSourceVo.getClass());
        DataSourceVo dataSourceVo = new DataSourceVo();
        dataSourceVo.setDriverClassName("com.mysql.jdbc.Driver");
        dataSourceVo.setJdbcUrl("jdbc:mysql://127.0.0.1:3306/twcart_qa_mall_mgmt?useUnicode=true&characterEncoding=UTF-8&zeroDateTimeBehavior=convertToNull&allowMultiQueries=true");
        dataSourceVo.setJdbcUserName("root");
        dataSourceVo.setJdbcUserPassword("root");
        dataSourceVo.setMaxActive(5);
        dataSourceVo.setValidationQuery("SELECT 1");
        dataSourceVo.setInitialSize(1);
        dataSourceVo.setMinIdle(0);
        dataSourceVo.setMaxWait(60000);

        dataSource.setDriverClassName(dataSourceVo.getDriverClassName());
        dataSource.setUrl(dataSourceVo.getJdbcUrl());
        dataSource.setUsername(dataSourceVo.getJdbcUserName());
        dataSource.setPassword(dataSourceVo.getJdbcUserPassword());
        dataSource.setMaxActive(dataSourceVo.getMaxActive());
        dataSource.setValidationQuery(dataSourceVo.getValidationQuery());
        dataSource.setInitialSize(dataSourceVo.getInitialSize());
        dataSource.setMinIdle(dataSourceVo.getMinIdle());
        dataSource.setMaxWait(dataSourceVo.getMaxWait());
        return dataSource;
    }

    @Bean(name = "sqlSessionFactory")
    public SqlSessionFactory sqlsessionFactoryBean(@Qualifier("datasource") DruidDataSource dataSource) throws Exception{
        org.apache.ibatis.session.Configuration config = new org.apache.ibatis.session.Configuration();
        config.setMapUnderscoreToCamelCase(true);
        config.setLogImpl(Slf4jImpl.class);
        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        sqlSessionFactoryBean.setDataSource(dataSource);
        sqlSessionFactoryBean.setConfiguration(config);

        return sqlSessionFactoryBean.getObject();
    }
}
