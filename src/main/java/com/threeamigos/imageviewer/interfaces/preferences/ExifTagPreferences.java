package com.threeamigos.imageviewer.interfaces.preferences;

import com.threeamigos.imageviewer.data.ExifTag;
import com.threeamigos.imageviewer.data.ExifTagVisibility;
import com.threeamigos.imageviewer.interfaces.persister.Persistable;

/**
 * Keeps track of the Exif tags we are interested in
 *
 * @author Stefano Reksten
 *
 */
public interface ExifTagPreferences extends Persistable {

	public static final boolean TAGS_VISIBLE_DEFAULT = true;
	public static final boolean OVERRIDING_TAGS_VISIBILITY_DEFAULT = false;

	public boolean isTagsVisible();

	public void setTagsVisible(boolean tagsVisible);

	public boolean isOverridingTagsVisibility();

	public void setOverridingTagsVisibility(boolean overridingTagsVisibility);

	public void setTagVisibility(ExifTag tag, ExifTagVisibility tagVisibility);

	public ExifTagVisibility getTagVisibility(ExifTag tag);

}
