package com.threeamigos.pixelpeeper.implementations.filters.ui;

import com.threeamigos.common.util.interfaces.messagehandler.ExceptionHandler;
import com.threeamigos.pixelpeeper.interfaces.datamodel.DataModel;
import com.threeamigos.pixelpeeper.interfaces.datamodel.ExifImageReader;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavors.CannyEdgesDetectorFilterPreferences;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavors.FilterPreferences;

import javax.swing.*;
import java.awt.*;
import java.util.Properties;

public class CannyFilterPreferencesSelectorImpl extends AbstractFilterPreferencesSelectorImpl {

    private static final String LOW_THRESHOLD = "Low threshold";
    private static final String HIGH_THRESHOLD = "High threshold";
    private static final String GAUSSIAN_KERNEL_RADIUS = "Gaussian kernel radius";
    private static final String GAUSSIAN_KERNEL_WIDTH = "Gaussian kernel width";
    private static final String CONTRAST_NORMALIZED = "Contrast normalized";

    private Dimension flavorDimension;

    public CannyFilterPreferencesSelectorImpl(FilterPreferences filterPreferences,
                                              CannyEdgesDetectorFilterPreferences cannyEdgesDetectorFilterPreferences, DataModel dataModel,
                                              ExifImageReader exifImageReader, ExceptionHandler exceptionHandler) {
        super(filterPreferences, dataModel, exifImageReader, exceptionHandler);

        preferencesSelectorDataModel = new CannyEdgesDetectorFilterPreferencesSelectorDataModel(dataModel,
                filterPreferences, cannyEdgesDetectorFilterPreferences, testImageCanvas);
        preferencesSelectorDataModel.setSourceImage(testImage);
        preferencesSelectorDataModel.startFilterCalculation();
    }

    String getPreferencesDescription() {
        return "Canny Edge Detector Preferences";
    }

    JPanel createFlavorPanel(Component component) {

        CannyEdgesDetectorFilterPreferencesSelectorDataModel downcastDatamodel = (CannyEdgesDetectorFilterPreferencesSelectorDataModel) preferencesSelectorDataModel;

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

        flavorDimension = getMaxDimension(component.getGraphics(), LOW_THRESHOLD, HIGH_THRESHOLD,
                GAUSSIAN_KERNEL_RADIUS, GAUSSIAN_KERNEL_WIDTH, CONTRAST_NORMALIZED, TRANSPARENCY);

        JPanel flavorPanel = new JPanel();
        flavorPanel.setLayout(new BoxLayout(flavorPanel, BoxLayout.PAGE_AXIS));

        createSliderPanel(flavorPanel, flavorDimension, LOW_THRESHOLD, downcastDatamodel.lowThresholdSlider,
                thresholdSliderLabelTable, downcastDatamodel.lowThresholdText);

        flavorPanel.add(Box.createVerticalStrut(SPACING));

        createSliderPanel(flavorPanel, flavorDimension, HIGH_THRESHOLD, downcastDatamodel.highThresholdSlider,
                thresholdSliderLabelTable, downcastDatamodel.highThresholdText);

        flavorPanel.add(Box.createVerticalStrut(SPACING));

        createSliderPanel(flavorPanel, flavorDimension, GAUSSIAN_KERNEL_RADIUS,
                downcastDatamodel.gaussianKernelRadiusSlider, gaussianKernelRadiusSliderLabelTable,
                downcastDatamodel.gaussianKernelRadiusText);

        flavorPanel.add(Box.createVerticalStrut(SPACING));

        createSliderPanel(flavorPanel, flavorDimension, GAUSSIAN_KERNEL_WIDTH,
                downcastDatamodel.gaussianKernelWidthSlider, gaussianKernelWidthSliderLabelTable,
                downcastDatamodel.gaussianKernelWidthText);

        flavorPanel.add(Box.createVerticalStrut(SPACING));

        createCheckboxPanel(flavorPanel, CONTRAST_NORMALIZED, downcastDatamodel.contrastNormalizedCheckbox);

        return flavorPanel;
    }

    Dimension getFlavorDimension() {
        return flavorDimension;
    }

}
