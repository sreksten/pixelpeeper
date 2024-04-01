package com.threeamigos.pixelpeeper.interfaces.preferences.flavours;

import com.threeamigos.common.util.interfaces.preferences.Preferences;
import com.threeamigos.pixelpeeper.data.ExifTag;
import com.threeamigos.pixelpeeper.data.ExifTagVisibility;

/**
 * Keeps track of the Exif tags we are interested in
 *
 * @author Stefano Reksten
 */
public interface ExifTagsPreferences extends Preferences {

    boolean TAGS_VISIBLE_DEFAULT = true;
    boolean OVERRIDING_TAGS_VISIBILITY_DEFAULT = false;
    int BORDER_THICKNESS_LINE = 1;
    int BORDER_THICKNESS_THIN = 11;
    int BORDER_THICKNESS_MEDIUM = 21;
    int BORDER_THICKNESS_LARGE = 31;
    int BORDER_THICKNESS_DEFAULT = BORDER_THICKNESS_LINE;
    int BORDER_THICKNESS_MAX = BORDER_THICKNESS_LARGE;

    default String getDescription() {
        return "Exif tags preferences";
    }

    boolean isTagsVisible();

    void setTagsVisible(boolean tagsVisible);

    boolean isOverridingTagsVisibility();

    void setOverridingTagsVisibility(boolean overridingTagsVisibility);

    void setTagVisibility(ExifTag tag, ExifTagVisibility tagVisibility);

    ExifTagVisibility getTagVisibility(ExifTag tag);

    int getBorderThickness();

    void setBorderThickness(int borderThickness);
}
