package com.rtambun.minio.service;

import com.rtambun.minio.config.ApplicationProperties;
import lombok.extern.slf4j.Slf4j;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FrameGrabber;
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
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

@Slf4j
class VideoServiceTest {
    private ApplicationProperties mockApplicationProperties;
    private FileService mockFileService;
    private VideoService videoService;

    static {
        try {
            FFmpegFrameGrabber.tryLoad();
        } catch (FrameGrabber.Exception e) {
            e.printStackTrace();
        }
    }

    @BeforeEach
    public void setUp() {
        mockApplicationProperties = mock(ApplicationProperties.class);
        mockFileService = mock(FileService.class);
        videoService = new VideoService(mockApplicationProperties, mockFileService);
    }

    @Test
    public void getThumbnailForVideo() throws IOException, FileServiceException {
        InputStream inputStream = new FileInputStream(new ClassPathResource("sample.mp4").getFile());
        when(mockFileService.getFileAsInputStream(any(), any())).thenReturn(inputStream);
        when(mockApplicationProperties.getConfigValue(Constants.DEFAULT_THUMBNAIL_WIDTH_KEY)).thenReturn("200");
        when(mockApplicationProperties.getConfigValue(Constants.DEFAULT_THUMBNAIL_HEIGHT_KEY)).thenReturn("200");

        Instant before = Instant.now();
        ByteArrayInputStream thumbNailStream = (ByteArrayInputStream) videoService
                .getThumbnail("incidentId", "sample.mp4");
        Instant after = Instant.now();
        log.info("Time to execute get thumbnail for video {}" , Duration.between(before, after));

        byte[] thumbNail = thumbNailStream.readAllBytes();
        log.info("Size of thumbnail {}", thumbNail.length);
        assertThat(thumbNail).isNotEmpty();
        assertThat(thumbNail.length).isEqualTo(4878);

        FileInputStream fis = new FileInputStream(new ClassPathResource("sample-thumb.jpg").getFile());
        byte[] expectedThumbnail = fis.readAllBytes();
        assertThat(thumbNail).containsExactly(expectedThumbnail);

        verify(mockFileService, times(1))
                .getFileAsInputStream("incidentId", "sample.mp4");

        assertThat(Duration.between(before, after).get(ChronoUnit.SECONDS)).isLessThan(1);
    }

    @ParameterizedTest
    @MethodSource(value = "getData_getHttpHeaders")
    public void getHttpHeaders(String fileName, MediaType mediaType) {
        HttpHeaders actual = videoService.buildHttpHeader(fileName);
        HttpHeaders expected = new HttpHeaders();
        expected.setContentDisposition(ContentDisposition.builder("inline").filename(fileName).build());
        expected.setContentType(mediaType);
        assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
    }

    static Stream<Arguments> getData_getHttpHeaders() {
        return Stream.of(Arguments.of("circle.mp4", MediaType.IMAGE_JPEG),
                Arguments.of("circle.avi", MediaType.IMAGE_JPEG));
    }
}