package com.rtambun.integration;

import com.rtambun.integration.container.FileMapRepositoryContainer;
import com.rtambun.integration.container.KafkaContainer;
import com.rtambun.integration.container.MinioClientContainer;
import com.rtambun.integration.container.MinioContainer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

public class InitIntegrationTest {
    @BeforeAll
    private static void setUp() {
        MinioContainer.startMinioContainer();
        MinioClientContainer.startMinioClientContainer(MinioContainer.MINIO_BUCKET,
                MinioContainer.MINIO_ACCESS_KEY,
                MinioContainer.MINIO_SECRET_KEY,
                MinioContainer.getMinioContainerIpAddress());
        FileMapRepositoryContainer.startFileMapRepositoryContainer();
        KafkaContainer.startKafkaCloseIncidentContainer();
    }

    @AfterAll
    private static void tearDown() {
        MinioContainer.stopMinioContainer();
        MinioClientContainer.stopMinioClientContainer();
        FileMapRepositoryContainer.stopFileMapRepositoryContainer();
        KafkaContainer.stopKafkaCloseIncidentContainer();
    }
}
