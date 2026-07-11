package com.threeamigos.pixelpeeper.implementations.filters.ui;

import com.threeamigos.common.util.interfaces.messagehandler.ThrowableHandler;
import com.threeamigos.pixelpeeper.interfaces.datamodel.DataModel;
import com.threeamigos.pixelpeeper.interfaces.datamodel.ExifImageReader;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavors.ChromaticAberrationFilterPreferences;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavors.FilterPreferences;

import javax.swing.*;
import java.awt.*;
import java.util.Properties;

public class ChromaticAberrationFilterPreferencesSelectorImpl extends FilterPreferencesSelectorImpl {

    private static final Dimension FLAVOR_DIMENSION = new Dimension(300, 100);

    private final ChromaticAberrationFilterPreferencesSelectorDataModel filterPreferencesSelectorDataModel;

    public ChromaticAberrationFilterPreferencesSelectorImpl(
            FilterPreferences filterPreferences,
            ChromaticAberrationFilterPreferences chromaticAberrationFilterPreferences,
            DataModel dataModel, ExifImageReader exifImageReader, ThrowableHandler exceptionHandler) {
        super(filterPreferences, dataModel, exifImageReader, exceptionHandler);

        filterPreferencesSelectorDataModel = new ChromaticAberrationFilterPreferencesSelectorDataModel(
                dataModel, filterPreferences, chromaticAberrationFilterPreferences, testImageCanvas);
        filterPreferencesSelectorDataModel.setSourceImage(testImage);
        filterPreferencesSelectorDataModel.startFilterCalculation();
    }

    @Override
    public ChromaticAberrationFilterPreferencesSelectorDataModel getFilterPreferencesSelectorDataModel() {
        return filterPreferencesSelectorDataModel;
    }

    @Override
    String getPreferencesDescription() {
        return "Chromatic Aberration Filter Preferences";
    }

    @Override
    JPanel createFlavorPanel(Component component) {
        JPanel flavorPanel = new JPanel();
        flavorPanel.setLayout(new BoxLayout(flavorPanel, BoxLayout.PAGE_AXIS));

        Properties edgeLabelTable = new Properties();
        edgeLabelTable.put(ChromaticAberrationFilterPreferences.EDGE_THRESHOLD_MIN,
                new JLabel(String.valueOf(ChromaticAberrationFilterPreferences.EDGE_THRESHOLD_MIN)));
        edgeLabelTable.put(ChromaticAberrationFilterPreferences.EDGE_THRESHOLD_MAX,
                new JLabel(String.valueOf(ChromaticAberrationFilterPreferences.EDGE_THRESHOLD_MAX)));

        createSliderPanel(flavorPanel, FLAVOR_DIMENSION, "Edge threshold",
                filterPreferencesSelectorDataModel.edgeThresholdSlider, edgeLabelTable,
                filterPreferencesSelectorDataModel.edgeThresholdText);

        flavorPanel.add(Box.createVerticalStrut(SPACING));

        Properties sensLabelTable = new Properties();
        sensLabelTable.put(ChromaticAberrationFilterPreferences.SENSITIVITY_MIN,
                new JLabel(String.valueOf(ChromaticAberrationFilterPreferences.SENSITIVITY_MIN)));
        sensLabelTable.put(ChromaticAberrationFilterPreferences.SENSITIVITY_MAX,
                new JLabel(String.valueOf(ChromaticAberrationFilterPreferences.SENSITIVITY_MAX)));

        createSliderPanel(flavorPanel, FLAVOR_DIMENSION, "Sensitivity",
                filterPreferencesSelectorDataModel.sensitivitySlider, sensLabelTable,
                filterPreferencesSelectorDataModel.sensitivityText);

        return flavorPanel;
    }

    @Override
    Dimension getFlavorDimension() {
        return FLAVOR_DIMENSION;
    }
}
