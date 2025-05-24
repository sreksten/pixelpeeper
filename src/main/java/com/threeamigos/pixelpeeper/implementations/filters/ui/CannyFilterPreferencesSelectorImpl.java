package com.threeamigos.pixelpeeper.implementations.filters.ui;

import com.threeamigos.common.util.interfaces.messagehandler.ExceptionHandler;
import com.threeamigos.pixelpeeper.interfaces.datamodel.DataModel;
import com.threeamigos.pixelpeeper.interfaces.datamodel.ExifImageReader;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavors.CannyEdgesDetectorFilterPreferences;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavors.FilterPreferences;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Properties;

public class CannyFilterPreferencesSelectorImpl extends FilterPreferencesSelectorImpl {

    private static final String LOW_THRESHOLD = "Low threshold";
    private static final String HIGH_THRESHOLD = "High threshold";
    private static final String GAUSSIAN_KERNEL_RADIUS = "Gaussian kernel radius";
    private static final String GAUSSIAN_KERNEL_WIDTH = "Gaussian kernel width";
    private static final String CONTRAST_NORMALIZED = "Contrast normalized";

    private Dimension flavorDimension;

    private final CannyEdgesDetectorFilterPreferencesSelectorDataModel filterPreferencesSelectorDataModel;

    public CannyFilterPreferencesSelectorImpl(FilterPreferences filterPreferences,
                                              CannyEdgesDetectorFilterPreferences cannyEdgesDetectorFilterPreferences, DataModel dataModel,
                                              ExifImageReader exifImageReader, ExceptionHandler exceptionHandler) {
        super(filterPreferences, dataModel, exifImageReader, exceptionHandler);

        filterPreferencesSelectorDataModel = new CannyEdgesDetectorFilterPreferencesSelectorDataModel(dataModel,
                filterPreferences, cannyEdgesDetectorFilterPreferences, testImageCanvas);
        filterPreferencesSelectorDataModel.setSourceImage(testImage);
        filterPreferencesSelectorDataModel.startFilterCalculation();
    }

    @Override
    protected CannyEdgesDetectorFilterPreferencesSelectorDataModel getFilterPreferencesSelectorDataModel() {
        return filterPreferencesSelectorDataModel;
    }

    String getPreferencesDescription() {
        return "Canny Edge Detector preferences";
    }

    JPanel createFlavorPanel(Component component) {

        Properties thresholdSliderLabelTable = new Properties();
        thresholdSliderLabelTable.put(1, new JLabel("0.1"));
        thresholdSliderLabelTable.put(50, new JLabel("5"));
        thresholdSliderLabelTable.put(100, new JLabel("10"));

        Properties gaussianKernelRadiusSliderLabelTable = new Properties();
        gaussianKernelRadiusSliderLabelTable.put(1, new JLabel("0.1"));
        gaussianKernelRadiusSliderLabelTable.put(50, new JLabel("5"));
        gaussianKernelRadiusSliderLabelTable.put(100, new JLabel("10"));

        Properties gaussianKernelWidthSliderLabelTable = new Properties();
        gaussianKernelWidthSliderLabelTable.put(2, new JLabel("2"));
        gaussianKernelWidthSliderLabelTable.put(16, new JLabel("16"));
        gaussianKernelWidthSliderLabelTable.put(32, new JLabel("32"));

        flavorDimension = getMaxDimension(component.getGraphics(), java.util.List.of(LOW_THRESHOLD, HIGH_THRESHOLD,
                GAUSSIAN_KERNEL_RADIUS, GAUSSIAN_KERNEL_WIDTH, CONTRAST_NORMALIZED, TRANSPARENCY));

        JPanel flavorPanel = new JPanel();
        flavorPanel.setLayout(new BoxLayout(flavorPanel, BoxLayout.PAGE_AXIS));

        createSliderPanel(flavorPanel, flavorDimension, LOW_THRESHOLD, filterPreferencesSelectorDataModel.lowThresholdSlider,
                thresholdSliderLabelTable, filterPreferencesSelectorDataModel.lowThresholdText);

        flavorPanel.add(Box.createVerticalStrut(SPACING));

        createSliderPanel(flavorPanel, flavorDimension, HIGH_THRESHOLD, filterPreferencesSelectorDataModel.highThresholdSlider,
                thresholdSliderLabelTable, filterPreferencesSelectorDataModel.highThresholdText);

        flavorPanel.add(Box.createVerticalStrut(SPACING));

        createSliderPanel(flavorPanel, flavorDimension, GAUSSIAN_KERNEL_RADIUS,
                filterPreferencesSelectorDataModel.gaussianKernelRadiusSlider, gaussianKernelRadiusSliderLabelTable,
                filterPreferencesSelectorDataModel.gaussianKernelRadiusText);

        flavorPanel.add(Box.createVerticalStrut(SPACING));

        createSliderPanel(flavorPanel, flavorDimension, GAUSSIAN_KERNEL_WIDTH,
                filterPreferencesSelectorDataModel.gaussianKernelWidthSlider, gaussianKernelWidthSliderLabelTable,
                filterPreferencesSelectorDataModel.gaussianKernelWidthText);

        flavorPanel.add(Box.createVerticalStrut(SPACING));

        createCheckboxPanel(flavorPanel, CONTRAST_NORMALIZED, filterPreferencesSelectorDataModel.contrastNormalizedCheckbox);

        return flavorPanel;
    }

    Dimension getFlavorDimension() {
        return flavorDimension;
    }
}
