package com.rtambun.minio.service;

import com.jlefebure.spring.boot.minio.MinioException;
import com.jlefebure.spring.boot.minio.MinioService;
import com.rtambun.minio.dto.FileResponse;
import com.rtambun.minio.model.FileMap;
import com.rtambun.minio.repository.FileMapRepository;
import com.rtambun.minio.util.UUIDProvider;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.UUID;

import static java.nio.file.Path.of;

@Service
@Log4j2
public class FileService {

    private final FileMapRepository fileMapRepository;
    private final MinioService minioService;
    private final UUIDProvider uuidProvider;

    public FileService(FileMapRepository fileMapRepository,
                       MinioService minioService,
                       UUIDProvider uuidProvider,
                       @Value("${minio.response.url}") String url) {
        this.fileMapRepository = fileMapRepository;
        this.minioService = minioService;
        this.uuidProvider = uuidProvider;
    }

    public FileResponse addFile(String incidentId, MultipartFile multipartFile) throws FileServiceException {
        String fileName = multipartFile.getOriginalFilename();
        if (fileName == null || fileName.isEmpty() || fileName.isBlank()) {
            throw new FileServiceException(FileServiceException.FILE_NAME_NOT_PROVIDED);
        }

        FileMap fileMap = fileMapRepository.findFileMapByIncidentIdAndFileName(incidentId, fileName);
        String mappedFileName;

        String[] processStringForExtension =  fileName.split("\\.");
        String ext = "";
        if (processStringForExtension.length == 2) {
            ext = processStringForExtension[1];
        }

        if (fileMap == null || fileMap.getId() == null) {
            do {
                UUID uuid = uuidProvider.randomUUID();
                mappedFileName = uuid.toString();
                mappedFileName += (ext.isEmpty() ? "" : "." + ext);
                fileMap = fileMapRepository.findFileMapByMappedFileName(mappedFileName);
            } while (!(fileMap == null || fileMap.getId() == null));

            fileMap = new FileMap(null, incidentId, fileName, mappedFileName);
            fileMapRepository.save(fileMap);
        } else {
            mappedFileName = fileMap.getMappedFileName();
        }

        try {
            HashMap<String, String> header = new HashMap<>();
            Path path = of(mappedFileName).normalize();
            minioService.upload(path, multipartFile.getInputStream(), multipartFile.getContentType(), header);
            return new FileResponse(fileName, null);
        } catch (MinioException | IOException e) {
            log.error(String.format("metadata for %s", e));
            throw new FileServiceException(FileServiceException.CONNECTION_ISSUE);
        }
    }

    public FileResponse getFile(String incidentId, String fileName) throws FileServiceException {
        try (InputStream inputStream = getFileAsInputStream(incidentId, fileName)) {
            return new FileResponse(fileName, inputStream.readAllBytes());
        } catch (IOException ioException) {
            log.error("Failed while connecting to minio: {}. Exception : {}", fileName, ioException);
            throw new FileServiceException(FileServiceException.CONNECTION_ISSUE);
        }
    }

    public InputStream getFileAsInputStream(String incidentId, String fileName) throws FileServiceException {
        FileMap fileMap;
        if (incidentId == null) {
            fileMap  = new FileMap(null, null, fileName, fileName);
        } else {
            fileMap = fileMapRepository.findFileMapByIncidentIdAndFileName(incidentId, fileName);
            if (fileMap == null || fileMap.getId() == null || fileMap.getId().isEmpty()) {
                fileMap  = new FileMap(null, null, fileName, fileName);
            }
        }

        InputStream inputStream;
        try {
            inputStream = minioService.get(of(fileMap.getMappedFileName()));
        } catch (MinioException minioException) {
            log.error("Failed to retrieve object: {}. Exception : {}}", fileName, minioException);
            throw new FileServiceException(FileServiceException.FILE_CANT_BE_READ);
        }
        return inputStream;
    }

    public FileResponse deleteFile(String incidentId, String fileName) throws FileServiceException {
        String fileNameToBeDeleted = fileName;
        if (incidentId != null) {
            FileMap fileMap = fileMapRepository.findFileMapByIncidentIdAndFileName(incidentId, fileName);
            if (fileMap != null && fileMap.getId() != null && !fileMap.getId().isEmpty()) {
                fileNameToBeDeleted = fileMap.getMappedFileName();
            }
        }
        try {
            minioService.remove(of(fileNameToBeDeleted));
        } catch (MinioException minioException) {
            throw new FileServiceException(FileServiceException.FILE_CANT_BE_FOUND);
        }
        return new FileResponse(fileName, null);
    }
}
