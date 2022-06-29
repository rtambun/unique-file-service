package com.rtambun.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.rtambun.integration.container.FileMapRepositoryContainer;
import com.rtambun.integration.container.KafkaContainer;
import com.rtambun.integration.container.MinioClientContainer;
import com.rtambun.integration.container.MinioContainer;
import com.rtambun.integration.util.TestUtil;
import com.rtambun.minio.SpringBootMinioApplication;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;

import java.net.URISyntaxException;

@SpringBootTest(classes = SpringBootMinioApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(initializers = {
        FileMapRepositoryContainer.Initializer.class,
        MinioContainer.Initializer.class,
        KafkaContainer.Initializer.class
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@EnableAutoConfiguration
@Log4j2
public class UploadFileWithoutIncidentId {

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

    @Autowired
    private TestRestTemplate restTemplate;

    @LocalServerPort
    private int randomServerPort;

    @ParameterizedTest
    @ValueSource(strings = {"circle-black-simple.png", "v2/incidentId/circle-black-simple.png"})
    public void testFileProcessing(String relativePathForDelete) throws URISyntaxException, JsonProcessingException {
        String fileName = "circle-black-simple.png";

        String baseUrl = "http://localhost:" + randomServerPort + "/files";

        TestUtil.uploadFileOk("circle-black-simple.png", baseUrl, restTemplate);

        String getFileUrl = baseUrl + "/" + fileName;
        TestUtil.getFileOk(fileName, getFileUrl, restTemplate);

        String getFileUrl_v2 = baseUrl + "/v2/" + fileName + "?incidentId=incidentId";
        TestUtil.getFileOk(fileName, getFileUrl_v2, restTemplate);

        String getThumbNailUrl = baseUrl + "/thumb/" + fileName;
        TestUtil.getThumbNailOk(fileName, getThumbNailUrl, 4782, restTemplate);

        String getThumbNailUrl_v2 = baseUrl + "/thumb/v2/" + fileName + "?incidentId=incidentId";
        TestUtil.getThumbNailOk(fileName, getThumbNailUrl_v2, 4782, restTemplate);

        String deleteFileUrl = baseUrl + "/" + relativePathForDelete;
        TestUtil.deleteFile(deleteFileUrl, restTemplate);

        TestUtil.getFileNok(getFileUrl, HttpStatus.NOT_FOUND, restTemplate);
        TestUtil.getFileNok(getFileUrl_v2, HttpStatus.NOT_FOUND, restTemplate);
    }

}
