package com.threeamigos.imageviewer.interfaces.datamodel;

import java.awt.image.BufferedImage;

public interface EdgesDetector {

	public void setSourceImage(BufferedImage sourceImage);

	public void process();

	public BufferedImage getEdgesImage();

}
