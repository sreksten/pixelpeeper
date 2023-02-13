package com.threeamigos.pixelpeeper.interfaces.datamodel;

import java.io.File;

import com.threeamigos.pixelpeeper.data.PictureData;

public interface ExifImageReader {

	public PictureData readImage(File file);

}
