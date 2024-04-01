package com.threeamigos.pixelpeeper.interfaces.ui;

import com.threeamigos.pixelpeeper.data.ExifTag;

import java.awt.*;
import java.io.File;
import java.util.Collection;

public interface ExifTagsFilter {

    int MAX_SELECTABLE_FILES_PER_GROUP = 9;

    Collection<File> filterByTags(Component component, Collection<File> files);

    ExifTag getTagToGroupBy();

    ExifTag getTagToOrderBy();

    int getTolerance();

}
