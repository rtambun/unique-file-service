package com.rtambun.minio.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

import javax.annotation.Nonnull;

@Configuration
@EnableElasticsearchRepositories(basePackages = "com.rtambun.minio.repository")
public class ElasticSearchConfiguration extends ElasticsearchConfiguration {

    private final String elasticSearchHost;
    private final int elasticSearchPort;
    private final String elasticSearchUserName;
    private final String elasticSearchPassword;

    public ElasticSearchConfiguration(@Value("${elasticsearch.host}") String elasticSearchHost,
                                      @Value("${elasticsearch.port}") int elasticSearchPort,
                                      @Value("${elasticsearch.username}") String elasticSearchUserName,
                                      @Value("${elasticsearch.password}") String elasticSearchPassword) {
        this.elasticSearchHost = elasticSearchHost;
        this.elasticSearchPort = elasticSearchPort;
        this.elasticSearchUserName = elasticSearchUserName;
        this.elasticSearchPassword = elasticSearchPassword;
    }

    @Override
    @Nonnull
    public ClientConfiguration clientConfiguration() {
        if (elasticSearchUserName != null && !elasticSearchUserName.isBlank()
                && elasticSearchPassword != null && !elasticSearchPassword.isBlank()) {
            return ClientConfiguration.builder()
                    .connectedTo(String.format("%s:%s", elasticSearchHost, elasticSearchPort))
                    .withBasicAuth(elasticSearchUserName, elasticSearchPassword)
                    .build();
        } else {
            return ClientConfiguration.create(String.format("http://%s:%s", elasticSearchHost, elasticSearchPort));
        }
    }
}
