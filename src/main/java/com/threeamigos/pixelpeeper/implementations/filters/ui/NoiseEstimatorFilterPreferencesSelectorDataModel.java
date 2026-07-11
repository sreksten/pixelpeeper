package com.threeamigos.pixelpeeper.implementations.filters.ui;

import com.threeamigos.pixelpeeper.implementations.filters.flavors.NoiseEstimatorFilterImpl;
import com.threeamigos.pixelpeeper.interfaces.datamodel.DataModel;
import com.threeamigos.pixelpeeper.interfaces.filters.Filter;
import com.threeamigos.pixelpeeper.interfaces.filters.FilterFlavor;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavors.FilterPreferences;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavors.NoiseEstimatorFilterPreferences;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import java.awt.*;

class NoiseEstimatorFilterPreferencesSelectorDataModel extends FilterPreferencesSelectorDataModel {

    private final NoiseEstimatorFilterPreferences preferences;
    private final int patchSizeBackup;
    private final int flatVarianceThresholdBackup;

    final JSlider patchSizeSlider;
    final JLabel patchSizeText;

    final JSlider flatVarianceThresholdSlider;
    final JLabel flatVarianceThresholdText;

    NoiseEstimatorFilterPreferencesSelectorDataModel(DataModel dataModel,
                                                    FilterPreferences filterPreferences,
                                                    NoiseEstimatorFilterPreferences preferences,
                                                    Component component) {
        super(dataModel, filterPreferences, component);
        this.preferences = preferences;

        patchSizeBackup = preferences.getPatchSize();
        flatVarianceThresholdBackup = preferences.getFlatVarianceThreshold();

        patchSizeSlider = createSlider(
                NoiseEstimatorFilterPreferences.PATCH_SIZE_MIN,
                NoiseEstimatorFilterPreferences.PATCH_SIZE_MAX,
                patchSizeBackup);
        patchSizeText = new JLabel(String.valueOf(patchSizeBackup));

        flatVarianceThresholdSlider = createSlider(
                NoiseEstimatorFilterPreferences.FLAT_VARIANCE_THRESHOLD_MIN,
                NoiseEstimatorFilterPreferences.FLAT_VARIANCE_THRESHOLD_MAX,
                flatVarianceThresholdBackup);
        flatVarianceThresholdText = new JLabel(String.valueOf(flatVarianceThresholdBackup));
    }

    @Override
    void cancelSelection() {
        preferences.setPatchSize(patchSizeBackup);
        patchSizeSlider.setValue(patchSizeBackup);
        preferences.setFlatVarianceThreshold(flatVarianceThresholdBackup);
        flatVarianceThresholdSlider.setValue(flatVarianceThresholdBackup);
    }

    @Override
    void acceptSelection() {
        preferences.setPatchSize(patchSizeSlider.getValue());
        preferences.setFlatVarianceThreshold(flatVarianceThresholdSlider.getValue());
    }

    @Override
    void reset() {
        preferences.setPatchSize(patchSizeBackup);
        patchSizeSlider.setValue(patchSizeBackup);
        preferences.setFlatVarianceThreshold(flatVarianceThresholdBackup);
        flatVarianceThresholdSlider.setValue(flatVarianceThresholdBackup);
    }

    @Override
    void resetToDefault() {
        int defPatchSize = NoiseEstimatorFilterPreferences.PATCH_SIZE_DEFAULT;
        int defThreshold = NoiseEstimatorFilterPreferences.FLAT_VARIANCE_THRESHOLD_DEFAULT;
        patchSizeSlider.setValue(defPatchSize);
        preferences.setPatchSize(defPatchSize);
        patchSizeText.setText(String.valueOf(defPatchSize));
        flatVarianceThresholdSlider.setValue(defThreshold);
        preferences.setFlatVarianceThreshold(defThreshold);
        flatVarianceThresholdText.setText(String.valueOf(defThreshold));
    }

    @Override
    public void handleStateChanged(ChangeEvent e) {
        if (e.getSource() == patchSizeSlider) {
            int value = patchSizeSlider.getValue();
            patchSizeText.setText(String.valueOf(value));
            preferences.setPatchSize(value);
        } else if (e.getSource() == flatVarianceThresholdSlider) {
            int value = flatVarianceThresholdSlider.getValue();
            flatVarianceThresholdText.setText(String.valueOf(value));
            preferences.setFlatVarianceThreshold(value);
        }
    }

    @Override
    public FilterFlavor getFilterFlavor() {
        return FilterFlavor.NOISE_ESTIMATOR;
    }

    @Override
    protected Filter getFilterImplementation() {
        return new NoiseEstimatorFilterImpl(preferences);
    }

    @Override
    boolean isAnyCalculationParameterModified() {
        return patchSizeSlider.getValue() != patchSizeBackup
                || flatVarianceThresholdSlider.getValue() != flatVarianceThresholdBackup;
    }
}
