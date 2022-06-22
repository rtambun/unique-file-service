package com.rtambun.minio.controller;

import com.jlefebure.spring.boot.minio.MinioService;
import com.rtambun.minio.service.IThumbnailService;
import com.rtambun.minio.service.ImageService;
import com.rtambun.minio.service.UploadService;
import com.rtambun.minio.service.VideoService;
import com.rtambun.minio.dto.FileResponse;
import com.rtambun.minio.service.FileService;
import com.rtambun.minio.service.FileServiceException;
import io.minio.messages.Item;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.List;

import static com.rtambun.minio.service.Constants.*;

@RestController
@RequestMapping("/files")
@Log4j2
public class FileUploadController {
    private final FileService fileService;
    private final MinioService minioService;
    private final ImageService imageService;
    private final VideoService videoService;
    private final String url;

    private static final String SUCCESS = "success";
    private static final String FALSE = "false";
    private static final String TRUE = "true";
    private static final String URL = "url";

    public FileUploadController(FileService fileService,
                                MinioService minioService,
                                ImageService imageService,
                                VideoService videoService,
                                @Value("${minio.response.url}") String url) {
        this.fileService = fileService;
        this.minioService = minioService;
        this.imageService = imageService;
        this.videoService = videoService;
        this.url = url;
    }

    @GetMapping
    public List<Item> testMinio() {
        log.info("Get All Files");
        return minioService.list();
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Object> addAttachment(@NotNull @RequestPart("file") MultipartFile file) {
        return addFile(null, file);
    }

    @PostMapping(value = "/v2/{incidentId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Object> addAttachment(@PathVariable("incidentId") String incidentId,
                                                @NotNull @RequestPart("file") MultipartFile file) {
        return addFile(incidentId, file);
    }

    private ResponseEntity<Object> addFile(String incidentId,
                                           MultipartFile file) {
        HashMap<String, String> responseObj = new HashMap<>();
        HttpStatus status;

        try {
            FileResponse fileResponse = fileService.addFile(incidentId, file);
            responseObj.put(SUCCESS, TRUE);
            responseObj.put(URL, url + fileResponse.getFileName());
            status = HttpStatus.OK;
        } catch (FileServiceException exception) {
            log.error("Error while add attachment to storage, {}", exception.getStatus());
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
    public ResponseEntity<Object> deleteObj(@PathVariable("object") String object) {
        return removeObj(null, object);
    }

    @DeleteMapping("/v2/{incidentId}/{object}")
    public ResponseEntity<Object> deleteObj(@PathVariable("incidentId") String incidentId,
                                            @PathVariable("object") String object) {
        return removeObj(incidentId, object);
    }

    private ResponseEntity<Object> removeObj(String incidentId, String object) {
        try {
            FileResponse fileResponse = fileService.deleteFile(incidentId, object);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentDisposition(ContentDisposition.builder("inline")
                    .filename(fileResponse.getFileName())
                    .build());
            String contentType = URLConnection.guessContentTypeFromName(fileResponse.getFileName());
            headers.setContentType(MediaType.valueOf(contentType));
            return new ResponseEntity<>(headers, HttpStatus.OK);
        } catch (FileServiceException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
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
        log.info(String.format("Get %s in %d ms", fileName, stopWatch.getTotalTimeMillis()));
        return responseEntity;
    }

    @GetMapping("thumb/{object}")
    public ResponseEntity<byte[]> getThumbnail(@PathVariable("object") String object) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        log.info("Get thumbnail");

        ResponseEntity<byte[]> response = retrieveThumbnailCommon(null, object);

        stopWatch.stop();
        log.info(String.format("Thumbnail generated for %s in %d ms", object, stopWatch.getTotalTimeMillis()));

        return response;
    }

    @GetMapping("thumb/v2/{object}")
    public ResponseEntity<byte[]> getThumbnail(@PathVariable("object") String object,
                                               @RequestParam("incidentId") String incidentId) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        log.info("Get thumbnail v2");

        ResponseEntity<byte[]> response = retrieveThumbnailCommon(incidentId, object);

        stopWatch.stop();
        log.info(String.format("Thumbnail generated for %s in %d ms", object, stopWatch.getTotalTimeMillis()));
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
            log.error(String.format("Failed to retrieve object: %s. Exception : %s", fileName, ex));
            response = new ResponseEntity<>(FileServiceException.mapExceptionToHttpStatus(ex, fileName));
        } catch (IOException ex) {
            log.error("Issue when reading thumbnail stream, {}", ex.getMessage());
            response = new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return response;
    }
}
