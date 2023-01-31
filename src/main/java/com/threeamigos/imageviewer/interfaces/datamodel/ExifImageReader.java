package com.threeamigos.imageviewer.interfaces.datamodel;

import java.io.File;

import com.threeamigos.imageviewer.data.PictureData;

public interface ExifImageReader {

	public PictureData readImage(File file);

}
