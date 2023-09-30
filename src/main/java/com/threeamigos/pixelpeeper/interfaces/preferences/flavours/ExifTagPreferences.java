package com.threeamigos.pixelpeeper.interfaces.preferences.flavours;

import java.util.Map;

import com.threeamigos.common.util.interfaces.preferences.Preferences;
import com.threeamigos.pixelpeeper.data.ExifTag;
import com.threeamigos.pixelpeeper.data.ExifTagVisibility;

/**
 * Keeps track of the Exif tags we are interested in
 *
 * @author Stefano Reksten
 *
 */
public interface ExifTagPreferences extends Preferences {

	public static final boolean TAGS_VISIBLE_DEFAULT = true;
	public static final boolean OVERRIDING_TAGS_VISIBILITY_DEFAULT = false;

	default String getDescription() {
		return "Exif tags preferences";
	}

	public boolean isTagsVisible();

	public void setTagsVisible(boolean tagsVisible);

	public boolean isOverridingTagsVisibility();

	public void setOverridingTagsVisibility(boolean overridingTagsVisibility);

	public void setTagVisibility(ExifTag tag, ExifTagVisibility tagVisibility);

	public ExifTagVisibility getTagVisibility(ExifTag tag);

	public Map<ExifTag, ExifTagVisibility> getVisibilityMap();

}
