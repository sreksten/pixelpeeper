package com.threeamigos.pixelpeeper.interfaces.datamodel;

import java.io.File;
import java.util.Optional;

import com.threeamigos.pixelpeeper.data.ExifMap;

public interface ExifCache {

	public void clear();

	public Optional<ExifMap> getExifMap(File file);

}
