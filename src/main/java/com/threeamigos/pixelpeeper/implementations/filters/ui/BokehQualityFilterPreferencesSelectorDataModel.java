package com.threeamigos.pixelpeeper.implementations.filters.ui;

import com.threeamigos.pixelpeeper.implementations.filters.flavors.BokehQualityFilterImpl;
import com.threeamigos.pixelpeeper.interfaces.datamodel.DataModel;
import com.threeamigos.pixelpeeper.interfaces.filters.Filter;
import com.threeamigos.pixelpeeper.interfaces.filters.FilterFlavor;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavors.BokehQualityFilterPreferences;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavors.FilterPreferences;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import java.awt.*;

class BokehQualityFilterPreferencesSelectorDataModel extends FilterPreferencesSelectorDataModel {

    private final BokehQualityFilterPreferences preferences;
    private final int sharpnessThresholdBackup;
    private final int patchSizeBackup;

    final JSlider sharpnessThresholdSlider;
    final JLabel sharpnessThresholdText;

    final JSlider patchSizeSlider;
    final JLabel patchSizeText;

    BokehQualityFilterPreferencesSelectorDataModel(DataModel dataModel,
                                                   FilterPreferences filterPreferences,
                                                   BokehQualityFilterPreferences preferences,
                                                   Component component) {
        super(dataModel, filterPreferences, component);
        this.preferences = preferences;

        sharpnessThresholdBackup = preferences.getSharpnessThreshold();
        patchSizeBackup = preferences.getPatchSize();

        sharpnessThresholdSlider = createSlider(
                BokehQualityFilterPreferences.SHARPNESS_THRESHOLD_MIN,
                BokehQualityFilterPreferences.SHARPNESS_THRESHOLD_MAX,
                sharpnessThresholdBackup);
        sharpnessThresholdText = new JLabel(String.valueOf(sharpnessThresholdBackup));

        patchSizeSlider = createSlider(
                BokehQualityFilterPreferences.PATCH_SIZE_MIN,
                BokehQualityFilterPreferences.PATCH_SIZE_MAX,
                patchSizeBackup);
        patchSizeText = new JLabel(String.valueOf(patchSizeBackup));
    }

    @Override
    void cancelSelection() {
        preferences.setSharpnessThreshold(sharpnessThresholdBackup);
        sharpnessThresholdSlider.setValue(sharpnessThresholdBackup);
        preferences.setPatchSize(patchSizeBackup);
        patchSizeSlider.setValue(patchSizeBackup);
    }

    @Override
    void acceptSelection() {
        preferences.setSharpnessThreshold(sharpnessThresholdSlider.getValue());
        preferences.setPatchSize(patchSizeSlider.getValue());
    }

    @Override
    void reset() {
        preferences.setSharpnessThreshold(sharpnessThresholdBackup);
        sharpnessThresholdSlider.setValue(sharpnessThresholdBackup);
        preferences.setPatchSize(patchSizeBackup);
        patchSizeSlider.setValue(patchSizeBackup);
    }

    @Override
    void resetToDefault() {
        int defThreshold = BokehQualityFilterPreferences.SHARPNESS_THRESHOLD_DEFAULT;
        int defPatch = BokehQualityFilterPreferences.PATCH_SIZE_DEFAULT;

        sharpnessThresholdSlider.setValue(defThreshold);
        preferences.setSharpnessThreshold(defThreshold);
        sharpnessThresholdText.setText(String.valueOf(defThreshold));

        patchSizeSlider.setValue(defPatch);
        preferences.setPatchSize(defPatch);
        patchSizeText.setText(String.valueOf(defPatch));
    }

    @Override
    public void handleStateChanged(ChangeEvent e) {
        if (e.getSource() == sharpnessThresholdSlider) {
            int value = sharpnessThresholdSlider.getValue();
            sharpnessThresholdText.setText(String.valueOf(value));
            preferences.setSharpnessThreshold(value);
        } else if (e.getSource() == patchSizeSlider) {
            int value = patchSizeSlider.getValue();
            patchSizeText.setText(String.valueOf(value));
            preferences.setPatchSize(value);
        }
    }

    @Override
    public FilterFlavor getFilterFlavor() {
        return FilterFlavor.BOKEH_QUALITY;
    }

    @Override
    protected Filter getFilterImplementation() {
        return new BokehQualityFilterImpl(preferences);
    }

    @Override
    boolean isAnyCalculationParameterModified() {
        return sharpnessThresholdSlider.getValue() != sharpnessThresholdBackup
                || patchSizeSlider.getValue() != patchSizeBackup;
    }
}
