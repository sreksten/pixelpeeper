package com.threeamigos.imageviewer.implementations.ui.plugins;

import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.util.EnumMap;
import java.util.Map;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;

import com.threeamigos.imageviewer.data.ExifTag;
import com.threeamigos.imageviewer.data.ExifTagVisibility;
import com.threeamigos.imageviewer.interfaces.preferences.flavours.ExifTagPreferences;

public class ExifTagsPlugin extends AbstractMainWindowPlugin {

	private final ExifTagPreferences exifTagPreferences;

	private Map<ExifTag, JMenu> exifTagMenusByTag = new EnumMap<>(ExifTag.class);

	public ExifTagsPlugin(ExifTagPreferences exifTagPreferences) {
		super();
		this.exifTagPreferences = exifTagPreferences;
	}

	@Override
	public void createMenu() {

		JMenu tagsMenu = mainWindow.getMenu("Tags");

		addCheckboxMenuItem(tagsMenu, "Show tags", SHOW_TAGS_KEY, exifTagPreferences.isTagsVisible(), event -> {
			exifTagPreferences.setTagsVisible(!exifTagPreferences.isTagsVisible());
			repaint();
		});
		addCheckboxMenuItem(tagsMenu, "overriding visibility", SHOW_TAGS_OVERRIDING_PREFERENCES_KEY,
				exifTagPreferences.isOverridingTagsVisibility(), event -> {
					exifTagPreferences.setOverridingTagsVisibility(!exifTagPreferences.isOverridingTagsVisibility());
					repaint();
				});
		tagsMenu.addSeparator();
		for (ExifTag exifTag : ExifTag.values()) {
			JMenu exifTagMenu = new JMenu(exifTag.getDescription());
			exifTagMenusByTag.put(exifTag, exifTagMenu);
			tagsMenu.add(exifTagMenu);
			addCheckboxMenuItem(exifTagMenu, ExifTagVisibility.YES.getDescription(), -1,
					exifTagPreferences.getTagVisibility(exifTag) == ExifTagVisibility.YES, event -> {
						exifTagPreferences.setTagVisibility(exifTag, ExifTagVisibility.YES);
						updateExifTagMenu(exifTag);
						repaint();
					});
			addCheckboxMenuItem(exifTagMenu, ExifTagVisibility.ONLY_IF_DIFFERENT.getDescription(), -1,
					exifTagPreferences.getTagVisibility(exifTag) == ExifTagVisibility.ONLY_IF_DIFFERENT, event -> {
						exifTagPreferences.setTagVisibility(exifTag, ExifTagVisibility.ONLY_IF_DIFFERENT);
						updateExifTagMenu(exifTag);
						repaint();
					});
			addCheckboxMenuItem(exifTagMenu, ExifTagVisibility.NO.getDescription(), -1,
					exifTagPreferences.getTagVisibility(exifTag) == ExifTagVisibility.NO, event -> {
						exifTagPreferences.setTagVisibility(exifTag, ExifTagVisibility.NO);
						updateExifTagMenu(exifTag);
						repaint();
					});
		}

	}

	private void updateExifTagMenu(ExifTag exifTag) {
		JMenu exifTagMenu = exifTagMenusByTag.get(exifTag);
		Component[] items = exifTagMenu.getMenuComponents();
		ExifTagVisibility exifTagVisibility = exifTagPreferences.getTagVisibility(exifTag);
		for (int i = 0; i < items.length; i++) {
			JCheckBoxMenuItem item = (JCheckBoxMenuItem) items[i];
			item.setSelected(exifTagVisibility.getDescription().equals(item.getText()));
		}
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
	}
}
