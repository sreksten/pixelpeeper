package com.threeamigos.imageviewer.implementations.preferences;

import java.util.EnumMap;
import java.util.Map;

import com.threeamigos.imageviewer.data.ExifTag;
import com.threeamigos.imageviewer.data.ExifTagVisibility;
import com.threeamigos.imageviewer.implementations.ui.AbstractPreferencesImpl;
import com.threeamigos.imageviewer.interfaces.persister.Persister;
import com.threeamigos.imageviewer.interfaces.preferences.ExifTagPreferences;

public class ExifTagPreferencesImpl extends AbstractPreferencesImpl<ExifTagPreferences> implements ExifTagPreferences {

	private boolean tagsVisible = true;

	private Map<ExifTag, ExifTagVisibility> tagsMap;

	@Override
	protected String getEntityDescription() {
		return "tag";
	}

	public ExifTagPreferencesImpl(Persister<ExifTagPreferences> persister) {
		super(persister);

		tagsMap = new EnumMap<>(ExifTag.class);

		loadPostConstruct();
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
}
