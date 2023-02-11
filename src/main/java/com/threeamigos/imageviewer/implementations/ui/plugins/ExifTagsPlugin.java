package com.threeamigos.imageviewer.implementations.ui.plugins;

import java.awt.Component;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.util.EnumMap;
import java.util.Map;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;

import com.threeamigos.imageviewer.data.ExifTag;
import com.threeamigos.imageviewer.data.ExifTagVisibility;
import com.threeamigos.imageviewer.implementations.ui.InputAdapter;
import com.threeamigos.imageviewer.interfaces.preferences.flavours.ExifTagPreferences;
import com.threeamigos.imageviewer.interfaces.ui.InputConsumer;
import com.threeamigos.imageviewer.interfaces.ui.KeyRegistry;

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

		addCheckboxMenuItem(tagsMenu, "Show tags", KeyRegistry.SHOW_TAGS_KEY, exifTagPreferences.isTagsVisible(),
				event -> toggleTagsVisibility());
		addCheckboxMenuItem(tagsMenu, "overriding visibility", KeyRegistry.SHOW_TAGS_OVERRIDING_PREFERENCES_KEY,
				exifTagPreferences.isOverridingTagsVisibility(), event -> toggleTagsOverriddenVisibility());
		tagsMenu.addSeparator();
		for (ExifTag exifTag : ExifTag.values()) {
			JMenu exifTagMenu = new JMenu(exifTag.getDescription());
			exifTagMenusByTag.put(exifTag, exifTagMenu);
			tagsMenu.add(exifTagMenu);
			addCheckboxMenuItem(exifTagMenu, ExifTagVisibility.YES.getDescription(), KeyRegistry.NO_KEY,
					exifTagPreferences.getTagVisibility(exifTag) == ExifTagVisibility.YES, event -> {
						exifTagPreferences.setTagVisibility(exifTag, ExifTagVisibility.YES);
						updateExifTagMenu(exifTag);
						repaint();
					});
			addCheckboxMenuItem(exifTagMenu, ExifTagVisibility.ONLY_IF_DIFFERENT.getDescription(), KeyRegistry.NO_KEY,
					exifTagPreferences.getTagVisibility(exifTag) == ExifTagVisibility.ONLY_IF_DIFFERENT, event -> {
						exifTagPreferences.setTagVisibility(exifTag, ExifTagVisibility.ONLY_IF_DIFFERENT);
						updateExifTagMenu(exifTag);
						repaint();
					});
			addCheckboxMenuItem(exifTagMenu, ExifTagVisibility.NO.getDescription(), KeyRegistry.NO_KEY,
					exifTagPreferences.getTagVisibility(exifTag) == ExifTagVisibility.NO, event -> {
						exifTagPreferences.setTagVisibility(exifTag, ExifTagVisibility.NO);
						updateExifTagMenu(exifTag);
						repaint();
					});
		}

	}

	private void toggleTagsVisibility() {
		exifTagPreferences.setTagsVisible(!exifTagPreferences.isTagsVisible());
	}

	private void toggleTagsOverriddenVisibility() {
		exifTagPreferences.setOverridingTagsVisibility(!exifTagPreferences.isOverridingTagsVisibility());
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

	public InputConsumer getInputConsumer() {

		return new InputAdapter() {

			@Override
			public void keyPressed(KeyEvent e) {
				int key = e.getKeyCode();
				if (key == KeyRegistry.SHOW_TAGS_KEY.getKeyCode()) {
					toggleTagsVisibility();
				} else if (key == KeyRegistry.SHOW_TAGS_OVERRIDING_PREFERENCES_KEY.getKeyCode()) {
					toggleTagsOverriddenVisibility();
				}
			}
		};
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
	}
}
