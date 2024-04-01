package com.threeamigos.pixelpeeper.interfaces.edgedetect;

import java.awt.image.BufferedImage;

public interface EdgesDetector {

    void setSourceImage(BufferedImage sourceImage);

    void process();

    void abort();

    BufferedImage getEdgesImage();

}
