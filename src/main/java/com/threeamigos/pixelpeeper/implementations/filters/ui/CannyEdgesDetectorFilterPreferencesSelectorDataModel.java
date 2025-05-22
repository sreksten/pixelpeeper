package com.threeamigos.pixelpeeper.implementations.filters.ui;

import com.threeamigos.pixelpeeper.implementations.filters.flavors.CannyEdgesDetectorFilterImpl;
import com.threeamigos.pixelpeeper.interfaces.datamodel.DataModel;
import com.threeamigos.pixelpeeper.interfaces.filters.Filter;
import com.threeamigos.pixelpeeper.interfaces.filters.FilterFlavor;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavors.CannyEdgesDetectorFilterPreferences;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavors.FilterPreferences;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import java.awt.*;

public class CannyEdgesDetectorFilterPreferencesSelectorDataModel extends AbstractFilterPreferencesSelectorDataModel
        implements CannyEdgesDetectorFilterPreferences {

    private static final float NORMALIZATION_VALUE = 10.0f;

    private static final int MIN_THRESHOLD = 1;
    private static final int MAX_THRESHOLD = 100;

    private final CannyEdgesDetectorFilterPreferences cannyEdgesDetectorFilterPreferences;

    private final int lowThresholdBackup;
    private final int highThresholdBackup;
    private final int gaussianKernelRadiusBackup;
    private final int gaussianKernelWidthBackup;
    private final boolean contrastNormalizedBackup;

    JLabel lowThresholdText;
    JLabel highThresholdText;
    JLabel gaussianKernelRadiusText;
    JLabel gaussianKernelWidthText;

    JSlider lowThresholdSlider;
    JSlider highThresholdSlider;
    JSlider gaussianKernelRadiusSlider;
    JSlider gaussianKernelWidthSlider;
    JCheckBox contrastNormalizedCheckbox;

    CannyEdgesDetectorFilterPreferencesSelectorDataModel(DataModel dataModel,
                                                         FilterPreferences filterPreferences,
                                                         CannyEdgesDetectorFilterPreferences cannyEdgesDetectorFilterPreferences, Component component) {
        super(dataModel, filterPreferences, component);
        this.cannyEdgesDetectorFilterPreferences = cannyEdgesDetectorFilterPreferences;

        lowThresholdBackup = normalize(cannyEdgesDetectorFilterPreferences.getLowThreshold());
        highThresholdBackup = normalize(cannyEdgesDetectorFilterPreferences.getHighThreshold());
        gaussianKernelRadiusBackup = normalize(cannyEdgesDetectorFilterPreferences.getGaussianKernelRadius());
        gaussianKernelWidthBackup = cannyEdgesDetectorFilterPreferences.getGaussianKernelWidth();
        contrastNormalizedBackup = cannyEdgesDetectorFilterPreferences.isContrastNormalized();

        lowThresholdText = new JLabel(String.valueOf(cannyEdgesDetectorFilterPreferences.getLowThreshold()));
        highThresholdText = new JLabel(String.valueOf(cannyEdgesDetectorFilterPreferences.getHighThreshold()));
        gaussianKernelRadiusText = new JLabel(String.valueOf(cannyEdgesDetectorFilterPreferences.getGaussianKernelRadius()));
        gaussianKernelWidthText = new JLabel(String.valueOf(cannyEdgesDetectorFilterPreferences.getGaussianKernelWidth()));

        lowThresholdSlider = createSlider(MIN_THRESHOLD, MAX_THRESHOLD, lowThresholdBackup);
        highThresholdSlider = createSlider(MIN_THRESHOLD, MAX_THRESHOLD, highThresholdBackup);
        gaussianKernelRadiusSlider = createSlider(1, 100, gaussianKernelRadiusBackup);
        gaussianKernelWidthSlider = createSlider(2, 32, gaussianKernelWidthBackup);
        contrastNormalizedCheckbox = createCheckbox(contrastNormalizedBackup);
    }

    void cancelSelection() {
        cannyEdgesDetectorFilterPreferences.setLowThreshold(denormalize(lowThresholdBackup));
        cannyEdgesDetectorFilterPreferences.setHighThreshold(denormalize(highThresholdBackup));
        cannyEdgesDetectorFilterPreferences.setGaussianKernelRadius(denormalize(gaussianKernelRadiusBackup));
        cannyEdgesDetectorFilterPreferences.setGaussianKernelWidth(gaussianKernelWidthBackup);
        cannyEdgesDetectorFilterPreferences.setContrastNormalized(contrastNormalizedBackup);
    }

    void acceptSelection() {
        cannyEdgesDetectorFilterPreferences.setLowThreshold(denormalize(lowThresholdSlider.getValue()));
        cannyEdgesDetectorFilterPreferences.setHighThreshold(denormalize(highThresholdSlider.getValue()));
        cannyEdgesDetectorFilterPreferences.setGaussianKernelRadius(denormalize(gaussianKernelRadiusSlider.getValue()));
        cannyEdgesDetectorFilterPreferences.setGaussianKernelWidth(gaussianKernelWidthSlider.getValue());
        cannyEdgesDetectorFilterPreferences.setContrastNormalized(contrastNormalizedCheckbox.isSelected());
    }

    void reset() {
        lowThresholdSlider.setValue(lowThresholdBackup);
        highThresholdSlider.setValue(highThresholdBackup);
        gaussianKernelRadiusSlider.setValue(gaussianKernelRadiusBackup);
        gaussianKernelWidthSlider.setValue(gaussianKernelWidthBackup);
        contrastNormalizedCheckbox.setSelected(contrastNormalizedBackup);
    }

    void resetToDefault() {
        lowThresholdSlider.setValue(normalize(CannyEdgesDetectorFilterPreferences.LOW_THRESHOLD_PREFERENCES_DEFAULT));
        highThresholdSlider.setValue(normalize(CannyEdgesDetectorFilterPreferences.HIGH_THRESHOLD_PREFERENCES_DEFAULT));
        gaussianKernelRadiusSlider.setValue(normalize(CannyEdgesDetectorFilterPreferences.GAUSSIAN_KERNEL_RADIUS_DEFAULT));
        gaussianKernelWidthSlider.setValue(CannyEdgesDetectorFilterPreferences.GAUSSIAN_KERNEL_WIDTH_DEFAULT);
        contrastNormalizedCheckbox.setSelected(CannyEdgesDetectorFilterPreferences.CONTRAST_NORMALIZED_DEFAULT);
    }

    private int normalize(float value) {
        return (int) (value * NORMALIZATION_VALUE);
    }

    private float denormalize(int value) {
        return value / NORMALIZATION_VALUE;
    }

    public void handleStateChanged(ChangeEvent e) {
        Object object = e.getSource();

        if (object == lowThresholdSlider) {
            lowThresholdText.setText(String.valueOf(denormalize(lowThresholdSlider.getValue())));
            if (lowThresholdSlider.getValue() > highThresholdSlider.getValue()) {
                highThresholdSlider.setValue(lowThresholdSlider.getValue());
                highThresholdText.setText(String.valueOf(denormalize(highThresholdSlider.getValue())));
            }
        } else if (object == highThresholdSlider) {
            highThresholdText.setText(String.valueOf(denormalize(highThresholdSlider.getValue())));
            if (highThresholdSlider.getValue() < lowThresholdSlider.getValue()) {
                lowThresholdSlider.setValue(highThresholdSlider.getValue());
                lowThresholdText.setText(String.valueOf(denormalize(lowThresholdSlider.getValue())));
            }
        } else if (object == gaussianKernelRadiusSlider) {
            gaussianKernelRadiusText.setText(String.valueOf(denormalize(gaussianKernelRadiusSlider.getValue())));
        } else if (object == gaussianKernelWidthSlider) {
            gaussianKernelWidthText.setText(String.valueOf(gaussianKernelWidthSlider.getValue()));
        }
    }

    @Override
    public FilterFlavor getFilterFlavor() {
        return FilterFlavor.CANNY_EDGES_DETECTOR;
    }

    @Override
    public float getLowThreshold() {
        return denormalize(lowThresholdSlider.getValue());
    }

    @Override
    public void setLowThreshold(float lowThreshold) {
        lowThresholdSlider.setValue(normalize(lowThreshold));
    }

    @Override
    public float getHighThreshold() {
        return denormalize(highThresholdBackup);
    }

    @Override
    public void setHighThreshold(float highThreshold) {
        highThresholdSlider.setValue(normalize(highThreshold));
    }

    @Override
    public float getGaussianKernelRadius() {
        return denormalize(gaussianKernelRadiusSlider.getValue());
    }

    @Override
    public void setGaussianKernelRadius(float gaussianKernelRadius) {
        gaussianKernelRadiusSlider.setValue(normalize(gaussianKernelRadius));
    }

    @Override
    public int getGaussianKernelWidth() {
        return gaussianKernelWidthSlider.getValue();
    }

    @Override
    public void setGaussianKernelWidth(int gaussianKernelWidth) {
        gaussianKernelWidthSlider.setValue(gaussianKernelWidth);
    }

    @Override
    public boolean isContrastNormalized() {
        return contrastNormalizedCheckbox.isSelected();
    }

    @Override
    public void setContrastNormalized(boolean contrastNormalized) {
        contrastNormalizedCheckbox.setSelected(contrastNormalized);
    }

    @Override
    protected Filter getFilterImplementation() {
        return new CannyEdgesDetectorFilterImpl(this);
    }

    @Override
    boolean isAnyCalculationParameterModified() {
        return normalize(cannyEdgesDetectorFilterPreferences.getLowThreshold()) != lowThresholdSlider.getValue()
                || normalize(cannyEdgesDetectorFilterPreferences.getHighThreshold()) != highThresholdSlider.getValue()
                || normalize(cannyEdgesDetectorFilterPreferences.getGaussianKernelRadius()) != gaussianKernelRadiusSlider
                .getValue()
                || cannyEdgesDetectorFilterPreferences.getGaussianKernelWidth() != gaussianKernelWidthSlider.getValue()
                || cannyEdgesDetectorFilterPreferences.isContrastNormalized() != contrastNormalizedCheckbox.isSelected();
    }

    @Override
    public String getDescription() {
        return CannyEdgesDetectorFilterPreferences.super.getDescription();
    }
}
