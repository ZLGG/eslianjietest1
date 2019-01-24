package com.example.eslianjietest1.controller;

import com.example.eslianjietest1.canal.ClientSample;
import com.example.eslianjietest1.mapper.IndexBuilderConfigMapper;
import com.example.eslianjietest1.service.ISynchroDataService;
import com.example.eslianjietest1.vo.IndexBuilderConfigEo;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("test")
public class controller {
    @Resource
    IndexBuilderConfigMapper indexBuilderConfigMapper;
    @Autowired
    ClientSample clientSample;
    @Autowired
    TransportClient client;
    @Autowired
    ISynchroDataService iSynchroDataService;

    @RequestMapping("/1")
    public String test() {
        //IndexBuilderConfigEo config = indexBuilderConfigMapper.selectConfigEo("it_item");
        clientSample.cannal();
        return "success";
    }

    @RequestMapping("/2")
    public String test1() {
        GetResponse response = client.prepareGet("qq", "doc", "1").get();
        String sourceAsString = response.getSourceAsString();
        return sourceAsString;
    }

    /**
     * 根据索引名字同步
     * @return
     * @throws Exception
     */
    @RequestMapping("/3")
    public String test3() throws Exception{

        iSynchroDataService.syncDataByindex("twcart_qa_user");
        return "success";
    }

    /**
     * 根据索引和表名同步
     * @return
     * @throws Exception
     */
    @RequestMapping("/4/{tableName}")
    public String test4(@PathVariable String tableName) throws Exception{
        iSynchroDataService.syncData("twcart_qa_user",tableName);
        return "success";
    }

    /**
     * 同步所有索引
     */
    @RequestMapping("/5")
    public String test5() {
        List<String> allIndexName = indexBuilderConfigMapper.selectAllIndexName();
        for (String s : allIndexName) {
            iSynchroDataService.syncDataByindex(s);
        }
        return "success";
    }
}
