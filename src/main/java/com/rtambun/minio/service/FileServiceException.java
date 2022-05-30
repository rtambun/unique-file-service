package com.rtambun.minio.service;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
@Slf4j
public class FileServiceException extends Throwable {
    public static final int FILE_CANT_BE_READ = 1;
    public static final int CONNECTION_ISSUE = 2;
    public static final int FILE_NAME_NOT_PROVIDED = 3;
    public static final int FILE_CANT_BE_FOUND = 4;

    private int status;

    public static HttpStatus mapExceptionToHttpStatus(FileServiceException e, String fileName) {
        if (e.getStatus() == FileServiceException.FILE_CANT_BE_READ ||
                e.getStatus() == FileServiceException.FILE_CANT_BE_FOUND) {
            log.error("Failed to retrieve object: {}. Exception : {}", fileName, e);
            return HttpStatus.NOT_FOUND;
        } else if (e.getStatus() == FileServiceException.FILE_NAME_NOT_PROVIDED) {
            log.error("File not name not provided for the operation");
            return HttpStatus.UNPROCESSABLE_ENTITY;
        } else {
            if (e.getStatus() == FileServiceException.CONNECTION_ISSUE) {
                log.error("Failed to send response. Exception : {}", e.getMessage());
            } else {
                log.error("Unknown exception");
            }
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }
}
