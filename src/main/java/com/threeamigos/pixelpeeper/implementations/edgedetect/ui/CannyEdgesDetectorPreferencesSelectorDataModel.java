package com.threeamigos.pixelpeeper.implementations.edgedetect.ui;

import com.threeamigos.pixelpeeper.implementations.edgedetect.flavours.CannyEdgesDetectorImpl;
import com.threeamigos.pixelpeeper.interfaces.datamodel.DataModel;
import com.threeamigos.pixelpeeper.interfaces.edgedetect.EdgesDetector;
import com.threeamigos.pixelpeeper.interfaces.edgedetect.EdgesDetectorFlavour;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavours.CannyEdgesDetectorPreferences;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavours.EdgesDetectorPreferences;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import java.awt.*;

public class CannyEdgesDetectorPreferencesSelectorDataModel extends AbstractEdgesDetectorPreferencesSelectorDataModel
        implements CannyEdgesDetectorPreferences {

    private static final float NORMALIZATION_VALUE = 10.0f;

    private static final int MIN_THRESHOLD = 1;
    private static final int MAX_THRESHOLD = 100;

    private final CannyEdgesDetectorPreferences cannyEdgesDetectorPreferences;

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

    CannyEdgesDetectorPreferencesSelectorDataModel(DataModel dataModel,
                                                   EdgesDetectorPreferences edgesDetectorPreferences,
                                                   CannyEdgesDetectorPreferences cannyEdgesDetectorPreferences, Component component) {
        super(dataModel, edgesDetectorPreferences, component);
        this.cannyEdgesDetectorPreferences = cannyEdgesDetectorPreferences;

        lowThresholdBackup = normalize(cannyEdgesDetectorPreferences.getLowThreshold());
        highThresholdBackup = normalize(cannyEdgesDetectorPreferences.getHighThreshold());
        gaussianKernelRadiusBackup = normalize(cannyEdgesDetectorPreferences.getGaussianKernelRadius());
        gaussianKernelWidthBackup = cannyEdgesDetectorPreferences.getGaussianKernelWidth();
        contrastNormalizedBackup = cannyEdgesDetectorPreferences.isContrastNormalized();

        lowThresholdText = new JLabel(String.valueOf(cannyEdgesDetectorPreferences.getLowThreshold()));
        highThresholdText = new JLabel(String.valueOf(cannyEdgesDetectorPreferences.getHighThreshold()));
        gaussianKernelRadiusText = new JLabel(String.valueOf(cannyEdgesDetectorPreferences.getGaussianKernelRadius()));
        gaussianKernelWidthText = new JLabel(String.valueOf(cannyEdgesDetectorPreferences.getGaussianKernelWidth()));

        lowThresholdSlider = createSlider(MIN_THRESHOLD, MAX_THRESHOLD, lowThresholdBackup);
        highThresholdSlider = createSlider(MIN_THRESHOLD, MAX_THRESHOLD, highThresholdBackup);
        gaussianKernelRadiusSlider = createSlider(1, 100, gaussianKernelRadiusBackup);
        gaussianKernelWidthSlider = createSlider(2, 32, gaussianKernelWidthBackup);
        contrastNormalizedCheckbox = createCheckbox(contrastNormalizedBackup);
    }

    void cancelSelection() {
        cannyEdgesDetectorPreferences.setLowThreshold(denormalize(lowThresholdBackup));
        cannyEdgesDetectorPreferences.setHighThreshold(denormalize(highThresholdBackup));
        cannyEdgesDetectorPreferences.setGaussianKernelRadius(denormalize(gaussianKernelRadiusBackup));
        cannyEdgesDetectorPreferences.setGaussianKernelWidth(gaussianKernelWidthBackup);
        cannyEdgesDetectorPreferences.setContrastNormalized(contrastNormalizedBackup);
    }

    void acceptSelection() {
        cannyEdgesDetectorPreferences.setLowThreshold(denormalize(lowThresholdSlider.getValue()));
        cannyEdgesDetectorPreferences.setHighThreshold(denormalize(highThresholdSlider.getValue()));
        cannyEdgesDetectorPreferences.setGaussianKernelRadius(denormalize(gaussianKernelRadiusSlider.getValue()));
        cannyEdgesDetectorPreferences.setGaussianKernelWidth(gaussianKernelWidthSlider.getValue());
        cannyEdgesDetectorPreferences.setContrastNormalized(contrastNormalizedCheckbox.isSelected());
    }

    void reset() {
        lowThresholdSlider.setValue(lowThresholdBackup);
        highThresholdSlider.setValue(highThresholdBackup);
        gaussianKernelRadiusSlider.setValue(gaussianKernelRadiusBackup);
        gaussianKernelWidthSlider.setValue(gaussianKernelWidthBackup);
        contrastNormalizedCheckbox.setSelected(contrastNormalizedBackup);
    }

    void resetToDefault() {
        lowThresholdSlider.setValue(normalize(CannyEdgesDetectorPreferences.LOW_THRESHOLD_PREFERENCES_DEFAULT));
        highThresholdSlider.setValue(normalize(CannyEdgesDetectorPreferences.HIGH_THRESHOLD_PREFERENCES_DEFAULT));
        gaussianKernelRadiusSlider.setValue(normalize(CannyEdgesDetectorPreferences.GAUSSIAN_KERNEL_RADIUS_DEFAULT));
        gaussianKernelWidthSlider.setValue(CannyEdgesDetectorPreferences.GAUSSIAN_KERNEL_WIDTH_DEFAULT);
        contrastNormalizedCheckbox.setSelected(CannyEdgesDetectorPreferences.CONTRAST_NORMALIZED_DEFAULT);
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
    public EdgesDetectorFlavour getEdgesDetectorFlavour() {
        return EdgesDetectorFlavour.CANNY_EDGES_DETECTOR;
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
    protected EdgesDetector getEdgesDetectorImplementation() {
        return new CannyEdgesDetectorImpl(this);
    }

    @Override
    boolean isAnyCalculationParameterModified() {
        return normalize(cannyEdgesDetectorPreferences.getLowThreshold()) != lowThresholdSlider.getValue()
                || normalize(cannyEdgesDetectorPreferences.getHighThreshold()) != highThresholdSlider.getValue()
                || normalize(cannyEdgesDetectorPreferences.getGaussianKernelRadius()) != gaussianKernelRadiusSlider
                .getValue()
                || cannyEdgesDetectorPreferences.getGaussianKernelWidth() != gaussianKernelWidthSlider.getValue()
                || cannyEdgesDetectorPreferences.isContrastNormalized() != contrastNormalizedCheckbox.isSelected();
    }

    @Override
    public String getDescription() {
        return CannyEdgesDetectorPreferences.super.getDescription();
    }
}
