package com.threeamigos.pixelpeeper.implementations.filters.ui;

import com.threeamigos.pixelpeeper.implementations.filters.flavors.DepthOfFieldFilterImpl;
import com.threeamigos.pixelpeeper.interfaces.datamodel.DataModel;
import com.threeamigos.pixelpeeper.interfaces.filters.Filter;
import com.threeamigos.pixelpeeper.interfaces.filters.FilterFlavor;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavors.DepthOfFieldFilterPreferences;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavors.FilterPreferences;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import java.awt.*;

class DepthOfFieldFilterPreferencesSelectorDataModel extends FilterPreferencesSelectorDataModel {

    private final DepthOfFieldFilterPreferences preferences;
    private final int cocDenominatorBackup;

    final JSlider cocDenominatorSlider;
    final JLabel cocDenominatorText;

    DepthOfFieldFilterPreferencesSelectorDataModel(DataModel dataModel,
                                                  FilterPreferences filterPreferences,
                                                  DepthOfFieldFilterPreferences preferences,
                                                  Component component) {
        super(dataModel, filterPreferences, component);
        this.preferences = preferences;

        cocDenominatorBackup = preferences.getCocDenominator();

        cocDenominatorSlider = createSlider(
                DepthOfFieldFilterPreferences.COC_DENOMINATOR_MIN,
                DepthOfFieldFilterPreferences.COC_DENOMINATOR_MAX,
                cocDenominatorBackup);
        cocDenominatorText = new JLabel(String.valueOf(cocDenominatorBackup));
    }

    @Override
    void cancelSelection() {
        preferences.setCocDenominator(cocDenominatorBackup);
        cocDenominatorSlider.setValue(cocDenominatorBackup);
    }

    @Override
    void acceptSelection() {
        preferences.setCocDenominator(cocDenominatorSlider.getValue());
    }

    @Override
    void reset() {
        preferences.setCocDenominator(cocDenominatorBackup);
        cocDenominatorSlider.setValue(cocDenominatorBackup);
    }

    @Override
    void resetToDefault() {
        int def = DepthOfFieldFilterPreferences.COC_DENOMINATOR_DEFAULT;
        cocDenominatorSlider.setValue(def);
        preferences.setCocDenominator(def);
        cocDenominatorText.setText(String.valueOf(def));
    }

    @Override
    public void handleStateChanged(ChangeEvent e) {
        if (e.getSource() == cocDenominatorSlider) {
            int value = cocDenominatorSlider.getValue();
            cocDenominatorText.setText(String.valueOf(value));
            preferences.setCocDenominator(value);
        }
    }

    @Override
    public FilterFlavor getFilterFlavor() {
        return FilterFlavor.DEPTH_OF_FIELD;
    }

    @Override
    protected Filter getFilterImplementation() {
        // No ExifMap is available in the preview context; the filter handles null gracefully.
        return new DepthOfFieldFilterImpl(preferences);
    }

    @Override
    boolean isAnyCalculationParameterModified() {
        return cocDenominatorSlider.getValue() != cocDenominatorBackup;
    }
}
