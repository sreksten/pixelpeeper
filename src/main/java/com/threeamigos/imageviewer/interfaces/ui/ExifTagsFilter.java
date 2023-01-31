package com.threeamigos.imageviewer.interfaces.ui;

import java.awt.Component;
import java.io.File;
import java.util.Collection;

import com.threeamigos.imageviewer.data.ExifTag;

public interface ExifTagsFilter {

	public static final int MAX_SELECTABLE_FILES_PER_GROUP = 9;

	public Collection<File> filterByTags(Component component, Collection<File> files);

	public ExifTag getTagToGroupBy();

}
