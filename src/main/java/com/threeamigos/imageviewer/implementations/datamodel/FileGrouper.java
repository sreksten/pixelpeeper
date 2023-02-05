package com.threeamigos.imageviewer.implementations.datamodel;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.threeamigos.imageviewer.data.ExifMap;
import com.threeamigos.imageviewer.data.ExifTag;
import com.threeamigos.imageviewer.data.ExifValue;

public class FileGrouper {

	public static final Map<ExifValue, Collection<File>> groupFiles(Map<File, ExifMap> filesToTagsMap,
			ExifTag tagToGroupBy, int tolerance) {
		Map<ExifValue, Collection<File>> groupedFiles = new HashMap<>();
		if (tagToGroupBy != null) {
			for (File file : filesToTagsMap.keySet()) {
				ExifMap tags = filesToTagsMap.get(file);
				ExifValue value = tags.getExifValue(tagToGroupBy);
				if (tagToGroupBy == ExifTag.FOCAL_LENGTH || tagToGroupBy == ExifTag.FOCAL_LENGTH_35MM_EQUIVALENT) {
					ExifValue nearestValue = getNearestValue(value, groupedFiles.keySet(), tolerance);
					if (nearestValue == null) {
						nearestValue = value;
					}
					groupedFiles.computeIfAbsent(nearestValue, k -> new ArrayList<>()).add(file);
				} else {
					groupedFiles.computeIfAbsent(value, k -> new ArrayList<>()).add(file);
				}
			}
		} else {
			groupedFiles.put(null, filesToTagsMap.keySet());
		}
		return groupedFiles;
	}

	private static final ExifValue getNearestValue(ExifValue value, Collection<ExifValue> possibleValues,
			int tolerance) {
		ExifValue nearestValue = null;
		float minDistance = Float.MAX_VALUE;
		float valueAsFloat = value.asFloat();
		for (ExifValue possibleValue : possibleValues) {
			float possibleValueAsFloat = possibleValue.asFloat();
			float distance = Math.abs(possibleValueAsFloat - valueAsFloat);
			if (distance <= tolerance && distance < minDistance) {
				minDistance = distance;
				nearestValue = possibleValue;
			} else {
			}
		}
		return nearestValue;
	}

}
