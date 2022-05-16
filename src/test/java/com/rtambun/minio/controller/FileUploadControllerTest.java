package com.rtambun.minio.controller;

import com.jlefebure.spring.boot.minio.MinioService;
import com.rtambun.minio.controller.FileUploadController;
import com.rtambun.minio.service.FileService;
import com.rtambun.minio.service.FileServiceException;
import com.rtambun.minio.core.ImageService;
import com.rtambun.minio.core.UploadService;
import com.rtambun.minio.core.VideoService;
import com.rtambun.minio.dto.FileResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import java.util.HashMap;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class FileUploadControllerTest {

    private FileService mockFileService;
    private MinioService mockMinioService;
    private ImageService mockImageService;
    private VideoService mockVideoService;
    private UploadService mockUploadService;
    private String url;

    private FileUploadController fileUploadController;
    private MockMultipartFile mockMultipartFile;

    @BeforeEach
    void setUp() {
        mockFileService = mock(FileService.class);
        mockMinioService = mock(MinioService.class);
        mockImageService = mock(ImageService.class);
        mockVideoService = mock(VideoService.class);
        mockUploadService = mock(UploadService.class);
        url = "http://test/";
        fileUploadController = new FileUploadController(mockFileService,
                mockMinioService,
                mockImageService,
                mockVideoService,
                mockUploadService,
                url);
        mockMultipartFile = mock(MockMultipartFile.class);
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    public void addAttachmentWithIncidentId() throws FileServiceException {
        FileResponse fileResponse = new FileResponse("fileName.jpg", null);
        when(mockFileService.addFile(any(), any())).thenReturn(fileResponse);

        ResponseEntity<Object> actual = fileUploadController.addAttachment("incidentId", mockMultipartFile);

        verify(mockFileService, times(1)).addFile(eq("incidentId"), any());

        HashMap<String, String> successObj = new HashMap<>();
        successObj.put("success", "true");
        successObj.put("url", "http://test/fileName.jpg");
        ResponseEntity<Object> expected = new ResponseEntity<>(successObj, HttpStatus.OK);
        assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
    }

    @ParameterizedTest
    @MethodSource(value = "getData_addAttachmentWithIncidentId_AddFileThrowException")
    public void addAttachmentWithIncidentId_AddFileThrowException(FileServiceException fileServiceException,
                                                                  HttpStatus httpStatus)
            throws FileServiceException {
        FileResponse fileResponse = new FileResponse("fileName.jpg", null);
        when(mockFileService.addFile(any(), any())).thenThrow(fileServiceException);

        ResponseEntity<Object> actual = fileUploadController.addAttachment("incidentId", mockMultipartFile);

        verify(mockFileService, times(1)).addFile(eq("incidentId"), any());

        HashMap<String, String> successObj = new HashMap<>();
        successObj.put("success", "false");
        ResponseEntity<Object> expected = new ResponseEntity<>(successObj, httpStatus);
        assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
    }

    static Stream<Arguments> getData_addAttachmentWithIncidentId_AddFileThrowException() {
        return Stream.of(
                Arguments.of(new FileServiceException(FileServiceException.FILE_NAME_NOT_PROVIDED),
                        HttpStatus.UNPROCESSABLE_ENTITY),
                Arguments.of(new FileServiceException(FileServiceException.CONNECTION_ISSUE),
                        HttpStatus.INTERNAL_SERVER_ERROR));
    }

    @Test
    public void getObject() throws FileServiceException {

        FileResponse fileResponse = new FileResponse("fileName.jpg", new byte[0]);
        when(mockFileService.getFile(any())).thenReturn(fileResponse);

        ResponseEntity<byte[]> actual = fileUploadController.getObject("fileName.jpg");

        verify(mockFileService, times(1)).getFile("fileName.jpg");

        assertThat(actual.getHeaders().getContentDisposition().getFilename()).isEqualTo("fileName.jpg");
        assert actual.getHeaders().getContentType() != null;
        assertThat(actual.getHeaders().getContentType().toString()).isEqualTo("image/jpeg");
        assertThat(actual.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void getObject_ThrowFileException() throws FileServiceException {

        FileResponse fileResponse = new FileResponse("fileName.jpg", new byte[0]);
        when(mockFileService.getFile(any())).thenThrow(new FileServiceException(FileServiceException.FILE_CANT_BE_READ));

        ResponseEntity<byte[]> actual = fileUploadController.getObject("fileName.jpg");

        verify(mockFileService, times(1)).getFile("fileName.jpg");

        assertThat(actual.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void getObject_ThrowIOException() throws FileServiceException {

        FileResponse fileResponse = new FileResponse("fileName.jpg", new byte[0]);
        when(mockFileService.getFile(any())).thenThrow(new FileServiceException(FileServiceException.CONNECTION_ISSUE));

        ResponseEntity<byte[]> actual = fileUploadController.getObject("fileName.jpg");

        verify(mockFileService, times(1)).getFile("fileName.jpg");

        assertThat(actual.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}