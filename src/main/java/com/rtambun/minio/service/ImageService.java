package com.rtambun.minio.service;

import com.rtambun.minio.config.ApplicationProperties;
import lombok.extern.log4j.Log4j2;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;

import static com.rtambun.minio.service.Constants.DEFAULT_THUMBNAIL_HEIGHT_KEY;
import static com.rtambun.minio.service.Constants.DEFAULT_THUMBNAIL_WIDTH_KEY;


/**
 * This class is meant to handle image file related operations
 * like getting thumbnail, rotating an image and so on
 */
@Service
@Log4j2
public class ImageService implements IThumbnailService {

    private final ApplicationProperties applicationProperties;
    private final FileService fileService;

    public ImageService(ApplicationProperties applicationProperties, FileService fileService) {
        this.applicationProperties = applicationProperties;
        this.fileService = fileService;
    }

    /**
     * Get thumbnail for an image
     * @param incidentId corresponding to the fileName which thumbnail should be generated.
     *                   If value is null then file is stored without using unique id.
     * @param fileName which thumbnail should be generated
     * @return thumbnail of the image
     * @throws FileServiceException thrown when file is not found or issue with filestream generated
     */
    public InputStream getThumbnail(String incidentId, String fileName) throws FileServiceException {
        log.info("Resize Image with Input Stream");
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             InputStream inputStream = fileService.getFileAsInputStream(incidentId, fileName)) {
            Thumbnails.of(inputStream)
                    .size(Integer.parseInt(applicationProperties.getConfigValue(DEFAULT_THUMBNAIL_WIDTH_KEY)),
                            Integer.parseInt(applicationProperties.getConfigValue(DEFAULT_THUMBNAIL_HEIGHT_KEY)))
                    .toOutputStream(outputStream);
            return new ByteArrayInputStream(outputStream.toByteArray());
        } catch (IOException ex) {
            log.error("Issue when reading input stream to get image thumbnail");
            throw new FileServiceException(FileServiceException.CONNECTION_ISSUE);
        }
    }

    public HttpHeaders buildHttpHeader(String fileName) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(ContentDisposition.builder("inline")
                .filename(fileName)
                .build());
        headers.setContentType(MediaType.valueOf(URLConnection.guessContentTypeFromName(fileName)));
        return headers;
    }
}
