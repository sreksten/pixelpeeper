package com.threeamigos.imageviewer.interfaces.ui;

import java.awt.Component;
import java.io.File;
import java.util.Collection;

public interface ExifTagsFilter {

	public Collection<File> filterByTags(Component component, Collection<File> files);

}
