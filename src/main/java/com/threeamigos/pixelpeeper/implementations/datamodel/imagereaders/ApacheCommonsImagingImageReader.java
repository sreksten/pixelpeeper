package com.threeamigos.pixelpeeper.implementations.datamodel.imagereaders;

import java.awt.image.BufferedImage;
import java.io.File;

import org.apache.commons.imaging.Imaging;

import com.threeamigos.pixelpeeper.interfaces.datamodel.ImageReader;

public class ApacheCommonsImagingImageReader implements ImageReader {

	@Override
	public BufferedImage readImage(File file) throws Exception {
		return Imaging.getBufferedImage(file);
	}

}
