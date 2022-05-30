package com.rtambun.minio.core;

import com.rtambun.minio.config.ApplicationProperties;
import com.rtambun.minio.service.FileService;
import com.rtambun.minio.service.FileServiceException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

@Slf4j
class ImageServiceTest {
    private ApplicationProperties mockApplicationProperties;
    private FileService mockFileService;
    private ImageService imageService;

    @BeforeEach
    public void setUp() {
        mockApplicationProperties = mock(ApplicationProperties.class);
        mockFileService = mock(FileService.class);

        imageService = new ImageService(mockApplicationProperties, mockFileService);
    }

    @Test
    public void getThumbNailForImage() throws IOException, FileServiceException {
        InputStream inputStream = new FileInputStream(new ClassPathResource("circle-black-simple.png").getFile());
        when(mockFileService.getFileAsInputStream(any(), any())).thenReturn(inputStream);
        when(mockApplicationProperties.getConfigValue(Constants.DEFAULT_THUMBNAIL_WIDTH_KEY)).thenReturn("200");
        when(mockApplicationProperties.getConfigValue(Constants.DEFAULT_THUMBNAIL_HEIGHT_KEY)).thenReturn("200");

        ByteArrayInputStream thumbNailStream = (ByteArrayInputStream) imageService
                .getThumbnail("incidentId", "circle-black-simple.png");

        byte[] thumbNail = thumbNailStream.readAllBytes();
        log.info("Size of thumbnail {}", thumbNail.length);
        assertThat(thumbNail).isNotEmpty();
        assertThat(thumbNail.length).isEqualTo(4782);

        verify(mockFileService, times(1))
                .getFileAsInputStream("incidentId", "circle-black-simple.png");
    }

    @Test
    public void getThumbNailForImage_IOException() throws IOException, FileServiceException {
        InputStream inputStream = new FileInputStream(new ClassPathResource("circle-black-simple.png").getFile());
        inputStream.close();
        when(mockFileService.getFileAsInputStream(any(), any())).thenReturn(inputStream);
        when(mockApplicationProperties.getConfigValue(Constants.DEFAULT_THUMBNAIL_WIDTH_KEY)).thenReturn("200");
        when(mockApplicationProperties.getConfigValue(Constants.DEFAULT_THUMBNAIL_HEIGHT_KEY)).thenReturn("200");

        FileServiceException ex = assertThrows(FileServiceException.class, ()-> imageService
                .getThumbnail("incidentId", "circle-black-simple.png"));
        assertThat(ex.getStatus()).isEqualTo(FileServiceException.CONNECTION_ISSUE);

        verify(mockFileService, times(1))
                .getFileAsInputStream("incidentId", "circle-black-simple.png");
    }

    @ParameterizedTest
    @MethodSource(value = "getData_getHttpHeaders")
    public void getHttpHeaders(String fileName, MediaType mediaType) {
        HttpHeaders actual = imageService.buildHttpHeader(fileName);
        HttpHeaders expected = new HttpHeaders();
        expected.setContentDisposition(ContentDisposition.builder("inline").filename(fileName).build());
        expected.setContentType(mediaType);
        assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
    }

    static Stream<Arguments> getData_getHttpHeaders() {
        return Stream.of(Arguments.of("circle-black-simple.jpg", MediaType.IMAGE_JPEG),
                Arguments.of("circle-black-simple.gif", MediaType.IMAGE_GIF),
                Arguments.of("circle-black-simple.png", MediaType.IMAGE_PNG));
    }
}