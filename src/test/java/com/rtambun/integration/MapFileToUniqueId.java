package com.rtambun.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rtambun.integration.container.FileMapRepositoryContainer;
import com.rtambun.integration.container.MinioClientContainer;
import com.rtambun.integration.container.MinioContainer;
import com.rtambun.integration.util.TestUtil;
import com.rtambun.minio.SpringBootMinioApplication;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.*;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = SpringBootMinioApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(initializers = {
        FileMapRepositoryContainer.Initializer.class,
        MinioContainer.Initializer.class
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@EnableAutoConfiguration
@Log4j2
public class MapFileToUniqueId {

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
    private TestRestTemplate restTemplate;

    @LocalServerPort
    private int randomServerPort;

    @Test
    public void testFileProcessing() throws URISyntaxException, JsonProcessingException {
        String fileName = "circle-black-simple.png";

        String baseUrl = "http://localhost:" + randomServerPort + "/files";

        //Upload file using v2 api
        //then check file is not readable using get file v1. only get file v2 will return the file
        String uploadFileUrl_v2 = baseUrl + "/v2/incidentId";
        TestUtil.uploadFileOk("circle-black-simple.png", uploadFileUrl_v2, restTemplate);
        String getFileUrl = baseUrl + "/" + fileName;
        TestUtil.getFileNok(getFileUrl, HttpStatus.NOT_FOUND, restTemplate);
        String getFileUrl_v2 = baseUrl + "/v2/" + fileName + "?incidentId=incidentId";
        TestUtil.getFileOk(fileName, getFileUrl_v2, restTemplate);

        // Get thumbnail v1 will return not found
        // Only get thumbnail v2 will return thumbnail file.
        String getThumbNailUrl = baseUrl + "/thumb/" + fileName;
        TestUtil.getThumbNailNok(getThumbNailUrl, HttpStatus.NOT_FOUND, restTemplate);

        String getThumbNailUrl_v2 = baseUrl + "/thumb/v2/" + fileName + "?incidentId=incidentId";
        TestUtil.getThumbNailOk(fileName, getThumbNailUrl_v2, 4782, restTemplate);

        //Delete file using v1 api will fail. Check that file can still be retrievable using get file v2 api
        String deleteFileUrl = baseUrl + "/" + fileName;
        TestUtil.deleteFile(deleteFileUrl, restTemplate);
        TestUtil.getFileNok(getFileUrl, HttpStatus.NOT_FOUND, restTemplate);
        TestUtil.getFileOk(fileName, getFileUrl_v2, restTemplate);

        //Delete file using v2 api is succeeded. Check that file can't be retrieved using both get fil v1 and v2 api
        String deleteFileUrl_v2 = baseUrl + "/v2/incidentId/" + fileName;
        TestUtil.deleteFile(deleteFileUrl_v2, restTemplate);
        TestUtil.getFileNok(getFileUrl, HttpStatus.NOT_FOUND, restTemplate);
        TestUtil.getFileNok(getFileUrl_v2, HttpStatus.NOT_FOUND, restTemplate);
    }

}
