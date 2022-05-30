package com.rtambun.integration.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rtambun.integration.container.MinioContainer;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;

public class TestUtil {

    public static void uploadFileOk(String fileName,
                                    String uploadFileUrl,
                                    TestRestTemplate restTemplate) throws URISyntaxException, JsonProcessingException {
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        map.add("file", new ClassPathResource(fileName));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(map, headers);
        ResponseEntity<Object> response = restTemplate.postForEntity(
                new URI(uploadFileUrl), requestEntity, Object.class);

        assert response.getBody() != null;
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonPayload = objectMapper.writeValueAsString(response.getBody());
        HashMap<String, String> responseBody = objectMapper.readValue(jsonPayload, new TypeReference<>() {});
        assert responseBody != null;

        String success = responseBody.get("success");
        assertThat(success).isEqualTo("true");

        String url = responseBody.get("url");
        assertThat(url.contains(MinioContainer.MINIO_RESPONSE_URL)).isTrue();
        String responseFileName = url.replace(MinioContainer.MINIO_RESPONSE_URL, "");
        assertThat(responseFileName).isEqualTo(fileName);
    }

    public static void getFileOk(String fileName,
                                 String getFileUrl,
                                 TestRestTemplate restTemplate) throws URISyntaxException {
        URI uri = new URI(getFileUrl);

        ResponseEntity<byte[]> getResponse = restTemplate.getForEntity(uri, byte[].class);
        ContentDisposition contentDisposition = getResponse.getHeaders().getContentDisposition();
        assertThat(contentDisposition.getFilename()).isEqualTo(fileName);
        assertThat(getResponse.getStatusCode().value()).isEqualTo(HttpStatus.OK.value());
    }

    public static void getFileNok(String getFileUrl,
                                 HttpStatus httpStatus,
                                 TestRestTemplate restTemplate) throws URISyntaxException {
        URI uri = new URI(getFileUrl);

        ResponseEntity<byte[]> getResponse = restTemplate.getForEntity(uri, byte[].class);
        ContentDisposition contentDisposition = getResponse.getHeaders().getContentDisposition();
        assertThat(contentDisposition.getFilename()).isNull();
        assertThat(getResponse.getStatusCode()).isEqualTo(httpStatus);
    }

    public static void deleteFile(String deleteFileUrl,
                                  TestRestTemplate restTemplate) throws URISyntaxException {
        restTemplate.delete(new URI(deleteFileUrl));
    }

    public static void getThumbNailOk(String fileName,
                                      String getThumbnailUrl,
                                      long thumbNailSize,
                                      TestRestTemplate restTemplate) throws URISyntaxException {
        ResponseEntity<byte[]> getResponse = restTemplate.getForEntity(new URI(getThumbnailUrl), byte[].class);
        ContentDisposition contentDisposition = getResponse.getHeaders().getContentDisposition();
        assertThat(contentDisposition.getFilename()).isEqualTo(fileName);
        assertThat(getResponse.getStatusCode().value()).isEqualTo(HttpStatus.OK.value());
        assert getResponse.getBody() != null;
        assertThat(getResponse.getBody().length).isEqualTo(thumbNailSize);
    }

    public static void getThumbNailNok(String getThumbnailUrl,
                                      HttpStatus httpStatus,
                                      TestRestTemplate restTemplate) throws URISyntaxException {
        ResponseEntity<byte[]> getResponse = restTemplate.getForEntity(new URI(getThumbnailUrl), byte[].class);
        ContentDisposition contentDisposition = getResponse.getHeaders().getContentDisposition();
        assertThat(contentDisposition.getFilename()).isNull();
        assertThat(getResponse.getStatusCode()).isEqualTo(httpStatus);
        assertThat(getResponse.getBody()).isNull();
    }
}
