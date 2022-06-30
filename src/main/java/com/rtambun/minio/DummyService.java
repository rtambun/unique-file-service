
package com.rtambun.minio;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jlefebure.spring.boot.minio.MinioException;
import com.jlefebure.spring.boot.minio.MinioService;
import com.jlefebure.spring.boot.minio.notification.MinioNotification;
import io.minio.messages.NotificationRecords;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Log4j2
public class DummyService {

    @Autowired
    private MinioService minioService;

    @MinioNotification({"s3:ObjectCreated:Put"})
    public void handleUpload(NotificationRecords notificationRecords) {
        log.info(notificationRecords.events()
            .stream()
            .map(notificationEvent -> "Receiving event " + notificationEvent.eventType().name() + " for " + notificationEvent.objectName())
            .collect(Collectors.joining(","))
        );
    }

    @MinioNotification(value = {"s3:ObjectAccessed:Get"}, suffix = ".pdf")
    public void handleGetPdf(NotificationRecords notificationRecords) {
        notificationRecords.events().stream()
                .map(notificationEvent -> {
                    try {
                        return minioService.getMetadata(Path.of(notificationEvent.objectName()));
                    } catch (MinioException e) {
                        log.info(e.toString());
                        return null;
                    }

                })
                .filter(Objects::nonNull)
                .forEach(objectStat -> log.info("metadata for " + objectStat.object() + "." + objectStat.contentType()));
    }

    @KafkaListener(topics = "${kafka.topic.minio.event}", groupId = "${kafka.client.id}")
    public void listenKafkaNotification(ObjectNode objectNode) {
        log.info("Receiving notification event from kafka");
    }

    public String helloWorld()
    {
        return "Hello World!";
    }

}
