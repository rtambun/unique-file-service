package com.rtambun.integration.container;

import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

public class FileMapRepositoryContainer extends ElasticsearchContainer {

    //see https://docs.spring.io/spring-data/elasticsearch/docs/current/reference/html/ for reference of elastic search
    //version for your spring boot
    private static final String ELASTICSEARCH_VERSION = "7.15.2";
    private static final String ELASTICSEARCH_REGISTRY  = "docker.elastic.co/elasticsearch/elasticsearch";

    public static final String ELASTICSEARCH_HOST = "localhost";
    public static final String ELASTICSEARCH_USERNAME = "elastic";
    public static final String ELASTICSEARCH_PASSWORD = "password";

    private static FileMapRepositoryContainer schedulerRepositoryContainer;

    public static FileMapRepositoryContainer startFileMapRepositoryContainer() {
        if (schedulerRepositoryContainer == null) {
            schedulerRepositoryContainer = new FileMapRepositoryContainer();
            schedulerRepositoryContainer.start();
        }
        return schedulerRepositoryContainer;
    }

    public static void stopFileMapRepositoryContainer() {
        schedulerRepositoryContainer.stop();
        schedulerRepositoryContainer = null;
    }

    private FileMapRepositoryContainer() {
        super(DockerImageName.parse(ELASTICSEARCH_REGISTRY).withTag(ELASTICSEARCH_VERSION));
        withPassword(ELASTICSEARCH_PASSWORD);
        withStartupTimeout(Duration.of(300, ChronoUnit.SECONDS));
    }

    public static class Initializer implements
            ApplicationContextInitializer<ConfigurableApplicationContext> {


        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            String elasticSearchPort = "elasticsearch.port=" +
                    schedulerRepositoryContainer.getMappedPort(9200);
            String elasticSearchHost = "elasticsearch.host=" + ELASTICSEARCH_HOST;
            String elasticSearchUserName = "elasticsearch.username=" + ELASTICSEARCH_USERNAME;
            String elasticSearchPassword = "elasticsearch.password=" + ELASTICSEARCH_PASSWORD;

            TestPropertyValues.of(elasticSearchPort).applyTo(applicationContext.getEnvironment());
            TestPropertyValues.of(elasticSearchHost).applyTo(applicationContext.getEnvironment());
            TestPropertyValues.of(elasticSearchUserName).applyTo(applicationContext.getEnvironment());
            TestPropertyValues.of(elasticSearchPassword).applyTo(applicationContext.getEnvironment());
        }
    }

}