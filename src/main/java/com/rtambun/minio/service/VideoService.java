package com.rtambun.minio.service;

import com.rtambun.minio.config.ApplicationProperties;
import lombok.extern.log4j.Log4j2;
import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.filters.Rotation;
import org.bytedeco.javacv.*;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static com.rtambun.minio.service.Constants.*;


/**
 * This class is meant to handle video file related operations
 * like getting thumbnail of video
 */
@Service
@Log4j2
public class VideoService implements IThumbnailService{

    private static final String ROTATE = "rotate";

    private final ApplicationProperties applicationProperties;
    private final FileService fileService;

    public VideoService(ApplicationProperties applicationProperties, FileService fileService) {
        this.applicationProperties = applicationProperties;
        this.fileService = fileService;
    }

    /**
     * Get thumbnail from a video file's inputStream
     * @param incidentId of the video file that will be retrieved, null if video file is not stored using unique id
     * @param fileName of the video file
     * @return inputStream contains thumbnail of corresponding video
     * @throws FileServiceException thrown when file is not found or issue with filestream generated
     */
    public InputStream getThumbnail(String incidentId, String fileName) throws FileServiceException {
        log.info("Resize Image with BufferedImage");

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             InputStream inputStream = fileService.getFileAsInputStream(incidentId, fileName)) {
            BufferedImageWrapper bufferedImageWrapper = getBufferedImageFromVideo(inputStream);
            BufferedImage bufferedImage= bufferedImageWrapper.getBufferedImage();

            if (bufferedImage != null) {
                String rotationMetaData= bufferedImageWrapper.getRotation();
                if(rotationMetaData!=null && (!rotationMetaData.isEmpty())) {
                    try {
                        int iRotationMetaData = Integer.parseInt(rotationMetaData);
                        bufferedImage=Rotation.newRotator(iRotationMetaData).apply(bufferedImage);
                    }catch (NumberFormatException exception){
                        log.warn("rotation not a number, so no rotation done");
                    }
                }
                Thumbnails.of(bufferedImage)
                        .size(Integer.parseInt(applicationProperties.getConfigValue(DEFAULT_THUMBNAIL_WIDTH_KEY)),
                                Integer.parseInt(applicationProperties.getConfigValue(DEFAULT_THUMBNAIL_HEIGHT_KEY)))
                        .outputFormat(JPG)
                        .toOutputStream(outputStream);
            }
            else {
                log.info("Buffered Thumbnail is empty");
            }

            return new ByteArrayInputStream(outputStream.toByteArray());
        } catch (IOException exception) {
            log.info("Error when working with the stream {}", exception.getLocalizedMessage());
            throw new FileServiceException(FileServiceException.CONNECTION_ISSUE);
        }
    }

    public HttpHeaders buildHttpHeader(String fileName) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(ContentDisposition.builder("inline")
                .filename(fileName)
                .build());
        headers.setContentType(MediaType.IMAGE_JPEG);
        return headers;
    }

    /**
     * Grab an image from Frame grab of an video file's inputStream
     * @param inputStream - stream contain video data
     * @return buffered image
     * @throws IOException thrown when creating buffered image
     */
    private BufferedImageWrapper getBufferedImageFromVideo(InputStream inputStream) throws IOException {
        log.info("GenerateVideoThumbnail");
        FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(inputStream);
        grabber.setFormat(MP4);
        grabber.start();
        String rotate = grabber.getVideoMetadata(ROTATE);

        if(rotate!=null){
            log.info("rotate is "+rotate);
        }else {
            log.info("rotate is null");
        }

        /*
         * Get a frame in the middle of the video
         */
        int startFrame = grabber.getLengthInVideoFrames() / 2;
        grabber.setVideoFrameNumber(startFrame);
        BufferedImage bi = null;
        Java2DFrameConverter java2DFrameConverter = new Java2DFrameConverter();
        try {
            for (int i = startFrame; i < grabber.getLengthInFrames(); i++) {
                try {
                    Frame frame = grabber.grab();
                    if (frame == null) {
                        log.info("Frame is empty.");
                        continue;
                    }
                    bi = java2DFrameConverter.getBufferedImage(frame);
                    break;
                } catch (FrameGrabber.Exception t) {
                    log.error("Exception thrown."+t.getMessage());
                }
            }
        } finally {
            grabber.stop();
            grabber.close();
        }

        return new BufferedImageWrapper(bi,rotate);
    }
}
