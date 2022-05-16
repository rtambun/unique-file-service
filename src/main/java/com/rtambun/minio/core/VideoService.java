package com.rtambun.minio.core;

import com.rtambun.minio.config.ApplicationProperties;
import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.filters.Rotation;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.Java2DFrameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static com.rtambun.minio.core.Constants.*;


/**
 * This class is meant to handle video file related operations
 * like getting thumbnail of video
 */
@Service
public class VideoService {

    private static final Logger LOGGER = LoggerFactory.getLogger(VideoService.class);
    private static final String ROTATE = "rotate";

    @Autowired
    private ApplicationProperties applicationProperties;
    /**
     * Get thumbnail from a video file's inputStream
     * @param inputStream - strem contains video data
     * @return stream contains thumbnail of video
     * @throws IOException thrown when
     */
    public InputStream getThumbnailForVideo(InputStream inputStream) throws IOException {
        LOGGER.info("Resize Image with BufferedImage");

        ByteArrayOutputStream baos1 = new ByteArrayOutputStream();
        BufferedImageWrapper bufferedImageWrapper = getBufferedImageFromVideo(inputStream);
        BufferedImage bufferedImage= bufferedImageWrapper.getBufferedImage();

        if (bufferedImage != null) {
            String rotationMetaData= bufferedImageWrapper.getRotation();
            if(rotationMetaData!=null && (!rotationMetaData.isEmpty())) {
                try {
                    int iRotationMetaData = Integer.parseInt(rotationMetaData);
                    bufferedImage=Rotation.newRotator(iRotationMetaData).apply(bufferedImage);
                }catch (NumberFormatException exception){
                    LOGGER.warn("rotation not a number, so no rotation done");
                }
            }
            Thumbnails.of(bufferedImage)
                    .size(Integer.parseInt(applicationProperties.getConfigValue(DEFAULT_THUMBNAIL_WIDTH_KEY)),
                            Integer.parseInt(applicationProperties.getConfigValue(DEFAULT_THUMBNAIL_HEIGHT_KEY)))
                    .outputFormat(JPG)
                    .toOutputStream(baos1);
        }
        else {
            LOGGER.info("Buffered Thumbnail is empty");
        }

        return new ByteArrayInputStream(baos1.toByteArray());
    }

    /**
     * Grab an image from Frame grab of an video file's inputStream
     * @param inputStream - stream contain video data
     * @return buffered image
     * @throws IOException thrown when creating buffered image
     */
    private BufferedImageWrapper getBufferedImageFromVideo(InputStream inputStream) throws IOException {
        LOGGER.info("GenerateVideoThumbnail");
        FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(inputStream);
        grabber.setFormat(MP4);
        grabber.start();
        String rotate = grabber.getVideoMetadata(ROTATE);

        if(rotate!=null){
            LOGGER.info("rotate is "+rotate);
        }else {
            LOGGER.info("rotate is null");
        }

        /*
         * Get a frame in the middle of the video
         */
        int startFrame = grabber.getLengthInVideoFrames() / 2;
        grabber.setVideoFrameNumber(startFrame);
        BufferedImage bi = null;
        try {
            for (int i = startFrame; i < grabber.getLengthInFrames(); i++) {
                try {
                    Frame frame = grabber.grab();
                    if (frame == null) {
                        LOGGER.info("Frame is empty.");
                        continue;
                    }
                    bi = Java2DFrameUtils.toBufferedImage(frame);
                    break;
                } catch (FrameGrabber.Exception t) {
                    LOGGER.error("Exception thrown."+t.getMessage());
                }
            }
        } finally {
            grabber.stop();
            grabber.close();
        }

        return new BufferedImageWrapper(bi,rotate);
    }

}
