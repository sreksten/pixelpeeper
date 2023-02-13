package com.threeamigos.pixelpeeper.implementations.preferences.flavours;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

import com.threeamigos.pixelpeeper.data.ExifTag;
import com.threeamigos.pixelpeeper.data.ExifTagVisibility;
import com.threeamigos.pixelpeeper.interfaces.datamodel.CommunicationMessages;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavours.ExifTagPreferences;

public class ExifTagPreferencesImpl extends PropertyChangeAwareImpl implements ExifTagPreferences {

	private boolean tagsVisible = TAGS_VISIBLE_DEFAULT;
	private boolean overridingTagsVisibility = OVERRIDING_TAGS_VISIBILITY_DEFAULT;
	private Map<ExifTag, ExifTagVisibility> tagsMap = new EnumMap<>(ExifTag.class);

	@Override
	public void setTagVisibility(ExifTag tag, ExifTagVisibility tagVisibility) {
		tagsMap.put(tag, tagVisibility);
		firePropertyChange(CommunicationMessages.TAG_VISIBILITY_CHANGED, tag, tagVisibility);
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
		boolean oldTagsVisible = this.tagsVisible;
		this.tagsVisible = tagsVisible;
		firePropertyChange(CommunicationMessages.TAGS_VISIBILITY_CHANGED, oldTagsVisible, tagsVisible);
	}

	@Override
	public boolean isOverridingTagsVisibility() {
		return overridingTagsVisibility;
	}

	@Override
	public void setOverridingTagsVisibility(boolean overridingTagsVisibility) {
		boolean oldOverridingTagsVisibility = this.overridingTagsVisibility;
		this.overridingTagsVisibility = overridingTagsVisibility;
		firePropertyChange(CommunicationMessages.TAGS_VISIBILITY_OVERRIDE_CHANGED, oldOverridingTagsVisibility,
				overridingTagsVisibility);
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
	}

}