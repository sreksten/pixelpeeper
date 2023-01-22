package com.threeamigos.imageviewer.interfaces.datamodel;

import java.io.File;
import java.util.Optional;

import com.threeamigos.imageviewer.data.ExifMap;

public interface ExifReader {

	public Optional<ExifMap> readMetadata(File file);

}
