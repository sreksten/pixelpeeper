package com.threeamigos.pixelpeeper.implementations.ui.plugins;

import com.threeamigos.common.util.interfaces.ui.InputConsumer;
import com.threeamigos.pixelpeeper.data.ExifTag;
import com.threeamigos.pixelpeeper.data.ExifTagVisibility;
import com.threeamigos.pixelpeeper.implementations.ui.InputAdapter;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavours.ExifTagsPreferences;
import com.threeamigos.pixelpeeper.interfaces.ui.KeyRegistry;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public class ExifTagsPlugin extends AbstractMainWindowPlugin {

    private final ExifTagsPreferences exifTagPreferences;

    private final Map<ExifTag, JMenu> exifTagMenusByTag = new EnumMap<>(ExifTag.class);

    private final Map<Integer, JCheckBoxMenuItem> borderThicknessMenuItemsByThickness = new HashMap<>();
    private JMenu borderThicknessMenu;

    public ExifTagsPlugin(ExifTagsPreferences exifTagPreferences) {
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

        borderThicknessMenu = new JMenu("Border rendering");
        tagsMenu.add(borderThicknessMenu);
        addBorderThicknessMenuItem("Line", ExifTagsPreferences.BORDER_THICKNESS_LINE);
        addBorderThicknessMenuItem("Thin shadow", ExifTagsPreferences.BORDER_THICKNESS_THIN);
        addBorderThicknessMenuItem("Medium shadow", ExifTagsPreferences.BORDER_THICKNESS_MEDIUM);
        addBorderThicknessMenuItem("Large shadow", ExifTagsPreferences.BORDER_THICKNESS_LARGE);
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

    private void addBorderThicknessMenuItem(final String description, final int borderThicknessLine) {
        borderThicknessMenuItemsByThickness.put(borderThicknessLine,
                addCheckboxMenuItem(borderThicknessMenu, description, KeyRegistry.NO_KEY,
                        exifTagPreferences.getBorderThickness() == borderThicknessLine,
                        event -> {
                            exifTagPreferences.setBorderThickness(borderThicknessLine);
                            updateBorderThicknessMenu();
                            repaint();
                        })
        );
    }

    private void toggleTagsVisibility() {
        exifTagPreferences.setTagsVisible(!exifTagPreferences.isTagsVisible());
    }

    private void toggleTagsOverriddenVisibility() {
        exifTagPreferences.setOverridingTagsVisibility(!exifTagPreferences.isOverridingTagsVisibility());
    }

    private void updateBorderThicknessMenu() {
        JCheckBoxMenuItem activeItem = borderThicknessMenuItemsByThickness.get(exifTagPreferences.getBorderThickness());
        for (Component component : borderThicknessMenu.getMenuComponents()) {
            JCheckBoxMenuItem item = (JCheckBoxMenuItem) component;
            item.setSelected(item.equals(activeItem));
        }
    }

    private void updateExifTagMenu(ExifTag exifTag) {
        JMenu exifTagMenu = exifTagMenusByTag.get(exifTag);
        ExifTagVisibility exifTagVisibility = exifTagPreferences.getTagVisibility(exifTag);
        for (Component component : exifTagMenu.getMenuComponents()) {
            JCheckBoxMenuItem item = (JCheckBoxMenuItem) component;
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
}
