package com.threeamigos.pixelpeeper.implementations.filters.ui;

import com.threeamigos.pixelpeeper.implementations.filters.flavors.SobelEdgesDetectorFilterImpl;
import com.threeamigos.pixelpeeper.interfaces.datamodel.DataModel;
import com.threeamigos.pixelpeeper.interfaces.filters.Filter;
import com.threeamigos.pixelpeeper.interfaces.filters.FilterFlavor;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavors.FilterPreferences;

import javax.swing.event.ChangeEvent;
import java.awt.*;

public class SobelEdgesDetectorPreferencesSelectorDataModel extends AbstractFilterPreferencesSelectorDataModel {

    SobelEdgesDetectorPreferencesSelectorDataModel(DataModel dataModel,
                                                   FilterPreferences filterPreferences, Component component) {
        super(dataModel, filterPreferences, component);
    }

    void cancelSelection() {
        // Sobel Edges Detector has no actual parameters
    }

    void acceptSelection() {
        // Sobel Edges Detector has no actual parameters
    }

    void reset() {
        // Sobel Edges Detector has no actual parameters
    }

    void resetToDefault() {
        // Sobel Edges Detector has no actual parameters
    }

    public void handleStateChanged(ChangeEvent e) {
        // Sobel Edges Detector has no actual parameters
    }

    @Override
    public FilterFlavor getFilterFlavor() {
        return FilterFlavor.SOBEL_EDGES_DETECTOR;
    }

    @Override
    protected Filter getFilterImplementation() {
        return new SobelEdgesDetectorFilterImpl();
    }

    @Override
    boolean isAnyCalculationParameterModified() {
        return false;
    }
}
