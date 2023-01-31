package com.threeamigos.imageviewer.implementations.datamodel;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.threeamigos.imageviewer.data.ExifMap;
import com.threeamigos.imageviewer.data.ExifTag;
import com.threeamigos.imageviewer.data.ExifValue;
import com.threeamigos.imageviewer.interfaces.datamodel.ExifCache;

class GroupedFilesByExifTag {

	private final ExifCache exifCache;
	private List<ExifValueToFilesHolder> groupedFiles;
	private int groupedFilesIndex;

	GroupedFilesByExifTag(ExifCache exifCache) {
		this.exifCache = exifCache;
	}

	public void set(Collection<File> files, ExifTag tagToGroupBy, int preferredGroupIndex) {
		prepareCollection();
		if (tagToGroupBy == null) {
			groupedFiles.add(new ExifValueToFilesHolder(null, files));
		} else {
			Map<ExifValue, Collection<File>> groupedMatchingFiles = new HashMap<>();
			for (File file : files) {
				Optional<ExifMap> tags = exifCache.getExifMap(file);
				if (tags.isPresent()) {
					ExifValue value = tags.get().getExifValue(tagToGroupBy);
					groupedMatchingFiles.computeIfAbsent(value, k -> new ArrayList<>()).add(file);
				}
			}
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

	private class ExifValueToFilesHolder {
		ExifValue exifValue;
		Collection<File> files;

		ExifValueToFilesHolder(ExifValue exifValue, Collection<File> files) {
			this.exifValue = exifValue;
			this.files = files;
		}
	}

	private void sort(List<ExifValueToFilesHolder> list) {
		final Comparator<ExifValue> exifValueComparator = ExifValue.getComparator();
		final Comparator<ExifValueToFilesHolder> comparator = (v1, v2) -> {
			return exifValueComparator.compare(v1.exifValue, v2.exifValue);
		};
		Collections.sort(list, comparator);
	}
}
