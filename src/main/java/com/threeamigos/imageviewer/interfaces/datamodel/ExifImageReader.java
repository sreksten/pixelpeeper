package com.threeamigos.imageviewer.interfaces.datamodel;

import java.io.File;

import com.threeamigos.imageviewer.data.ExifMap;
import com.threeamigos.imageviewer.data.PictureData;

public interface ExifImageReader {

	public ExifMap readMetadata(File file);

	public PictureData readImage(File file);

}
