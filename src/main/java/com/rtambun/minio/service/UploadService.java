package com.rtambun.minio.service;

import com.jlefebure.spring.boot.minio.MinioException;
import com.jlefebure.spring.boot.minio.MinioService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;

import static java.nio.file.Path.of;

/**
 * This class is meant to handle upload of a file into minio
 */
@Service
public class UploadService {

    private static final String SUCCESS = "success";
    private static final String FALSE = "false";
    private static final String TRUE = "true";
    private static final String URL = "url";

    private static final Logger LOGGER = LoggerFactory.getLogger(UploadService.class);


    public ResponseEntity<Object> addAttachment(@NotNull MultipartFile file, String url, MinioService minioService){
        HashMap<String, String> successObj = new HashMap<>();
        HashMap<String, String> failObj = new HashMap<>();
        HashMap<String, String> header = new HashMap<>();

        try {
            String fileName = getValidatedFileName(file);
            if(fileName.isEmpty()) {
                successObj.put(SUCCESS, FALSE);
                return new ResponseEntity<>(failObj, HttpStatus.OK);
            }
            Path path = of(fileName).normalize();
            minioService.upload(path, file.getInputStream(), file.getContentType(), header);
            successObj.put(SUCCESS, TRUE);
            successObj.put(URL, url + path);
            return new ResponseEntity<>(successObj, HttpStatus.OK);
        } catch (MinioException | IOException e) {
            LOGGER.error(String.format("metadata for %s", e));
            successObj.put(SUCCESS, FALSE);
            return new ResponseEntity<>(failObj, HttpStatus.OK);
        }
    }
    private String getValidatedFileName(@NotNull MultipartFile file) {
        String fileName = file.getOriginalFilename();
        if(fileName == null)
            return "";

        int startIndex = fileName.replace("\\\\", "/").lastIndexOf("/");
        fileName = fileName.substring(startIndex + 1);
        return fileName;
    }
}
