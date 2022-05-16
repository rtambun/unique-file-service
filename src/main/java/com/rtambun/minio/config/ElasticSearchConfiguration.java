package com.rtambun.minio.config;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.RestClients;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

@Configuration
@EnableElasticsearchRepositories(basePackages = "com.rtambun.minio.repository")
public class ElasticSearchConfiguration {

    @Value("${elasticsearch.host}")
    private String elasticSearchHost;

    @Value("${elasticsearch.port}")
    private int elasticSearchPort;

    @Value("${elasticsearch.username}")
    private String elasticSearchUserName;

    @Value("${elasticsearch.password}")
    private String elasticSearchPassword;

    @Bean
    public RestHighLevelClient client() {
        if (elasticSearchUserName != null && !elasticSearchUserName.isBlank()
                && elasticSearchPassword != null && !elasticSearchPassword.isBlank()) {
            ClientConfiguration clientConfiguration = ClientConfiguration.builder()
                    .connectedTo(String.format("%s:%s", elasticSearchHost, elasticSearchPort))
                    .withBasicAuth(elasticSearchUserName, elasticSearchPassword)
                    .build();
            return RestClients.create(clientConfiguration).rest();
        } else {
            return(new RestHighLevelClient(
                    RestClient.builder(new HttpHost(elasticSearchHost, elasticSearchPort, "http"))));
        }
    }

    @Bean
    public ElasticsearchOperations elasticsearchTemplate() {
        return new ElasticsearchRestTemplate(client());
    }
}
