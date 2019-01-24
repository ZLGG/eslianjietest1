package com.example.eslianjietest1.mapper;

import com.example.eslianjietest1.vo.IndexBuilderConfigEo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface IndexBuilderConfigMapper {
    @Select("select * from s_indexbuilder_config where dr = 0 and source_table = #{source_table}")
    List<IndexBuilderConfigEo> selectConfigEo(String source_table);

    @Select("select * from s_indexbuilder_config where dr = 0 and index_name = #{index_name} order by id asc")
    List<IndexBuilderConfigEo> selectConfigEoByIndex(String index_name);


   /* @Select("<script>" +
            "select * from s_indexbuilder_config s where dr =0 " +
            "<if test='indexName != null'> and index_name = #{indexName} </if>"+
            "<if test='tableName != null'> and source_table = #{tableName} </if>"+
            " order by id asc" +
            "</script>")*/
   @Select("select * from s_indexbuilder_config where dr = 0 and index_name = #{indexName} and source_table = #{tableName}")
    List<IndexBuilderConfigEo> selectConfigEoByIndexAndTable(@Param("indexName") String indexName, @Param("tableName")String tableName);

    @Select("SELECT DISTINCT index_name FROM `s_indexbuilder_config` ORDER BY id ASC")
    List<String> selectAllIndexName();

}
