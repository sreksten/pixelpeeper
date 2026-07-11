package com.threeamigos.pixelpeeper.implementations.filters.ui;

import com.threeamigos.pixelpeeper.implementations.filters.flavors.EquivalentExposureFilterImpl;
import com.threeamigos.pixelpeeper.interfaces.datamodel.DataModel;
import com.threeamigos.pixelpeeper.interfaces.filters.Filter;
import com.threeamigos.pixelpeeper.interfaces.filters.FilterFlavor;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavors.FilterPreferences;

import javax.swing.event.ChangeEvent;
import java.awt.*;

public class EquivalentExposureFilterPreferencesSelectorDataModel extends FilterPreferencesSelectorDataModel {

    EquivalentExposureFilterPreferencesSelectorDataModel(DataModel dataModel,
                                                         FilterPreferences filterPreferences,
                                                         Component component) {
        super(dataModel, filterPreferences, component);
    }

    void cancelSelection() {
        // Equivalent Exposure has no actual parameters
    }

    void acceptSelection() {
        // Equivalent Exposure has no actual parameters
    }

    void reset() {
        // Equivalent Exposure has no actual parameters
    }

    void resetToDefault() {
        // Equivalent Exposure has no actual parameters
    }

    public void handleStateChanged(ChangeEvent e) {
        // Equivalent Exposure has no actual parameters
    }

    @Override
    public FilterFlavor getFilterFlavor() {
        return FilterFlavor.EQUIVALENT_EXPOSURE;
    }

    @Override
    protected Filter getFilterImplementation() {
        return new EquivalentExposureFilterImpl();
    }

    @Override
    boolean isAnyCalculationParameterModified() {
        return false;
    }
}
