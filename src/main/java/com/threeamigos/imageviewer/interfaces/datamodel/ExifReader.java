package com.threeamigos.imageviewer.interfaces.datamodel;

import java.io.File;

import com.threeamigos.imageviewer.data.ExifMap;

public interface ExifReader {

	public ExifMap readMetadata(File file);

}
