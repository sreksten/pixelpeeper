package com.threeamigos.pixelpeeper.implementations.edgedetect.ui;

import com.threeamigos.common.util.interfaces.messagehandler.ExceptionHandler;
import com.threeamigos.pixelpeeper.interfaces.datamodel.DataModel;
import com.threeamigos.pixelpeeper.interfaces.datamodel.ExifImageReader;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavours.C64EdgesDetectorPreferences;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavours.EdgesDetectorPreferences;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavours.ZXSpectrumEdgesDetectorPreferences;

import javax.swing.*;
import java.awt.*;
import java.util.Properties;

public class C64EdgesDetectorPreferencesSelectorImpl extends AbstractEdgesDetectorPreferencesSelectorImpl {

    private static final String COLOR_CLASH_ENABLED = "Color Clash";
    private static final String SATURATION_THRESHOLD = "Min saturation before grayscale mapping";
    private static final String LIGHTNESS_MIN_THRESHOLD = "Min lightness before black mapping";
    private static final String LIGHTNESS_MAX_THRESHOLD = "Max lightness before white mapping";
    private static final String COLOR_MAPPING = "Color mapping";
    private static final String HUE_WEIGHT = "Hue weight";
    private static final String SATURATION_WEIGHT = "Saturation weight";
    private static final String LIGHTNESS_WEIGHT = "Lightness weight";
    private static final String SKIN_TONES_MAPPING_ENABLED = "Skin tones mapping";

    private Dimension flavourDimension;

    public C64EdgesDetectorPreferencesSelectorImpl(EdgesDetectorPreferences edgesDetectorPreferences,
                                                   C64EdgesDetectorPreferences c64EdgesDetectorPreferences,
                                                   DataModel dataModel, ExifImageReader exifImageReader, ExceptionHandler exceptionHandler) {
        super(edgesDetectorPreferences, dataModel, exifImageReader, exceptionHandler);

        preferencesSelectorDataModel = new C64EdgesDetectorPreferencesSelectorDataModel(dataModel,
                edgesDetectorPreferences, c64EdgesDetectorPreferences, testImageCanvas);
        preferencesSelectorDataModel.setSourceImage(testImage);
        preferencesSelectorDataModel.startEdgesCalculation();
    }

    String getPreferencesDescription() {
        return "C64 Edge Detector Preferences";
    }

    JPanel createFlavourPanel(Component component) {

        C64EdgesDetectorPreferencesSelectorDataModel downcastDatamodel =
                (C64EdgesDetectorPreferencesSelectorDataModel) preferencesSelectorDataModel;

        Properties saturationThresholdSliderLabelTable = new Properties();
        saturationThresholdSliderLabelTable.put(1, new JLabel("0"));
        saturationThresholdSliderLabelTable.put(50, new JLabel("50"));
        saturationThresholdSliderLabelTable.put(100, new JLabel("100"));

        Properties lightnessMinThresholdSliderLabelTable = new Properties();
        lightnessMinThresholdSliderLabelTable.put(1, new JLabel("0"));
        lightnessMinThresholdSliderLabelTable.put(50, new JLabel("50"));
        lightnessMinThresholdSliderLabelTable.put(100, new JLabel("100"));

        Properties lightnessMaxThresholdSliderLabelTable = new Properties();
        lightnessMaxThresholdSliderLabelTable.put(1, new JLabel("0"));
        lightnessMaxThresholdSliderLabelTable.put(50, new JLabel("50"));
        lightnessMaxThresholdSliderLabelTable.put(100, new JLabel("100"));

        Properties hueWeightSliderLabelTable = new Properties();
        hueWeightSliderLabelTable.put(1, new JLabel("0.0"));
        hueWeightSliderLabelTable.put(50, new JLabel("0.5"));
        hueWeightSliderLabelTable.put(100, new JLabel("1.0"));

        Properties saturationWeightSliderLabelTable = new Properties();
        saturationWeightSliderLabelTable.put(1, new JLabel("0.0"));
        saturationWeightSliderLabelTable.put(50, new JLabel("0.5"));
        saturationWeightSliderLabelTable.put(100, new JLabel("1.0"));

        Properties lightnessWeightSliderLabelTable = new Properties();
        lightnessWeightSliderLabelTable.put(1, new JLabel("0.0"));
        lightnessWeightSliderLabelTable.put(50, new JLabel("0.5"));
        lightnessWeightSliderLabelTable.put(100, new JLabel("1.0"));

        flavourDimension = getMaxDimension(component.getGraphics(), COLOR_CLASH_ENABLED,
                SATURATION_THRESHOLD, LIGHTNESS_MIN_THRESHOLD, LIGHTNESS_MAX_THRESHOLD,
                HUE_WEIGHT, SATURATION_WEIGHT, LIGHTNESS_WEIGHT, SKIN_TONES_MAPPING_ENABLED);

        JPanel flavourPanel = new JPanel();
        flavourPanel.setLayout(new BoxLayout(flavourPanel, BoxLayout.PAGE_AXIS));

        createCheckboxPanel(flavourPanel, COLOR_CLASH_ENABLED, downcastDatamodel.colorClashEnabledCheckbox);

        flavourPanel.add(Box.createVerticalStrut(SPACING));

        createSliderPanel(flavourPanel, flavourDimension, SATURATION_THRESHOLD,
                downcastDatamodel.saturationThresholdSlider,
                saturationThresholdSliderLabelTable,
                downcastDatamodel.saturationThresholdText);

        flavourPanel.add(Box.createVerticalStrut(SPACING));

        createSliderPanel(flavourPanel, flavourDimension, LIGHTNESS_MIN_THRESHOLD,
                downcastDatamodel.lightnessMinThresholdSlider,
                lightnessMinThresholdSliderLabelTable,
                downcastDatamodel.lightnessMinThresholdText);

        flavourPanel.add(Box.createVerticalStrut(SPACING));

        createSliderPanel(flavourPanel, flavourDimension, LIGHTNESS_MAX_THRESHOLD,
                downcastDatamodel.lightnessMaxThresholdSlider,
                lightnessMaxThresholdSliderLabelTable,
                downcastDatamodel.lightnessMaxThresholdText);

        flavourPanel.add(Box.createVerticalStrut(SPACING));

        flavourPanel.add(new JLabel(COLOR_MAPPING));

        createSliderPanel(flavourPanel, flavourDimension, HUE_WEIGHT,
                downcastDatamodel.hueWeightSlider,
                hueWeightSliderLabelTable,
                downcastDatamodel.hueWeightText);

        flavourPanel.add(Box.createVerticalStrut(SPACING));

        createSliderPanel(flavourPanel, flavourDimension, SATURATION_WEIGHT,
                downcastDatamodel.saturationWeightSlider,
                saturationWeightSliderLabelTable,
                downcastDatamodel.saturationWeightText);

        flavourPanel.add(Box.createVerticalStrut(SPACING));

        createSliderPanel(flavourPanel, flavourDimension, LIGHTNESS_WEIGHT,
                downcastDatamodel.lightnessWeightSlider,
                lightnessWeightSliderLabelTable,
                downcastDatamodel.lightnessWeightText);

        flavourPanel.add(Box.createVerticalStrut(SPACING));

        createCheckboxPanel(flavourPanel, SKIN_TONES_MAPPING_ENABLED, downcastDatamodel.skinTonesMappingEnabledCheckbox);

        return flavourPanel;
    }

    Dimension getFlavourDimension() {
        return flavourDimension;
    }

}
