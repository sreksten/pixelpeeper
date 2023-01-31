package com.threeamigos.imageviewer.interfaces.datamodel;

import java.io.File;
import java.util.Optional;

import com.threeamigos.imageviewer.data.ExifMap;

public interface ExifCache {

	public void clear();

	public Optional<ExifMap> getExifMap(File file);

}
