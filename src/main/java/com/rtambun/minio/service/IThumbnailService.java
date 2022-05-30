package com.rtambun.minio.service;

import com.rtambun.minio.service.FileServiceException;
import org.springframework.http.HttpHeaders;

import java.io.InputStream;

public interface IThumbnailService {
    InputStream getThumbnail(String incidentId, String fileName) throws FileServiceException;
    HttpHeaders buildHttpHeader(String fileName);
}
