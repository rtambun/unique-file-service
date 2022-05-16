package com.rtambun.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rtambun.integration.container.FileMapRepositoryContainer;
import com.rtambun.integration.container.MinioClientContainer;
import com.rtambun.integration.container.MinioContainer;
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
public class UploadFileWithoutIncidentId {

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
    public void testUploadFile() throws URISyntaxException, JsonProcessingException {

        String baseUrl = "http://localhost:" + randomServerPort + "/files";
        URI uri = new URI(baseUrl);

        MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        String path = "circle-black-simple.png";
        map.add("file", new ClassPathResource(path));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(map, headers);

        ResponseEntity<Object> response = restTemplate.postForEntity(uri, requestEntity, Object.class);

        assert response.getBody() != null;
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonPayload = objectMapper.writeValueAsString(response.getBody());
        HashMap<String, String> responseBody = objectMapper.readValue(jsonPayload, new TypeReference<>() {});
        assert responseBody != null;
        String success = responseBody.get("success");
        assertThat(success).isEqualTo("true");
        String url = responseBody.get("url");
        assertThat(url.contains(MinioContainer.MINIO_RESPONSE_URL)).isTrue();

        String fileName = url.replace(MinioContainer.MINIO_RESPONSE_URL, "");
        assertThat(fileName).isEqualTo(path);

        uri = new URI("http://localhost:" + randomServerPort + "/files/" + fileName);

        ResponseEntity<byte[]> getResponse = restTemplate.getForEntity(uri, byte[].class);
        ContentDisposition contentDisposition = getResponse.getHeaders().getContentDisposition();
        assertThat(contentDisposition.getFilename()).isEqualTo(path);
        assertThat(getResponse.getStatusCode().value()).isEqualTo(HttpStatus.OK.value());

        uri = new URI("http://localhost:" + randomServerPort + "/files/v2/" + fileName + "?incidentId=incidentId");

        getResponse = restTemplate.getForEntity(uri, byte[].class);
        assertThat(getResponse.getStatusCode().value()).isEqualTo(HttpStatus.NOT_FOUND.value());
    }

}
