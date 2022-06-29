package com.rtambun.integration;

import com.rtambun.integration.container.FileMapRepositoryContainer;
import com.rtambun.integration.container.KafkaContainer;
import com.rtambun.integration.container.MinioContainer;
import com.rtambun.minio.SpringBootMinioApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@SpringBootTest(classes = SpringBootMinioApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(initializers = {
        FileMapRepositoryContainer.Initializer.class,
        MinioContainer.Initializer.class,
        KafkaContainer.Initializer.class
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@EnableAutoConfiguration
public @interface IntegrationTest {
}
