package com.rtambun.integration.container;

import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.utility.DockerImageName;

public class KafkaContainer extends org.testcontainers.containers.KafkaContainer {

    //see https://docs.spring.io/spring-data/elasticsearch/docs/current/reference/html/ for reference of elastic search
    //version for your spring boot
    private static final String KAFKA_VERSION = "6.2.1";

    private static KafkaContainer kafkaContainer;
    private static String bootStrapServer;

    public static void startKafkaCloseIncidentContainer() {
        if (kafkaContainer == null) {
            kafkaContainer = new KafkaContainer();
            kafkaContainer.start();
            bootStrapServer = kafkaContainer.getBootstrapServers();
        }
    }

    public static void stopKafkaCloseIncidentContainer() {
        kafkaContainer.stop();
        kafkaContainer = null;
    }

    private KafkaContainer() {
        super(DockerImageName.parse("confluentinc/cp-kafka:"+ KAFKA_VERSION));
    }

    public static class Initializer implements
            ApplicationContextInitializer<ConfigurableApplicationContext> {


        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            String kafkaBootStrapServer1 = "kafka.bootstrap.server1=" + bootStrapServer;
            String kafkaBootStrapServer2 = "kafka.bootstrap.server2=" + bootStrapServer;
            String kafkaBootStrapServer3 = "kafka.bootstrap.server3=" + bootStrapServer;
            String kafkaProducerRetries = "kafka.producer.retries=3";
            String kafkaClientId = "kafka.client.id=incident-client1";
            String kafkaClientIdForTest = "kafka.client.test.id=test";
            String kafkaTopic = "kafka.topic.minio.event=minio";

            TestPropertyValues.of(kafkaBootStrapServer1).applyTo(applicationContext.getEnvironment());
            TestPropertyValues.of(kafkaBootStrapServer2).applyTo(applicationContext.getEnvironment());
            TestPropertyValues.of(kafkaBootStrapServer3).applyTo(applicationContext.getEnvironment());
            TestPropertyValues.of(kafkaProducerRetries).applyTo(applicationContext.getEnvironment());
            TestPropertyValues.of(kafkaClientId).applyTo(applicationContext.getEnvironment());
            TestPropertyValues.of(kafkaClientIdForTest).applyTo(applicationContext.getEnvironment());
            TestPropertyValues.of(kafkaTopic).applyTo(applicationContext.getEnvironment());
        }
    }
}
