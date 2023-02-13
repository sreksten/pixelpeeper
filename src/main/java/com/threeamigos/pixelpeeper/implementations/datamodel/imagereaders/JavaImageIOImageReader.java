package com.threeamigos.pixelpeeper.implementations.datamodel.imagereaders;

import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import com.threeamigos.pixelpeeper.interfaces.datamodel.ImageReader;

public class JavaImageIOImageReader implements ImageReader {

	@Override
	public BufferedImage readImage(File file) throws Exception {
		return ImageIO.read(file);
	}

}
