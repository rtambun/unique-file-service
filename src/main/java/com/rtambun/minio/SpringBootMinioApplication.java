package com.rtambun.minio;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchDataAutoConfiguration;

@SpringBootApplication(exclude = ElasticsearchDataAutoConfiguration.class)
public class SpringBootMinioApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringBootMinioApplication.class, args);
    }

}
