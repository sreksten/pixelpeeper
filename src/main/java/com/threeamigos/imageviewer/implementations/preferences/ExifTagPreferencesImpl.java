package com.threeamigos.imageviewer.implementations.preferences;

import java.util.EnumMap;
import java.util.Map;

import com.threeamigos.common.util.interfaces.ErrorMessageHandler;
import com.threeamigos.imageviewer.data.ExifTag;
import com.threeamigos.imageviewer.data.ExifTagVisibility;
import com.threeamigos.imageviewer.implementations.ui.AbstractPreferencesImpl;
import com.threeamigos.imageviewer.interfaces.persister.Persister;
import com.threeamigos.imageviewer.interfaces.preferences.ExifTagPreferences;

public class ExifTagPreferencesImpl extends AbstractPreferencesImpl<ExifTagPreferences> implements ExifTagPreferences {

	private boolean tagsVisibleAtStart;
	private boolean overridingTagsVisibilityAtStart;
	private Map<ExifTag, ExifTagVisibility> tagsMapAtStart;

	private boolean tagsVisible = TAGS_VISIBLE_DEFAULT;
	private boolean overridingTagsVisibility = OVERRIDING_TAGS_VISIBILITY_DEFAULT;
	private Map<ExifTag, ExifTagVisibility> tagsMap;

	@Override
	protected String getEntityDescription() {
		return "tag";
	}

	public ExifTagPreferencesImpl(Persister<ExifTagPreferences> persister, ErrorMessageHandler errorMessageHandler) {
		super(persister, errorMessageHandler);

		tagsMap = new EnumMap<>(ExifTag.class);

		loadPostConstruct();
		copyPreferencesAtStart();
	}

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
	protected void loadDefaultValues() {
		for (ExifTag tag : ExifTag.values()) {
			setTagVisibility(tag, ExifTagVisibility.YES);
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

	private void copyPreferencesAtStart() {
		tagsVisibleAtStart = tagsVisible;
		overridingTagsVisibilityAtStart = overridingTagsVisibility;
		tagsMapAtStart = new EnumMap<>(ExifTag.class);
		tagsMap.forEach(tagsMapAtStart::put);
	}

	@Override
	public boolean hasChanged() {
		return tagsVisible != tagsVisibleAtStart || overridingTagsVisibility != overridingTagsVisibilityAtStart
				|| !tagsMap.equals(tagsMapAtStart);
	}
}
