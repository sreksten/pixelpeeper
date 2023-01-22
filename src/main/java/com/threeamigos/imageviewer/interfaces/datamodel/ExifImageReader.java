package com.threeamigos.imageviewer.interfaces.datamodel;

import java.io.File;
import java.util.Optional;

import com.threeamigos.imageviewer.data.ExifMap;
import com.threeamigos.imageviewer.data.PictureData;

public interface ExifImageReader {

	public Optional<ExifMap> readExifMap(File file);

	public PictureData readImage(File file);

}
