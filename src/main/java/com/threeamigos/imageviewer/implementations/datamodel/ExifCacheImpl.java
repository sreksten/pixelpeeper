package com.threeamigos.imageviewer.implementations.datamodel;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.threeamigos.imageviewer.data.ExifMap;
import com.threeamigos.imageviewer.interfaces.datamodel.ExifCache;
import com.threeamigos.imageviewer.interfaces.datamodel.ExifReaderFactory;

public class ExifCacheImpl implements ExifCache {

	private final ExifReaderFactory exifReaderFactory;
	private final Map<File, ExifMap> map;

	public ExifCacheImpl(ExifReaderFactory exifReaderFactory) {
		this.exifReaderFactory = exifReaderFactory;
		map = new HashMap<>();
	}

	@Override
	public void clear() {
		map.clear();
	}

	@Override
	public Optional<ExifMap> getExifMap(File file) {
		ExifMap exifMap = map.get(file);
		if (exifMap == null) {
			exifMap = exifReaderFactory.getExifReader().readMetadata(file).orElse(null);
			map.put(file, exifMap);
		}
		return Optional.ofNullable(exifMap);
	}

}
