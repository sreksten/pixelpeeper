package com.threeamigos.pixelpeeper.implementations.datamodel;

import com.threeamigos.pixelpeeper.data.ExifMap;
import com.threeamigos.pixelpeeper.data.ExifTag;
import com.threeamigos.pixelpeeper.data.ExifValue;

import java.io.File;
import java.util.*;

public class FileGrouper {

    private FileGrouper() {
    }

    public static Map<ExifValue, Collection<File>> groupFiles(Map<File, ExifMap> filesToTagsMap,
                                                              ExifTag tagToGroupBy, int tolerance, ExifTag tagToOrderBy) {
        Map<ExifValue, Collection<File>> groupedFiles = groupFilesImpl(filesToTagsMap, tagToGroupBy, tolerance);
        if (tagToOrderBy != null) {
            orderGroupedFilesImpl(filesToTagsMap, tagToOrderBy, groupedFiles);
        }
        return groupedFiles;
    }

    private static Map<ExifValue, Collection<File>> groupFilesImpl(Map<File, ExifMap> filesToTagsMap, ExifTag tagToGroupBy, int tolerance) {
        Map<ExifValue, Collection<File>> groupedFiles = new HashMap<>();
        if (tagToGroupBy != null) {
            for (Map.Entry<File, ExifMap> entry : filesToTagsMap.entrySet()) {
                File file = entry.getKey();
                ExifMap tags = entry.getValue();
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
            List<File> files = new ArrayList<>(filesToTagsMap.keySet());
            groupedFiles.put(null, files);
        }
        return groupedFiles;
    }

    private static ExifValue getNearestValue(ExifValue value, Collection<ExifValue> possibleValues,
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
            }
        }
        return nearestValue;
    }

    private static void orderGroupedFilesImpl(Map<File, ExifMap> filesToTagsMap, ExifTag tagToOrderBy, Map<ExifValue, Collection<File>> groupedFiles) {
        for (Collection<File> collection : groupedFiles.values()) {
            List<File> list = (List<File>) collection;
            list.sort((file1, file2) -> {
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

}
