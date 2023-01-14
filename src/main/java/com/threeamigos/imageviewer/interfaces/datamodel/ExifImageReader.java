package com.threeamigos.imageviewer.interfaces.datamodel;

import java.io.File;

import com.threeamigos.imageviewer.data.ExifMap;

public interface ExifImageReader {

	public ExifMap readMetadata(File file);

	public boolean readImage(File file);

}
