package com.threeamigos.imageviewer.interfaces.edgedetect;

import java.awt.image.BufferedImage;

public interface EdgesDetector {

	public void setSourceImage(BufferedImage sourceImage);

	public void process();

	public void abort();

	public BufferedImage getEdgesImage();

}
