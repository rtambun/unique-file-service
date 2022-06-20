
package com.rtambun.minio;

import com.jlefebure.spring.boot.minio.MinioException;
import com.jlefebure.spring.boot.minio.MinioService;
import com.jlefebure.spring.boot.minio.notification.MinioNotification;
import io.minio.notification.NotificationInfo;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Log4j2
public class DummyService {

    @Autowired
    private MinioService minioService;

    @MinioNotification({"s3:ObjectCreated:Put"})
    public void handleUpload(NotificationInfo notificationInfo) {
        log.info(Arrays
            .stream(notificationInfo.records)
            .map(notificationEvent -> "Receiving event " + notificationEvent.eventName + " for " + notificationEvent.s3.object.key)
            .collect(Collectors.joining(","))
        );
    }

    @MinioNotification(value = {"s3:ObjectAccessed:Get"}, suffix = ".pdf")
    public void handleGetPdf(NotificationInfo notificationInfo) {
        Arrays.stream(notificationInfo.records)
                .map(notificationEvent -> {
                    try {
                        return minioService.getMetadata(Path.of(notificationEvent.s3.object.key));
                    } catch (MinioException e) {
                        log.info(e.toString());
                        return null;
                    }

                })
                .filter(Objects::nonNull)
                .forEach(objectStat -> log.info("metadata for " + objectStat.name() + objectStat.httpHeaders()));
    }

    public String helloWorld()
    {
        return "Hello World!";
    }

}
