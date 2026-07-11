package com.threeamigos.pixelpeeper.implementations.filters.ui;

import com.threeamigos.pixelpeeper.implementations.filters.flavors.VignettingProfileFilterImpl;
import com.threeamigos.pixelpeeper.interfaces.datamodel.DataModel;
import com.threeamigos.pixelpeeper.interfaces.filters.Filter;
import com.threeamigos.pixelpeeper.interfaces.filters.FilterFlavor;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavors.FilterPreferences;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavors.VignettingProfileFilterPreferences;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import java.awt.*;

class VignettingProfileFilterPreferencesSelectorDataModel extends FilterPreferencesSelectorDataModel {

    private final VignettingProfileFilterPreferences preferences;
    private final int ringCountBackup;

    final JSlider ringCountSlider;
    final JLabel ringCountText;

    VignettingProfileFilterPreferencesSelectorDataModel(DataModel dataModel,
                                                       FilterPreferences filterPreferences,
                                                       VignettingProfileFilterPreferences preferences,
                                                       Component component) {
        super(dataModel, filterPreferences, component);
        this.preferences = preferences;

        ringCountBackup = preferences.getRingCount();

        ringCountSlider = createSlider(
                VignettingProfileFilterPreferences.RING_COUNT_MIN,
                VignettingProfileFilterPreferences.RING_COUNT_MAX,
                ringCountBackup);
        ringCountText = new JLabel(String.valueOf(ringCountBackup));
    }

    @Override
    void cancelSelection() {
        preferences.setRingCount(ringCountBackup);
        ringCountSlider.setValue(ringCountBackup);
    }

    @Override
    void acceptSelection() {
        preferences.setRingCount(ringCountSlider.getValue());
    }

    @Override
    void reset() {
        preferences.setRingCount(ringCountBackup);
        ringCountSlider.setValue(ringCountBackup);
    }

    @Override
    void resetToDefault() {
        int defRingCount = VignettingProfileFilterPreferences.RING_COUNT_DEFAULT;
        ringCountSlider.setValue(defRingCount);
        preferences.setRingCount(defRingCount);
        ringCountText.setText(String.valueOf(defRingCount));
    }

    @Override
    public void handleStateChanged(ChangeEvent e) {
        if (e.getSource() == ringCountSlider) {
            int value = ringCountSlider.getValue();
            ringCountText.setText(String.valueOf(value));
            preferences.setRingCount(value);
        }
    }

    @Override
    public FilterFlavor getFilterFlavor() {
        return FilterFlavor.VIGNETTING_PROFILE;
    }

    @Override
    protected Filter getFilterImplementation() {
        return new VignettingProfileFilterImpl(preferences);
    }

    @Override
    boolean isAnyCalculationParameterModified() {
        return ringCountSlider.getValue() != ringCountBackup;
    }
}
