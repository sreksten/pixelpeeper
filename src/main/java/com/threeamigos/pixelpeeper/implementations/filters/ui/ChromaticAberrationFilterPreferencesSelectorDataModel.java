package com.threeamigos.pixelpeeper.implementations.filters.ui;

import com.threeamigos.pixelpeeper.implementations.filters.flavors.ChromaticAberrationFilterImpl;
import com.threeamigos.pixelpeeper.interfaces.datamodel.DataModel;
import com.threeamigos.pixelpeeper.interfaces.filters.Filter;
import com.threeamigos.pixelpeeper.interfaces.filters.FilterFlavor;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavors.ChromaticAberrationFilterPreferences;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavors.FilterPreferences;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import java.awt.*;

class ChromaticAberrationFilterPreferencesSelectorDataModel extends FilterPreferencesSelectorDataModel {

    private final ChromaticAberrationFilterPreferences preferences;
    private final int edgeThresholdBackup;
    private final int sensitivityBackup;

    final JSlider edgeThresholdSlider;
    final JLabel edgeThresholdText;

    final JSlider sensitivitySlider;
    final JLabel sensitivityText;

    ChromaticAberrationFilterPreferencesSelectorDataModel(DataModel dataModel,
                                                          FilterPreferences filterPreferences,
                                                          ChromaticAberrationFilterPreferences preferences,
                                                          Component component) {
        super(dataModel, filterPreferences, component);
        this.preferences = preferences;

        edgeThresholdBackup = preferences.getEdgeThreshold();
        sensitivityBackup = preferences.getSensitivity();

        edgeThresholdSlider = createSlider(
                ChromaticAberrationFilterPreferences.EDGE_THRESHOLD_MIN,
                ChromaticAberrationFilterPreferences.EDGE_THRESHOLD_MAX,
                edgeThresholdBackup);
        edgeThresholdText = new JLabel(String.valueOf(edgeThresholdBackup));

        sensitivitySlider = createSlider(
                ChromaticAberrationFilterPreferences.SENSITIVITY_MIN,
                ChromaticAberrationFilterPreferences.SENSITIVITY_MAX,
                sensitivityBackup);
        sensitivityText = new JLabel(String.valueOf(sensitivityBackup));
    }

    @Override
    void cancelSelection() {
        preferences.setEdgeThreshold(edgeThresholdBackup);
        edgeThresholdSlider.setValue(edgeThresholdBackup);
        preferences.setSensitivity(sensitivityBackup);
        sensitivitySlider.setValue(sensitivityBackup);
    }

    @Override
    void acceptSelection() {
        preferences.setEdgeThreshold(edgeThresholdSlider.getValue());
        preferences.setSensitivity(sensitivitySlider.getValue());
    }

    @Override
    void reset() {
        preferences.setEdgeThreshold(edgeThresholdBackup);
        edgeThresholdSlider.setValue(edgeThresholdBackup);
        preferences.setSensitivity(sensitivityBackup);
        sensitivitySlider.setValue(sensitivityBackup);
    }

    @Override
    void resetToDefault() {
        int defEdge = ChromaticAberrationFilterPreferences.EDGE_THRESHOLD_DEFAULT;
        int defSens = ChromaticAberrationFilterPreferences.SENSITIVITY_DEFAULT;

        edgeThresholdSlider.setValue(defEdge);
        preferences.setEdgeThreshold(defEdge);
        edgeThresholdText.setText(String.valueOf(defEdge));

        sensitivitySlider.setValue(defSens);
        preferences.setSensitivity(defSens);
        sensitivityText.setText(String.valueOf(defSens));
    }

    @Override
    public void handleStateChanged(ChangeEvent e) {
        if (e.getSource() == edgeThresholdSlider) {
            int value = edgeThresholdSlider.getValue();
            edgeThresholdText.setText(String.valueOf(value));
            preferences.setEdgeThreshold(value);
        } else if (e.getSource() == sensitivitySlider) {
            int value = sensitivitySlider.getValue();
            sensitivityText.setText(String.valueOf(value));
            preferences.setSensitivity(value);
        }
    }

    @Override
    public FilterFlavor getFilterFlavor() {
        return FilterFlavor.CHROMATIC_ABERRATION;
    }

    @Override
    protected Filter getFilterImplementation() {
        return new ChromaticAberrationFilterImpl(preferences);
    }

    @Override
    boolean isAnyCalculationParameterModified() {
        return edgeThresholdSlider.getValue() != edgeThresholdBackup
                || sensitivitySlider.getValue() != sensitivityBackup;
    }
}
