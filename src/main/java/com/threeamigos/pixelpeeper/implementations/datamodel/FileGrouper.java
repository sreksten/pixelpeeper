package com.threeamigos.pixelpeeper.implementations.datamodel;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.threeamigos.pixelpeeper.data.ExifMap;
import com.threeamigos.pixelpeeper.data.ExifTag;
import com.threeamigos.pixelpeeper.data.ExifValue;

public class FileGrouper {

	public static final Map<ExifValue, Collection<File>> groupFiles(Map<File, ExifMap> filesToTagsMap,
			ExifTag tagToGroupBy, int tolerance, ExifTag tagToOrderBy) {
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
			List<File> files = new ArrayList<>();
			files.addAll(filesToTagsMap.keySet());
			groupedFiles.put(null, files);
		}
		if (tagToOrderBy != null) {
			for (Collection<File> collection : groupedFiles.values()) {
				List<File> list = (List<File>) collection;
				Collections.sort(list, (file1, file2) -> {
					ExifMap map1 = filesToTagsMap.get(file1);
					ExifMap map2 = filesToTagsMap.get(file2);
					if (map1 == null) {
						return 1;
					} else if (map2 == null) {
						return -1;
					}
					String value1 = map1.getTagDescriptive(tagToOrderBy);
					String value2 = map2.getTagDescriptive(tagToOrderBy);
					if (value1 == null) {
						return 1;
					} else if (value2 == null) {
						return -1;
					}
					return value1.compareTo(value2);
				});
			}
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
