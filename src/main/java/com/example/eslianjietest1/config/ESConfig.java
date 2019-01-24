package com.example.eslianjietest1.config;

import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.InetAddress;

@Configuration
public class ESConfig {
    @Bean("client")
    public TransportClient getTransprotClient() throws Exception{
        TransportClient client = new PreBuiltTransportClient(Settings.EMPTY).addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("127.0.0.1"), 9300));
        return client;
    }

/*@Bean("esTemplate")
    @Scope("prototype")
    public ESTemplate esTemplate() throws Exception{
        return new ESTemplate(getTransprotClient());
    }*/

}
