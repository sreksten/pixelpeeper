package com.threeamigos.pixelpeeper.implementations.edgedetect.ui;

import com.threeamigos.pixelpeeper.implementations.edgedetect.flavours.SobelEdgesDetectorImpl;
import com.threeamigos.pixelpeeper.interfaces.datamodel.DataModel;
import com.threeamigos.pixelpeeper.interfaces.edgedetect.EdgesDetector;
import com.threeamigos.pixelpeeper.interfaces.edgedetect.EdgesDetectorFlavour;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavours.EdgesDetectorPreferences;

import javax.swing.event.ChangeEvent;
import java.awt.*;

public class SobelEdgesDetectorPreferencesSelectorDataModel extends AbstractEdgesDetectorPreferencesSelectorDataModel {

    SobelEdgesDetectorPreferencesSelectorDataModel(DataModel dataModel,
                                                   EdgesDetectorPreferences edgesDetectorPreferences, Component component) {
        super(dataModel, edgesDetectorPreferences, component);
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
    public EdgesDetectorFlavour getEdgesDetectorFlavour() {
        return EdgesDetectorFlavour.SOBEL_EDGES_DETECTOR;
    }

    @Override
    protected EdgesDetector getEdgesDetectorImplementation() {
        return new SobelEdgesDetectorImpl();
    }

    @Override
    boolean isAnyCalculationParameterModified() {
        return false;
    }
}
