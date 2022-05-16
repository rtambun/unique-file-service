package com.rtambun.integration.container;

import lombok.extern.log4j.Log4j2;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.HttpWaitStrategy;

import java.time.Duration;

@Log4j2
public class MinioContainer extends GenericContainer<MinioContainer>{

    public final static String MINIO_ACCESS_KEY = "testAccessKey";
    public final static String MINIO_SECRET_KEY = "testSecretKey";
    public final static String MINIO_BUCKET = "isms";
    public final static String MINIO_RESPONSE_URL = "http://test/";

    private static MinioContainer minioContainer;

    private MinioContainer() {
        super("minio/minio");
        withEnv("MINIO_ROOT_USER", MINIO_ACCESS_KEY);
        withEnv("MINIO_ROOT_PASSWORD", MINIO_SECRET_KEY);
        withCommand("-v /data");
        withCommand("server /data");
        withExposedPorts(9000);
        waitingFor(new HttpWaitStrategy()
                .forPath("/minio/health/ready")
                .withStartupTimeout(Duration.ofSeconds(10)));
    }

    public static MinioContainer startMinioContainer() {
        if(minioContainer == null) {
            minioContainer = new MinioContainer();
            minioContainer.start();
            getMinioContainerIpAddress();
        }
        return minioContainer;
    }

    public static void stopMinioContainer() {
        if(minioContainer != null) {
            minioContainer.stop();
            minioContainer = null;
        }
    }

    public static String getMinioContainerIpAddress() {
        String containerIpAddress = minioContainer.getContainerInfo().getNetworkSettings().getIpAddress() + ":9000";
        log.info("Minio container ip address is : {}" + containerIpAddress);
        return containerIpAddress;
    }

    public static class Initializer implements
            ApplicationContextInitializer<ConfigurableApplicationContext> {

        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            Integer port = minioContainer.getMappedPort(9000);
            log.info("Host port for minio container is {}", port);

            String minioUrl = "spring.minio.url=http://localhost:" + port;
            String minioBucket = "spring.minio.bucket=" + MINIO_BUCKET;
            String minioAccessKey = "spring.minio.access-key=" + MINIO_ACCESS_KEY;
            String minioSecretKey = "spring.minio.secret-key=" + MINIO_SECRET_KEY;
            String minioResponseUrl = "minio.response.url=" + MINIO_RESPONSE_URL;

            TestPropertyValues.of(minioUrl).applyTo(applicationContext.getEnvironment());
            TestPropertyValues.of(minioBucket).applyTo(applicationContext.getEnvironment());
            TestPropertyValues.of(minioAccessKey).applyTo(applicationContext.getEnvironment());
            TestPropertyValues.of(minioSecretKey).applyTo(applicationContext.getEnvironment());
            TestPropertyValues.of(minioResponseUrl).applyTo(applicationContext.getEnvironment());
        }
    }

}
