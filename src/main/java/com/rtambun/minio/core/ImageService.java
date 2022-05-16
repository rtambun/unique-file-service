package com.rtambun.minio.core;

import com.rtambun.minio.config.ApplicationProperties;
import net.coobird.thumbnailator.Thumbnails;
//import org.bytedeco.opencv.opencv_core.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static com.rtambun.minio.core.Constants.DEFAULT_THUMBNAIL_HEIGHT_KEY;
import static com.rtambun.minio.core.Constants.DEFAULT_THUMBNAIL_WIDTH_KEY;


/**
 * This class is meant to handle image file related operations
 * like getting thumbnail, rotating an image and so on
 */
@Service
public class ImageService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImageService.class);

    @Autowired
    private ApplicationProperties applicationProperties;

    /**
     * Get thumbnail for an image
     * @param inputStream - stream contain image data where to retrieve thumbnail
     * @return stream contain thumbnail
     * @throws IOException thrown when creating the stream contain thumbnail
     */
    public InputStream getThumbnailForImage(InputStream inputStream) throws IOException {
        LOGGER.info("Resize Image with Input Stream");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Thumbnails.of(inputStream)
                .size(Integer.parseInt(applicationProperties.getConfigValue(DEFAULT_THUMBNAIL_WIDTH_KEY)),
                Integer.parseInt(applicationProperties.getConfigValue(DEFAULT_THUMBNAIL_HEIGHT_KEY)))
                .toOutputStream(baos);
        return new ByteArrayInputStream(baos.toByteArray());
    }
}
