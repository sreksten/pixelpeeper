package com.threeamigos.imageviewer.implementations.datamodel;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.threeamigos.imageviewer.data.ExifMap;
import com.threeamigos.imageviewer.data.ExifTag;
import com.threeamigos.imageviewer.interfaces.datamodel.ExifCache;
import com.threeamigos.imageviewer.interfaces.datamodel.ExifReaderFactory;
import com.threeamigos.imageviewer.interfaces.ui.CropFactorProvider;

public class ExifCacheImpl implements ExifCache {

	private final ExifReaderFactory exifReaderFactory;
	private final CropFactorProvider cropFactorProvider;
	private final Map<File, ExifMap> map;

	public ExifCacheImpl(ExifReaderFactory exifReaderFactory, CropFactorProvider cropFactorProvider) {
		this.exifReaderFactory = exifReaderFactory;
		this.cropFactorProvider = cropFactorProvider;
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
			if (exifMap != null) {
				check35mmEquivalencePresence(exifMap);
			}
			map.put(file, exifMap);
		}
		return Optional.ofNullable(exifMap);
	}

	private void check35mmEquivalencePresence(ExifMap exifMap) {
		if (exifMap.getExifValue(ExifTag.FOCAL_LENGTH_35MM_EQUIVALENT) == null) {
			float cropFactor = cropFactorProvider.getCropFactor(exifMap.getTagDescriptive(ExifTag.CAMERA_MANUFACTURER),
					exifMap.getTagDescriptive(ExifTag.CAMERA_MODEL), null);
			if (exifMap.getExifValue(ExifTag.FOCAL_LENGTH) != null) {
				float focalLength = exifMap.getAsFloat(ExifTag.FOCAL_LENGTH);
				int focalLength35mmEquivalent = (int) (focalLength * cropFactor);
				exifMap.setIfAbsent(ExifTag.FOCAL_LENGTH_35MM_EQUIVALENT, focalLength35mmEquivalent + " mm",
						focalLength35mmEquivalent);
			}
		}
	}

}
