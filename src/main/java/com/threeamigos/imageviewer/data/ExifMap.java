package com.threeamigos.imageviewer.data;

import java.util.EnumMap;
import java.util.Map;

public class ExifMap {

	private Map<ExifTag, String> tagToDescriptions = new EnumMap<>(ExifTag.class);
	private Map<ExifTag, Object> tagToObjects = new EnumMap<>(ExifTag.class);

	public void setIfAbsent(ExifTag exifTag, String value, Object object) {
		if (value == null || value.trim().isEmpty()) {
			return;
		}
		tagToDescriptions.putIfAbsent(exifTag, value);
		tagToObjects.putIfAbsent(exifTag, object);
	}

	public String getTagDescriptive(ExifTag exifTag) {
		return tagToDescriptions.computeIfAbsent(exifTag, t -> "N/A");
	}

	public Object getTagObject(ExifTag exifTag) {
		return tagToObjects.computeIfAbsent(exifTag, t -> "N/A");
	}

}
