package com.rtambun.minio.service;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class FileServiceException extends Throwable {
    public static final int FILE_CANT_BE_READ = 1;
    public static final int CONNECTION_ISSUE = 2;
    public static final int FILE_NAME_NOT_PROVIDED = 3;
    public static final int FILE_CANT_BE_FOUND = 4;

    private int status;
}
