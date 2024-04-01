package com.threeamigos.pixelpeeper.implementations.datamodel;

import com.threeamigos.pixelpeeper.data.ExifMap;
import com.threeamigos.pixelpeeper.data.ExifTag;
import com.threeamigos.pixelpeeper.data.ExifValue;
import com.threeamigos.pixelpeeper.interfaces.datamodel.ExifCache;

import java.io.File;
import java.util.*;

class GroupedFilesByExifTag {

    private final ExifCache exifCache;
    private List<ExifValueToFilesHolder> groupedFiles;
    private int groupedFilesIndex;

    GroupedFilesByExifTag(ExifCache exifCache) {
        this.exifCache = exifCache;
    }

    public void set(Collection<File> files, ExifTag tagToGroupBy, int tolerance, ExifTag tagToOrderBy, int preferredGroupIndex) {
        prepareCollection();
        if (tagToGroupBy == null) {
            groupedFiles.add(new ExifValueToFilesHolder(null, files));
        } else {
            Map<File, ExifMap> map = new HashMap<>();
            for (File file : files) {
                exifCache.getExifMap(file).ifPresent(exifMap -> map.put(file, exifMap));
            }
            Map<ExifValue, Collection<File>> groupedMatchingFiles = FileGrouper.groupFiles(map, tagToGroupBy,
                    tolerance, tagToOrderBy);

            for (Map.Entry<ExifValue, Collection<File>> entry : groupedMatchingFiles.entrySet()) {
                groupedFiles.add(new ExifValueToFilesHolder(entry.getKey(), entry.getValue()));
            }
            sort(groupedFiles);
        }
        if (preferredGroupIndex < groupedFiles.size()) {
            groupedFilesIndex = preferredGroupIndex;
        }
    }

    private void prepareCollection() {
        groupedFiles = new ArrayList<>();
        groupedFilesIndex = 0;
    }

    public int getGroupsCount() {
        return groupedFiles.size();
    }

    public int getCurrentGroup() {
        return groupedFilesIndex;
    }

    public void next() {
        groupedFilesIndex = (groupedFilesIndex + 1) % groupedFiles.size();
    }

    public void previous() {
        groupedFilesIndex--;
        if (groupedFilesIndex < 0) {
            groupedFilesIndex = groupedFiles.size() - 1;
        }
    }

    public Optional<ExifValue> getCurrentExifValue() {
        return Optional.ofNullable(groupedFiles.get(groupedFilesIndex).exifValue);
    }

    public Collection<File> getCurrentFiles() {
        return groupedFiles.get(groupedFilesIndex).files;
    }

    private static class ExifValueToFilesHolder {
        ExifValue exifValue;
        Collection<File> files;

        ExifValueToFilesHolder(ExifValue exifValue, Collection<File> files) {
            this.exifValue = exifValue;
            this.files = new ArrayList<>();
            this.files.addAll(files);
        }
    }

    private void sort(List<ExifValueToFilesHolder> list) {
        final Comparator<ExifValue> exifValueComparator = ExifValue.getComparator();
        final Comparator<ExifValueToFilesHolder> comparator = (v1, v2) -> exifValueComparator.compare(v1.exifValue, v2.exifValue);
        list.sort(comparator);
    }
}
