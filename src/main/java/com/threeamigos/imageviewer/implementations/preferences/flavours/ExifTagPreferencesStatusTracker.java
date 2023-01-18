package com.threeamigos.imageviewer.implementations.preferences.flavours;

import java.util.EnumMap;
import java.util.Map;

import com.threeamigos.imageviewer.data.ExifTag;
import com.threeamigos.imageviewer.data.ExifTagVisibility;
import com.threeamigos.imageviewer.interfaces.persister.StatusTracker;
import com.threeamigos.imageviewer.interfaces.preferences.flavours.ExifTagPreferences;

public class ExifTagPreferencesStatusTracker implements StatusTracker<ExifTagPreferences> {

	private boolean tagsVisibleAtStart;
	private boolean overridingTagsVisibilityAtStart;
	private Map<ExifTag, ExifTagVisibility> tagsMapAtStart;

	private final ExifTagPreferences exifTagPreferences;

	public ExifTagPreferencesStatusTracker(ExifTagPreferences exifTagPreferences) {
		this.exifTagPreferences = exifTagPreferences;
	}

	@Override
	public void loadInitialValues() {
		tagsVisibleAtStart = exifTagPreferences.isTagsVisible();
		overridingTagsVisibilityAtStart = exifTagPreferences.isOverridingTagsVisibility();
		tagsMapAtStart = new EnumMap<>(ExifTag.class);
		exifTagPreferences.getVisibilityMap().forEach(tagsMapAtStart::put);
	}

	@Override
	public boolean hasChanged() {
		return exifTagPreferences.isTagsVisible() != tagsVisibleAtStart
				|| exifTagPreferences.isOverridingTagsVisibility() != overridingTagsVisibilityAtStart
				|| !exifTagPreferences.getVisibilityMap().equals(tagsMapAtStart);
	}

}
