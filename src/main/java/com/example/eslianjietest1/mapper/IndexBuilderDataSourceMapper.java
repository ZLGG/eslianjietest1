package com.example.eslianjietest1.mapper;

import com.example.eslianjietest1.vo.IndexBuilderDataSourceEo;
import org.apache.ibatis.annotations.Select;

public interface IndexBuilderDataSourceMapper {
    @Select("select * from s_indexbuilder_datasource where db_name = #{db_Name}")
    IndexBuilderDataSourceEo selectDataSourceEo(String db_Name);
}