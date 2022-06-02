package com.rtambun.minio;

import com.rtambun.integration.container.FileMapRepositoryContainer;
import com.rtambun.integration.container.MinioClientContainer;
import com.rtambun.integration.container.MinioContainer;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;

import static org.junit.jupiter.api.Assertions.assertSame;

@SpringBootTest(classes = SpringBootMinioApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(initializers = {
        FileMapRepositoryContainer.Initializer.class,
        MinioContainer.Initializer.class
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@EnableAutoConfiguration
@Log4j2
public class ServiceTest {

    @BeforeAll
    private static void setUp() {
        MinioContainer.startMinioContainer();
        MinioClientContainer.startMinioClientContainer(MinioContainer.MINIO_BUCKET,
                MinioContainer.MINIO_ACCESS_KEY,
                MinioContainer.MINIO_SECRET_KEY,
                MinioContainer.getMinioContainerIpAddress());
        FileMapRepositoryContainer.startFileMapRepositoryContainer();
    }

    @AfterAll
    private static void tearDown() {
        MinioContainer.stopMinioContainer();
        MinioClientContainer.stopMinioClientContainer();
        FileMapRepositoryContainer.stopFileMapRepositoryContainer();
    }
    @Autowired
    private DummyService service = new DummyService();
    @Test
    public void testHelloWorld()
    {
        assertSame("Hello World!", service.helloWorld());
    }
}
