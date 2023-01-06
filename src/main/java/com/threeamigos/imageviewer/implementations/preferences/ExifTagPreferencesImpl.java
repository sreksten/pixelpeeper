package com.threeamigos.imageviewer.implementations.preferences;

import java.util.EnumMap;
import java.util.Map;

import com.threeamigos.imageviewer.data.ExifTag;
import com.threeamigos.imageviewer.implementations.ui.AbstractPreferencesImpl;
import com.threeamigos.imageviewer.interfaces.persister.Persister;
import com.threeamigos.imageviewer.interfaces.preferences.ExifTagPreferences;

public class ExifTagPreferencesImpl extends AbstractPreferencesImpl<ExifTagPreferences> implements ExifTagPreferences {

	private boolean tagsVisible = true;
	private Map<ExifTag, Boolean> tagsMap;

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
	public void setTagVisible(ExifTag tag, boolean visible) {
		tagsMap.put(tag, visible);
	}

	@Override
	public boolean isTagVisible(ExifTag tag) {
		Boolean value = tagsMap.get(tag);
		if (value == null) {
			return false;
		} else {
			return value;
		}
	}

	@Override
	public void toggle(ExifTag tag) {
		setTagVisible(tag, !isTagVisible(tag));
	}

	@Override
	protected void loadDefaultValues() {
		for (ExifTag tag : ExifTag.values()) {
			setTagVisible(tag, true);
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
