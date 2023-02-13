package com.threeamigos.pixelpeeper.interfaces.edgedetect;

import java.awt.image.BufferedImage;

public interface EdgesDetector {

	public void setSourceImage(BufferedImage sourceImage);

	public void process();

	public void abort();

	public BufferedImage getEdgesImage();

}
