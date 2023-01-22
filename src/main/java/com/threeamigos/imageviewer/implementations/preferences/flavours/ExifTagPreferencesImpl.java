package com.threeamigos.imageviewer.implementations.preferences.flavours;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

import com.threeamigos.imageviewer.data.ExifTag;
import com.threeamigos.imageviewer.data.ExifTagVisibility;
import com.threeamigos.imageviewer.interfaces.preferences.flavours.ExifTagPreferences;

public class ExifTagPreferencesImpl implements ExifTagPreferences {

	private boolean tagsVisible = TAGS_VISIBLE_DEFAULT;
	private boolean overridingTagsVisibility = OVERRIDING_TAGS_VISIBILITY_DEFAULT;
	private Map<ExifTag, ExifTagVisibility> tagsMap = new EnumMap<>(ExifTag.class);

	@Override
	public void setTagVisibility(ExifTag tag, ExifTagVisibility tagVisibility) {
		tagsMap.put(tag, tagVisibility);
	}

	@Override
	public ExifTagVisibility getTagVisibility(ExifTag tag) {
		ExifTagVisibility value = tagsMap.get(tag);
		if (value == null) {
			return ExifTagVisibility.NO;
		} else {
			return value;
		}
	}

	@Override
	public boolean isTagsVisible() {
		return tagsVisible;
	}

	@Override
	public void setTagsVisible(boolean tagsVisible) {
		this.tagsVisible = tagsVisible;
	}

	@Override
	public boolean isOverridingTagsVisibility() {
		return overridingTagsVisibility;
	}

	@Override
	public void setOverridingTagsVisibility(boolean overridingTagsVisibility) {
		this.overridingTagsVisibility = overridingTagsVisibility;
	}

	@Override
	public Map<ExifTag, ExifTagVisibility> getVisibilityMap() {
		return Collections.unmodifiableMap(tagsMap);
	}

	@Override
	public void loadDefaultValues() {
		tagsVisible = TAGS_VISIBLE_DEFAULT;
		overridingTagsVisibility = OVERRIDING_TAGS_VISIBILITY_DEFAULT;
		tagsMap.clear();
		for (ExifTag tag : ExifTag.values()) {
			setTagVisibility(tag, ExifTagVisibility.YES);
		}
	}

	@Override
	public void validate() {
		// TODO Auto-generated method stub

	}

}
