package com.rtambun.minio.core;

import java.awt.image.BufferedImage;

public class BufferedImageWrapper {
    private BufferedImage bufferedImage;
    private String rotation;

    public BufferedImageWrapper(BufferedImage bufferedImage, String rotation) {
        this.bufferedImage = bufferedImage;
        this.rotation = rotation;
    }

    public BufferedImage getBufferedImage() {
        return bufferedImage;
    }

    public String getRotation() {
        return rotation;
    }
}
