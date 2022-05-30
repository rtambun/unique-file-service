package com.rtambun.minio.controller;

import com.jlefebure.spring.boot.minio.MinioService;
import com.rtambun.minio.service.FileService;
import com.rtambun.minio.service.FileServiceException;
import com.rtambun.minio.core.ImageService;
import com.rtambun.minio.core.UploadService;
import com.rtambun.minio.core.VideoService;
import com.rtambun.minio.dto.FileResponse;
import io.minio.messages.Item;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.*;
import org.springframework.mock.web.MockMultipartFile;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
    public void getObject_v1() throws FileServiceException {

        FileResponse fileResponse = new FileResponse("fileName.jpg", new byte[0]);
        when(mockFileService.getFile(any(), any())).thenReturn(fileResponse);

        ResponseEntity<byte[]> actual = fileUploadController.getObject("fileName.jpg");

        verify(mockFileService, times(1)).getFile(null, "fileName.jpg");

        assertThat(actual.getHeaders().getContentDisposition().getFilename()).isEqualTo("fileName.jpg");
        assert actual.getHeaders().getContentType() != null;
        assertThat(actual.getHeaders().getContentType().toString()).isEqualTo("image/jpeg");
        assertThat(actual.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void getObject_v1_ThrowFileException() throws FileServiceException {

        FileResponse fileResponse = new FileResponse("fileName.jpg", new byte[0]);
        when(mockFileService.getFile(any(), any())).thenThrow(new FileServiceException(FileServiceException.FILE_CANT_BE_READ));

        ResponseEntity<byte[]> actual = fileUploadController.getObject("fileName.jpg");

        verify(mockFileService, times(1)).getFile(null, "fileName.jpg");

        assertThat(actual.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void getObject_v1_ThrowIOException() throws FileServiceException {

        FileResponse fileResponse = new FileResponse("fileName.jpg", new byte[0]);
        when(mockFileService.getFile(any(), any())).thenThrow(new FileServiceException(FileServiceException.CONNECTION_ISSUE));

        ResponseEntity<byte[]> actual = fileUploadController.getObject("fileName.jpg");

        verify(mockFileService, times(1)).getFile(null, "fileName.jpg");

        assertThat(actual.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    public void getObject_v2() throws FileServiceException {

        FileResponse fileResponse = new FileResponse("fileName.jpg", new byte[0]);
        when(mockFileService.getFile(any(), any())).thenReturn(fileResponse);

        ResponseEntity<byte[]> actual = fileUploadController.getObject("fileName.jpg", "incidentId");

        verify(mockFileService, times(1)).getFile("incidentId", "fileName.jpg");

        assertThat(actual.getHeaders().getContentDisposition().getFilename()).isEqualTo("fileName.jpg");
        assert actual.getHeaders().getContentType() != null;
        assertThat(actual.getHeaders().getContentType().toString()).isEqualTo("image/jpeg");
        assertThat(actual.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void getObject_v2_ThrowFileException() throws FileServiceException {

        FileResponse fileResponse = new FileResponse("fileName.jpg", new byte[0]);
        when(mockFileService.getFile(any(), any())).thenThrow(new FileServiceException(FileServiceException.FILE_CANT_BE_READ));

        ResponseEntity<byte[]> actual = fileUploadController.getObject("fileName.jpg", "incidentId");

        verify(mockFileService, times(1)).getFile("incidentId", "fileName.jpg");

        assertThat(actual.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void getObject_v2_ThrowIOException() throws FileServiceException {

        FileResponse fileResponse = new FileResponse("fileName.jpg", new byte[0]);
        when(mockFileService.getFile(any(), any())).thenThrow(new FileServiceException(FileServiceException.CONNECTION_ISSUE));

        ResponseEntity<byte[]> actual = fileUploadController.getObject("fileName.jpg", "incidentId");

        verify(mockFileService, times(1)).getFile("incidentId", "fileName.jpg");

        assertThat(actual.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    public void getThumbNail_v1_getThumbNailImage() throws IOException, FileServiceException {
        String fileName = "circle-black-simple.png";
        InputStream inputStream = new FileInputStream(new ClassPathResource(fileName).getFile());
        when(mockImageService.getThumbnail(any(), any())).thenReturn(inputStream);

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentDisposition(ContentDisposition.builder("inline").filename(fileName).build());
        httpHeaders.setContentType(MediaType.IMAGE_JPEG);

        when(mockImageService.buildHttpHeader(any())).thenReturn(httpHeaders);

        ResponseEntity<byte[]> actual = fileUploadController.getThumbnail(fileName);

        inputStream = new FileInputStream(new ClassPathResource(fileName).getFile());

        ResponseEntity<byte[]> expected = new ResponseEntity<>(inputStream.readAllBytes(), httpHeaders, HttpStatus.OK);
        assertThat(actual).usingRecursiveComparison().isEqualTo(expected);

        verify(mockImageService, times(1)).getThumbnail(null, fileName);
        verify(mockImageService, times(1)).buildHttpHeader(fileName);
    }

    @ParameterizedTest
    @ValueSource(strings = {"sample.mp4", "sample.avi"})
    public void getThumbNail_v1_getThumbNailVideo(String objectName) throws IOException, FileServiceException {
        String fileName = "circle-black-simple.png";
        InputStream inputStream = new FileInputStream(new ClassPathResource(fileName).getFile());
        when(mockVideoService.getThumbnail(any(), any())).thenReturn(inputStream);

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentDisposition(ContentDisposition.builder("inline").filename(objectName).build());
        httpHeaders.setContentType(MediaType.IMAGE_JPEG);

        when(mockVideoService.buildHttpHeader(any())).thenReturn(httpHeaders);

        ResponseEntity<byte[]> actual = fileUploadController.getThumbnail(objectName);

        inputStream = new FileInputStream(new ClassPathResource(fileName).getFile());

        ResponseEntity<byte[]> expected = new ResponseEntity<>(inputStream.readAllBytes(), httpHeaders, HttpStatus.OK);
        assertThat(actual).usingRecursiveComparison().isEqualTo(expected);

        verify(mockVideoService, times(1)).getThumbnail(null, objectName);
        verify(mockVideoService, times(1)).buildHttpHeader(objectName);
    }

    @Test
    public void getThumbNail_v1_getThumbNailImage_FileServiceException() throws FileServiceException {
        when(mockImageService.getThumbnail(any(), any()))
                .thenThrow(new FileServiceException(FileServiceException.FILE_CANT_BE_FOUND));

        HttpHeaders httpHeaders = new HttpHeaders();
        String fileName = "circle.jpg";
        httpHeaders.setContentDisposition(ContentDisposition.builder("inline").filename(fileName).build());
        httpHeaders.setContentType(MediaType.IMAGE_JPEG);

        when(mockImageService.buildHttpHeader(any())).thenReturn(httpHeaders);

        ResponseEntity<byte[]> actual = fileUploadController.getThumbnail(fileName);

        ResponseEntity<byte[]> expected = new ResponseEntity<>(HttpStatus.NOT_FOUND);
        assertThat(actual).usingRecursiveComparison().isEqualTo(expected);


        verify(mockImageService, times(1)).getThumbnail(null, fileName);
        verify(mockImageService, times(0)).buildHttpHeader(any());
    }

    @Test
    public void getThumbNail_v1_getThumbNailImage_IOException() throws IOException, FileServiceException {
        String fileName = "circle-black-simple.png";
        InputStream inputStream = new FileInputStream(new ClassPathResource(fileName).getFile());
        inputStream.close();
        when(mockImageService.getThumbnail(any(), any())).thenReturn(inputStream);

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentDisposition(ContentDisposition.builder("inline").filename(fileName).build());
        httpHeaders.setContentType(MediaType.IMAGE_JPEG);

        when(mockImageService.buildHttpHeader(any())).thenReturn(httpHeaders);

        ResponseEntity<byte[]> actual = fileUploadController.getThumbnail(fileName);

        ResponseEntity<byte[]> expected = new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(actual).usingRecursiveComparison().isEqualTo(expected);

        verify(mockImageService, times(1)).getThumbnail(null, fileName);
        verify(mockImageService, times(0)).buildHttpHeader(any());
    }

    @Test
    public void getThumbNail_v2_getThumbNailImage() throws IOException, FileServiceException {
        String fileName = "circle-black-simple.png";
        InputStream inputStream = new FileInputStream(new ClassPathResource(fileName).getFile());
        when(mockImageService.getThumbnail(any(), any())).thenReturn(inputStream);

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentDisposition(ContentDisposition.builder("inline").filename(fileName).build());
        httpHeaders.setContentType(MediaType.IMAGE_JPEG);

        when(mockImageService.buildHttpHeader(any())).thenReturn(httpHeaders);

        ResponseEntity<byte[]> actual = fileUploadController.getThumbnail(fileName, "incidentId");

        inputStream = new FileInputStream(new ClassPathResource(fileName).getFile());

        ResponseEntity<byte[]> expected = new ResponseEntity<>(inputStream.readAllBytes(), httpHeaders, HttpStatus.OK);
        assertThat(actual).usingRecursiveComparison().isEqualTo(expected);

        verify(mockImageService, times(1)).getThumbnail("incidentId", fileName);
        verify(mockImageService, times(1)).buildHttpHeader(fileName);
    }

    @ParameterizedTest
    @ValueSource(strings = {"sample.mp4", "sample.avi"})
    public void getThumbNail_v2_getThumbNailVideo(String objectName) throws IOException, FileServiceException {
        String fileName = "circle-black-simple.png";
        InputStream inputStream = new FileInputStream(new ClassPathResource(fileName).getFile());
        when(mockVideoService.getThumbnail(any(), any())).thenReturn(inputStream);

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentDisposition(ContentDisposition.builder("inline").filename(objectName).build());
        httpHeaders.setContentType(MediaType.IMAGE_JPEG);

        when(mockVideoService.buildHttpHeader(any())).thenReturn(httpHeaders);

        ResponseEntity<byte[]> actual = fileUploadController.getThumbnail(objectName, "incidentId");

        inputStream = new FileInputStream(new ClassPathResource(fileName).getFile());

        ResponseEntity<byte[]> expected = new ResponseEntity<>(inputStream.readAllBytes(), httpHeaders, HttpStatus.OK);
        assertThat(actual).usingRecursiveComparison().isEqualTo(expected);

        verify(mockVideoService, times(1)).getThumbnail("incidentId", objectName);
        verify(mockVideoService, times(1)).buildHttpHeader(objectName);
    }

    @Test
    public void getThumbNail_v2_getThumbNailImage_FileServiceException() throws FileServiceException {
        when(mockImageService.getThumbnail(any(), any()))
                .thenThrow(new FileServiceException(FileServiceException.FILE_CANT_BE_FOUND));

        HttpHeaders httpHeaders = new HttpHeaders();
        String fileName = "circle-black-simple.png";
        httpHeaders.setContentDisposition(ContentDisposition.builder("inline").filename(fileName).build());
        httpHeaders.setContentType(MediaType.IMAGE_JPEG);

        when(mockImageService.buildHttpHeader(any())).thenReturn(httpHeaders);

        ResponseEntity<byte[]> actual = fileUploadController.getThumbnail(fileName, "incidentId");

        ResponseEntity<byte[]> expected = new ResponseEntity<>(HttpStatus.NOT_FOUND);
        assertThat(actual).usingRecursiveComparison().isEqualTo(expected);


        verify(mockImageService, times(1)).getThumbnail("incidentId", fileName);
        verify(mockImageService, times(0)).buildHttpHeader(any());
    }

    @Test
    public void getThumbNail_v2_getThumbNailImage_IOException() throws IOException, FileServiceException {
        String fileName = "circle-black-simple.png";
        InputStream inputStream = new FileInputStream(new ClassPathResource(fileName).getFile());
        inputStream.close();
        when(mockImageService.getThumbnail(any(), any())).thenReturn(inputStream);

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentDisposition(ContentDisposition.builder("inline").filename(fileName).build());
        httpHeaders.setContentType(MediaType.IMAGE_JPEG);

        when(mockImageService.buildHttpHeader(any())).thenReturn(httpHeaders);

        ResponseEntity<byte[]> actual = fileUploadController.getThumbnail(fileName, "incidentId");

        ResponseEntity<byte[]> expected = new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(actual).usingRecursiveComparison().isEqualTo(expected);

        verify(mockImageService, times(1)).getThumbnail("incidentId", fileName);
        verify(mockImageService, times(0)).buildHttpHeader(any());
    }

    @Test
    public void testMinio() {
        when(mockMinioService.list()).thenReturn(new ArrayList<>());

        List<Item> actual = fileUploadController.testMinio();

        assertThat(actual).isEmpty();
    }
}