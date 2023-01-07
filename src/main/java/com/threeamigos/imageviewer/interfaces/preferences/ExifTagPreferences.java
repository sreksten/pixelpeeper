package com.threeamigos.imageviewer.interfaces.preferences;

import com.threeamigos.imageviewer.data.ExifTag;
import com.threeamigos.imageviewer.interfaces.persister.Persistable;

/**
 * Keeps track of the Exif tags we are interested in
 *
 * @author Stefano Reksten
 *
 */
public interface ExifTagPreferences extends Persistable {

	public static final boolean TAGS_VISIBLE_DEFAULT = true;
	public static final boolean TAGS_VISIBLE_ONLY_IF_DIFFERENT_DEFAULT = false;

	public boolean isTagsVisible();

	public void setTagsVisible(boolean tagsVisible);

	public boolean isTagsVisibleOnlyIfDifferent();

	public void setTagsVisibleOnlyIfDifferent(boolean tagsVisibleOnlyIfDifferent);

	public void setTagVisible(ExifTag tag, boolean visible);

	public boolean isTagVisible(ExifTag tag);

	public void toggle(ExifTag tag);

}
