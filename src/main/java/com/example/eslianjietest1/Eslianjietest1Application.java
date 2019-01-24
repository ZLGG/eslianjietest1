package com.example.eslianjietest1;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class Eslianjietest1Application {

    public static void main(String[] args) throws Exception{
        SpringApplication.run(Eslianjietest1Application.class, args);
        System.out.println("success");
        String l = "1193891581379337327";
        long l1 = Long.parseLong(l);
        System.out.println(l1);
        //System.out.println(test());
    }

    /*public static String test() throws Exception{
        //Settings.Builder builder = Settings.builder().put("client.transport.sniff", false);
        TransportClient transportClient = new PreBuiltTransportClient(Settings.EMPTY)
                .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("127.0.0.1"),9300));
        GetResponse fields = transportClient.prepareGet("qq", "doc", "1").get();

        return fields.toString();
    }*/

}

