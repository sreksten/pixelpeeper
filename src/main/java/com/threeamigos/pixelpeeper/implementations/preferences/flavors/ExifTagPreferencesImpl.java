package com.threeamigos.pixelpeeper.implementations.preferences.flavors;

import com.threeamigos.common.util.implementations.BasicPropertyChangeAware;
import com.threeamigos.pixelpeeper.data.ExifTag;
import com.threeamigos.pixelpeeper.data.ExifTagVisibility;
import com.threeamigos.pixelpeeper.interfaces.datamodel.CommunicationMessages;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavors.ExifTagsPreferences;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

public class ExifTagPreferencesImpl extends BasicPropertyChangeAware implements ExifTagsPreferences {

    private boolean tagsVisible = TAGS_VISIBLE_DEFAULT;
    private boolean overridingTagsVisibility = OVERRIDING_TAGS_VISIBILITY_DEFAULT;
    private final Map<ExifTag, ExifTagVisibility> tagsMap = new EnumMap<>(ExifTag.class);
    private int borderThickness = BORDER_THICKNESS_DEFAULT;

    @Override
    public void setTagVisibility(ExifTag tag, ExifTagVisibility tagVisibility) {
        tagsMap.put(tag, tagVisibility);
        firePropertyChange(CommunicationMessages.TAG_VISIBILITY_CHANGED, tag, tagVisibility);
    }

    @Override
    public ExifTagVisibility getTagVisibility(ExifTag tag) {
        ExifTagVisibility value = tagsMap.get(tag);
        return Objects.requireNonNullElse(value, ExifTagVisibility.NO);
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
    public void loadDefaultValues() {
        tagsVisible = TAGS_VISIBLE_DEFAULT;
        overridingTagsVisibility = OVERRIDING_TAGS_VISIBILITY_DEFAULT;
        tagsMap.clear();
        for (ExifTag tag : ExifTag.values()) {
            setTagVisibility(tag, ExifTagVisibility.YES);
        }
    }

    @Override
    public int getBorderThickness() {
        return borderThickness;
    }

    @Override
    public void setBorderThickness(int borderThickness) {
        int oldBorderThickness = this.borderThickness;
        this.borderThickness = borderThickness;
        firePropertyChange(CommunicationMessages.TAGS_RENDERING_CHANGED, oldBorderThickness, borderThickness);
    }

    @Override
    public void validate() {
        if (borderThickness < 1 || borderThickness > BORDER_THICKNESS_MAX) {
            borderThickness = BORDER_THICKNESS_DEFAULT;
        }
    }
}
