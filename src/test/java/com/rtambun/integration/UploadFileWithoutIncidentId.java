package com.rtambun.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.rtambun.integration.util.TestUtil;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;

import java.net.URISyntaxException;

public class UploadFileWithoutIncidentId extends InitIntegrationTest{

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
