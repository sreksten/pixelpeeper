package com.threeamigos.pixelpeeper.interfaces.datamodel;

import java.awt.image.BufferedImage;
import java.io.File;

public interface ImageReader {

	public BufferedImage readImage(File file) throws Exception;

}