package com.threeamigos.imageviewer.implementations.ui;

import java.awt.Component;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.threeamigos.imageviewer.interfaces.preferences.CannyEdgeDetectorPreferences;
import com.threeamigos.imageviewer.interfaces.preferences.WindowPreferences;

public class CannyEdgeDetectorPreferencesSelectorDataModel implements ChangeListener {

	private static final float NORMALIZATION_VALUE = 10.0f;

	static final int MIN_TRANSPARENCY = 0;
	static final int MAX_TRANSPARENCY = 100;

	private final WindowPreferences windowPreferences;
	private final CannyEdgeDetectorPreferences cannyEdgeDetectorPreferences;
	private final Component component;

	private int lowThreshold;
	private int highThreshold;
	private int gaussianKernelRadius;
	private int gaussianKernelWidth;
	private boolean contrastNormalized;
	private int transparency;

	JLabel lowThresholdText;
	JLabel highThresholdText;
	JLabel gaussianKernelRadiusText;
	JLabel gaussianKernelWidthText;
	JLabel transparencyText;

	JSlider lowThresholdSlider;
	JSlider highThresholdSlider;
	JSlider gaussianKernelRadiusSlider;
	JSlider gaussianKernelWidthSlider;
	JCheckBox contrastNormalizedCheckbox;
	JSlider transparencySlider;

	CannyEdgeDetectorPreferencesSelectorDataModel(WindowPreferences windowPreferences,
			CannyEdgeDetectorPreferences cannyEdgeDetectorPreferences, Component component) {
		this.windowPreferences = windowPreferences;
		this.cannyEdgeDetectorPreferences = cannyEdgeDetectorPreferences;
		this.component = component;

		lowThreshold = normalize(cannyEdgeDetectorPreferences.getLowThreshold());
		highThreshold = normalize(cannyEdgeDetectorPreferences.getHighThreshold());
		gaussianKernelRadius = normalize(cannyEdgeDetectorPreferences.getGaussianKernelRadius());
		gaussianKernelWidth = cannyEdgeDetectorPreferences.getGaussianKernelWidth();
		contrastNormalized = cannyEdgeDetectorPreferences.isContrastNormalized();
		transparency = normalizeTransparency(windowPreferences.getEdgeImagesTransparency());

		lowThresholdText = new JLabel(String.valueOf(cannyEdgeDetectorPreferences.getLowThreshold()));
		highThresholdText = new JLabel(String.valueOf(cannyEdgeDetectorPreferences.getHighThreshold()));
		gaussianKernelRadiusText = new JLabel(String.valueOf(cannyEdgeDetectorPreferences.getGaussianKernelRadius()));
		gaussianKernelWidthText = new JLabel(String.valueOf(cannyEdgeDetectorPreferences.getGaussianKernelWidth()));
		transparencyText = new JLabel(String.valueOf(windowPreferences.getEdgeImagesTransparency()));

		lowThresholdSlider = createSlider(0, 100, lowThreshold);
		highThresholdSlider = createSlider(0, 100, highThreshold);
		gaussianKernelRadiusSlider = createSlider(1, 100, gaussianKernelRadius);
		gaussianKernelWidthSlider = createSlider(2, 32, gaussianKernelWidth);
		contrastNormalizedCheckbox = createCheckbox(contrastNormalized);
		transparencySlider = createSlider(MIN_TRANSPARENCY, MAX_TRANSPARENCY, transparency);

	}

	void cancelSelection() {
		windowPreferences.setEdgeImagesTransparency(normalizeTransparency(transparency));
	}

	void acceptSelection() {
		cannyEdgeDetectorPreferences.setLowThreshold(denormalize(lowThresholdSlider.getValue()));
		cannyEdgeDetectorPreferences.setHighThreshold(denormalize(highThresholdSlider.getValue()));
		cannyEdgeDetectorPreferences.setGaussianKernelRadius(denormalize(gaussianKernelRadiusSlider.getValue()));
		cannyEdgeDetectorPreferences.setGaussianKernelWidth(gaussianKernelWidthSlider.getValue());
		cannyEdgeDetectorPreferences.setContrastNormalized(contrastNormalizedCheckbox.isSelected());
		windowPreferences.setEdgeImagesTransparency(normalizeTransparency(transparencySlider.getValue()));
	}

	void reset() {
		lowThresholdSlider.setValue(lowThreshold);
		highThresholdSlider.setValue(highThreshold);
		gaussianKernelRadiusSlider.setValue(gaussianKernelRadius);
		gaussianKernelWidthSlider.setValue(gaussianKernelWidth);
		contrastNormalizedCheckbox.setSelected(contrastNormalized);
	}

	void resetToDefault() {
		lowThresholdSlider.setValue(normalize(CannyEdgeDetectorPreferences.LOW_THRESHOLD_PREFERENCES_DEFAULT));
		highThresholdSlider.setValue(normalize(CannyEdgeDetectorPreferences.HIGH_THRESHOLD_PREFERENCES_DEFAULT));
		gaussianKernelRadiusSlider.setValue(normalize(CannyEdgeDetectorPreferences.GAUSSIAN_KERNEL_RADIUS_DEFAULT));
		gaussianKernelWidthSlider.setValue(CannyEdgeDetectorPreferences.GAUSSIAN_KERNEL_WIDTH_DEFAULT);
		contrastNormalizedCheckbox.setSelected(CannyEdgeDetectorPreferences.CONTRAST_NORMALIZED_DEFAULT);
	}

	private JSlider createSlider(int minValue, int maxValue, int currentValue) {
		JSlider slider = new JSlider(JSlider.HORIZONTAL, minValue, maxValue, currentValue);
		slider.addChangeListener(this);
		return slider;
	}

	private JCheckBox createCheckbox(boolean currentValue) {
		JCheckBox checkbox = new JCheckBox();
		checkbox.setSelected(currentValue);
		checkbox.addChangeListener(this);
		return checkbox;
	}

	private int normalize(float value) {
		return (int) (value * NORMALIZATION_VALUE);
	}

	private float denormalize(int value) {
		return value / NORMALIZATION_VALUE;
	}

	private int normalizeTransparency(int transparency) {
		return MAX_TRANSPARENCY - transparency;
	}

	private int denormalizeTransparency(int transparency) {
		return normalizeTransparency(transparency);
	}

	public void stateChanged(ChangeEvent e) {
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
		} else if (object == transparencySlider) {
			transparencyText.setText(String.valueOf(denormalizeTransparency(transparencySlider.getValue())));
			windowPreferences.setEdgeImagesTransparency(denormalizeTransparency(transparencySlider.getValue()));
			component.repaint();
		}
	}

}
