package com.rtambun.minio.controller;

import com.google.api.client.util.IOUtils;
import com.jlefebure.spring.boot.minio.MinioException;
import com.jlefebure.spring.boot.minio.MinioService;
import com.rtambun.minio.core.IThumbnailService;
import com.rtambun.minio.core.ImageService;
import com.rtambun.minio.core.UploadService;
import com.rtambun.minio.core.VideoService;
import com.rtambun.minio.dto.FileResponse;
import com.rtambun.minio.service.FileService;
import com.rtambun.minio.service.FileServiceException;
import io.minio.messages.Item;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.List;

import static com.rtambun.minio.core.Constants.*;
import static java.nio.file.Path.of;

@RestController
@RequestMapping("/files")
public class FileUploadController {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileUploadController.class);

    private final FileService fileService;
    private final MinioService minioService;
    private final ImageService imageService;
    private final VideoService videoService;
    private final UploadService uploadService;
    private final String url;

    private static final String SUCCESS = "success";
    private static final String FALSE = "false";
    private static final String TRUE = "true";
    private static final String URL = "url";

    public FileUploadController(FileService fileService,
                                MinioService minioService,
                                ImageService imageService,
                                VideoService videoService,
                                UploadService uploadService,
                                @Value("${minio.response.url}") String url) {
        this.fileService = fileService;
        this.minioService = minioService;
        this.imageService = imageService;
        this.videoService = videoService;
        this.uploadService = uploadService;
        this.url = url;
    }

    @GetMapping
    public List<Item> testMinio() {
        LOGGER.info("Get All Files");
        return minioService.list();
    }

    @PostMapping
    public ResponseEntity<Object> addAttachment(@NotNull @RequestParam("file") MultipartFile file) {
        return uploadService.addAttachment(file,url,minioService);
    }

    @PostMapping("/v2/{incidentId}")
    public ResponseEntity<Object> addAttachment(@PathVariable("incidentId") String incidentId,
                                                @NotNull @RequestParam("file") MultipartFile file) {

        HashMap<String, String> responseObj = new HashMap<>();
        HttpStatus status;

        try {
            FileResponse fileResponse = fileService.addFile(incidentId, file);
            responseObj.put(SUCCESS, TRUE);
            responseObj.put(URL, url + fileResponse.getFileName());
            status = HttpStatus.OK;
        } catch (FileServiceException exception) {
            LOGGER.error("Error while add attachment to storage, {}", exception.getStatus());
            responseObj.put(SUCCESS, FALSE);
            if (exception.getStatus() == FileServiceException.FILE_NAME_NOT_PROVIDED) {
                status = HttpStatus.UNPROCESSABLE_ENTITY;
            } else {
                status = HttpStatus.INTERNAL_SERVER_ERROR;
            }
        }
        return new ResponseEntity<>(responseObj, status);
    }

    @DeleteMapping("/{object}")
    public void deleteObj(@PathVariable("object") String object, HttpServletResponse response) throws MinioException, IOException {
        minioService.remove(of(object));

        // Set the content type and attachment header.
        response.addHeader(CONTENT_DISPOSITION, ATTACHMENT_FILENAME + object);
        response.setContentType(URLConnection.guessContentTypeFromName(object));

        // Copy the stream to the response's output stream.
        response.flushBuffer();
    }

    @GetMapping("/{object}")
    public ResponseEntity<byte[]> getObject(@PathVariable("object") String fileName) {
        return retrieveFile(null, fileName);
    }

    @GetMapping("/v2/{object}")
    public ResponseEntity<byte[]> getObject(@PathVariable("object") String fileName,
                                            @RequestParam("incidentId") String incidentId) {
        return retrieveFile(incidentId, fileName);
    }

    private ResponseEntity<byte[]> retrieveFile(String incidentId, String fileName) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        ResponseEntity<byte[]> responseEntity;

        try {
            FileResponse fileResponse = fileService.getFile(incidentId, fileName);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentDisposition(ContentDisposition.builder("inline")
                    .filename(fileResponse.getFileName())
                    .build());
            headers.setContentType(MediaType.valueOf(URLConnection.guessContentTypeFromName(fileName)));
            responseEntity = new ResponseEntity<>(fileResponse.getFileContent(), headers, HttpStatus.OK);
        } catch (FileServiceException e) {
            responseEntity = new ResponseEntity<>(FileServiceException.mapExceptionToHttpStatus(e, fileName));
        }

        stopWatch.stop();
        LOGGER.info(String.format("Get %s in %d ms", fileName, stopWatch.getTotalTimeMillis()));
        return responseEntity;
    }

    @GetMapping("thumb/{object}")
    public ResponseEntity<byte[]> getThumbnail(@PathVariable("object") String object) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        LOGGER.info("Get thumbnail");

        ResponseEntity<byte[]> response = retrieveThumbnailCommon(null, object);

        stopWatch.stop();
        LOGGER.info(String.format("Thumbnail generated for %s in %d ms", object, stopWatch.getTotalTimeMillis()));

        return response;
    }

    @GetMapping("thumb/v2/{object}")
    public ResponseEntity<byte[]> getThumbnail(@PathVariable("object") String object,
                                               @RequestParam("incidentId") String incidentId) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        LOGGER.info("Get thumbnail v2");

        ResponseEntity<byte[]> response = retrieveThumbnailCommon(incidentId, object);

        stopWatch.stop();
        LOGGER.info(String.format("Thumbnail generated for %s in %d ms", object, stopWatch.getTotalTimeMillis()));
        return response;
    }

    private ResponseEntity<byte[]> retrieveThumbnailCommon(String incidentId, String fileName) {
        IThumbnailService thumbnailService;
        String extension = fileName.substring(fileName.lastIndexOf('.') + 1);
        if (extension.equals(MP4) || extension.equals(AVI) ) {
            thumbnailService = videoService;
        } else {
            thumbnailService = imageService;
        }

        ResponseEntity<byte[]> response;
        try (InputStream thumbnailStream = thumbnailService.getThumbnail(incidentId, fileName)) {
            response = new ResponseEntity<>(thumbnailStream.readAllBytes(),
                    thumbnailService.buildHttpHeader(fileName),
                    HttpStatus.OK);
        }
        catch (FileServiceException ex) {
            LOGGER.error(String.format("Failed to retrieve object: %s. Exception : %s", fileName, ex));
            response = new ResponseEntity<>(FileServiceException.mapExceptionToHttpStatus(ex, fileName));
        } catch (IOException ex) {
            LOGGER.error("Issue when reading thumbnail stream, {}", ex.getMessage());
            response = new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return response;
    }
}
