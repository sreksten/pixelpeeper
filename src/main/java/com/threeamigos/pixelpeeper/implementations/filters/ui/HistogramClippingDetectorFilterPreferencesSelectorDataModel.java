package com.threeamigos.pixelpeeper.implementations.filters.ui;

import com.threeamigos.pixelpeeper.implementations.filters.flavors.HistogramClippingDetectorFilterImpl;
import com.threeamigos.pixelpeeper.interfaces.datamodel.DataModel;
import com.threeamigos.pixelpeeper.interfaces.filters.Filter;
import com.threeamigos.pixelpeeper.interfaces.filters.FilterFlavor;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavors.FilterPreferences;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavors.HistogramClippingDetectorFilterPreferences;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import java.awt.*;

class HistogramClippingDetectorFilterPreferencesSelectorDataModel extends FilterPreferencesSelectorDataModel {

    private final HistogramClippingDetectorFilterPreferences preferences;
    private final int highlightThresholdBackup;
    private final int shadowThresholdBackup;

    final JSlider highlightThresholdSlider;
    final JLabel highlightThresholdText;

    final JSlider shadowThresholdSlider;
    final JLabel shadowThresholdText;

    HistogramClippingDetectorFilterPreferencesSelectorDataModel(DataModel dataModel,
                                                               FilterPreferences filterPreferences,
                                                               HistogramClippingDetectorFilterPreferences preferences,
                                                               Component component) {
        super(dataModel, filterPreferences, component);
        this.preferences = preferences;

        highlightThresholdBackup = preferences.getHighlightThreshold();
        shadowThresholdBackup = preferences.getShadowThreshold();

        highlightThresholdSlider = createSlider(
                HistogramClippingDetectorFilterPreferences.HIGHLIGHT_THRESHOLD_MIN,
                HistogramClippingDetectorFilterPreferences.HIGHLIGHT_THRESHOLD_MAX,
                highlightThresholdBackup);
        highlightThresholdText = new JLabel(String.valueOf(highlightThresholdBackup));

        shadowThresholdSlider = createSlider(
                HistogramClippingDetectorFilterPreferences.SHADOW_THRESHOLD_MIN,
                HistogramClippingDetectorFilterPreferences.SHADOW_THRESHOLD_MAX,
                shadowThresholdBackup);
        shadowThresholdText = new JLabel(String.valueOf(shadowThresholdBackup));
    }

    @Override
    void cancelSelection() {
        preferences.setHighlightThreshold(highlightThresholdBackup);
        highlightThresholdSlider.setValue(highlightThresholdBackup);
        preferences.setShadowThreshold(shadowThresholdBackup);
        shadowThresholdSlider.setValue(shadowThresholdBackup);
    }

    @Override
    void acceptSelection() {
        preferences.setHighlightThreshold(highlightThresholdSlider.getValue());
        preferences.setShadowThreshold(shadowThresholdSlider.getValue());
    }

    @Override
    void reset() {
        preferences.setHighlightThreshold(highlightThresholdBackup);
        highlightThresholdSlider.setValue(highlightThresholdBackup);
        preferences.setShadowThreshold(shadowThresholdBackup);
        shadowThresholdSlider.setValue(shadowThresholdBackup);
    }

    @Override
    void resetToDefault() {
        int defHighlight = HistogramClippingDetectorFilterPreferences.HIGHLIGHT_THRESHOLD_DEFAULT;
        int defShadow = HistogramClippingDetectorFilterPreferences.SHADOW_THRESHOLD_DEFAULT;
        highlightThresholdSlider.setValue(defHighlight);
        preferences.setHighlightThreshold(defHighlight);
        highlightThresholdText.setText(String.valueOf(defHighlight));
        shadowThresholdSlider.setValue(defShadow);
        preferences.setShadowThreshold(defShadow);
        shadowThresholdText.setText(String.valueOf(defShadow));
    }

    @Override
    public void handleStateChanged(ChangeEvent e) {
        if (e.getSource() == highlightThresholdSlider) {
            int value = highlightThresholdSlider.getValue();
            highlightThresholdText.setText(String.valueOf(value));
            preferences.setHighlightThreshold(value);
        } else if (e.getSource() == shadowThresholdSlider) {
            int value = shadowThresholdSlider.getValue();
            shadowThresholdText.setText(String.valueOf(value));
            preferences.setShadowThreshold(value);
        }
    }

    @Override
    public FilterFlavor getFilterFlavor() {
        return FilterFlavor.HISTOGRAM_CLIPPING_DETECTOR;
    }

    @Override
    protected Filter getFilterImplementation() {
        return new HistogramClippingDetectorFilterImpl(preferences);
    }

    @Override
    boolean isAnyCalculationParameterModified() {
        return highlightThresholdSlider.getValue() != highlightThresholdBackup
                || shadowThresholdSlider.getValue() != shadowThresholdBackup;
    }
}
