package com.threeamigos.imageviewer.implementations.ui;

import java.awt.Component;
import java.awt.image.BufferedImage;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.threeamigos.imageviewer.implementations.datamodel.CannyEdgeDetectorImpl;
import com.threeamigos.imageviewer.interfaces.datamodel.CannyEdgeDetector;
import com.threeamigos.imageviewer.interfaces.preferences.CannyEdgeDetectorPreferences;

public class CannyEdgeDetectorPreferencesSelectorDataModel implements CannyEdgeDetectorPreferences, ChangeListener {

	private static final float NORMALIZATION_VALUE = 10.0f;

	static final int MIN_THRESHOLD = 1;
	static final int MAX_THRESHOLD = 100;
	static final int MIN_TRANSPARENCY = 0;
	static final int MAX_TRANSPARENCY = 100;

	private final CannyEdgeDetectorPreferences cannyEdgeDetectorPreferences;
	private final Component component;

	private BufferedImage sourceImage;
	private BufferedImage edgesImage;

	private int transparency;

	private int lowThreshold;
	private int highThreshold;
	private int gaussianKernelRadius;
	private int gaussianKernelWidth;
	private boolean contrastNormalized;

	JLabel transparencyText;

	JLabel lowThresholdText;
	JLabel highThresholdText;
	JLabel gaussianKernelRadiusText;
	JLabel gaussianKernelWidthText;

	JSlider transparencySlider;

	JSlider lowThresholdSlider;
	JSlider highThresholdSlider;
	JSlider gaussianKernelRadiusSlider;
	JSlider gaussianKernelWidthSlider;
	JCheckBox contrastNormalizedCheckbox;

	CannyEdgeDetectorPreferencesSelectorDataModel(CannyEdgeDetectorPreferences cannyEdgeDetectorPreferences,
			Component component) {
		this.cannyEdgeDetectorPreferences = cannyEdgeDetectorPreferences;
		this.component = component;

		transparency = normalizeTransparency(cannyEdgeDetectorPreferences.getEdgesTransparency());

		lowThreshold = normalize(cannyEdgeDetectorPreferences.getLowThreshold());
		highThreshold = normalize(cannyEdgeDetectorPreferences.getHighThreshold());
		gaussianKernelRadius = normalize(cannyEdgeDetectorPreferences.getGaussianKernelRadius());
		gaussianKernelWidth = cannyEdgeDetectorPreferences.getGaussianKernelWidth();
		contrastNormalized = cannyEdgeDetectorPreferences.isContrastNormalized();

		transparencyText = new JLabel(String.valueOf(transparency));

		lowThresholdText = new JLabel(String.valueOf(cannyEdgeDetectorPreferences.getLowThreshold()));
		highThresholdText = new JLabel(String.valueOf(cannyEdgeDetectorPreferences.getHighThreshold()));
		gaussianKernelRadiusText = new JLabel(String.valueOf(cannyEdgeDetectorPreferences.getGaussianKernelRadius()));
		gaussianKernelWidthText = new JLabel(String.valueOf(cannyEdgeDetectorPreferences.getGaussianKernelWidth()));

		lowThresholdSlider = createSlider(MIN_THRESHOLD, MAX_THRESHOLD, lowThreshold);
		highThresholdSlider = createSlider(MIN_THRESHOLD, MAX_THRESHOLD, highThreshold);
		gaussianKernelRadiusSlider = createSlider(1, 100, gaussianKernelRadius);
		gaussianKernelWidthSlider = createSlider(2, 32, gaussianKernelWidth);
		contrastNormalizedCheckbox = createCheckbox(contrastNormalized);
		transparencySlider = createSlider(MIN_TRANSPARENCY, MAX_TRANSPARENCY, transparency);

	}

	void cancelSelection() {
		cannyEdgeDetectorPreferences.setEdgesTransparency(normalizeTransparency(transparency));
	}

	void acceptSelection() {
		cannyEdgeDetectorPreferences.setEdgesTransparency(normalizeTransparency(transparencySlider.getValue()));

		cannyEdgeDetectorPreferences.setLowThreshold(denormalize(lowThresholdSlider.getValue()));
		cannyEdgeDetectorPreferences.setHighThreshold(denormalize(highThresholdSlider.getValue()));
		cannyEdgeDetectorPreferences.setGaussianKernelRadius(denormalize(gaussianKernelRadiusSlider.getValue()));
		cannyEdgeDetectorPreferences.setGaussianKernelWidth(gaussianKernelWidthSlider.getValue());
		cannyEdgeDetectorPreferences.setContrastNormalized(contrastNormalizedCheckbox.isSelected());
	}

	void reset() {
		cannyEdgeDetectorPreferences.setEdgesTransparency(transparency);

		lowThresholdSlider.setValue(lowThreshold);
		highThresholdSlider.setValue(highThreshold);
		gaussianKernelRadiusSlider.setValue(gaussianKernelRadius);
		gaussianKernelWidthSlider.setValue(gaussianKernelWidth);
		contrastNormalizedCheckbox.setSelected(contrastNormalized);
		transparencySlider.setValue(transparency);

		component.repaint();
	}

	void resetToDefault() {
		cannyEdgeDetectorPreferences
				.setEdgesTransparency(CannyEdgeDetectorPreferences.EDGES_TRANSPARENCY_DEFAULT);
		transparencySlider
				.setValue(normalizeTransparency(CannyEdgeDetectorPreferences.EDGES_TRANSPARENCY_DEFAULT));

		lowThresholdSlider.setValue(normalize(CannyEdgeDetectorPreferences.LOW_THRESHOLD_PREFERENCES_DEFAULT));
		highThresholdSlider.setValue(normalize(CannyEdgeDetectorPreferences.HIGH_THRESHOLD_PREFERENCES_DEFAULT));
		gaussianKernelRadiusSlider.setValue(normalize(CannyEdgeDetectorPreferences.GAUSSIAN_KERNEL_RADIUS_DEFAULT));
		gaussianKernelWidthSlider.setValue(CannyEdgeDetectorPreferences.GAUSSIAN_KERNEL_WIDTH_DEFAULT);
		contrastNormalizedCheckbox.setSelected(CannyEdgeDetectorPreferences.CONTRAST_NORMALIZED_DEFAULT);

		component.repaint();
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

		if (object == transparencySlider) {
			transparencyText.setText(String.valueOf(transparencySlider.getValue()));
			cannyEdgeDetectorPreferences
					.setEdgesTransparency(denormalizeTransparency(transparencySlider.getValue()));
		} else if (object == lowThresholdSlider) {
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

		startEdgesCalculation();
		component.repaint();
	}

	@Override
	public void persist() {
	}

	@Override
	public void setShowEdges(boolean showEdges) {
	}

	@Override
	public boolean isShowEdges() {
		return true;
	}

	public int getEdgesTransparency() {
		return denormalizeTransparency(transparencySlider.getValue());
	}

	public void setEdgesTransparency(int transparency) {
		transparencySlider.setValue(normalizeTransparency(transparency));
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
		return denormalize(highThreshold);
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

	void startEdgesCalculation() {
		if (sourceImage != null) {
			CannyEdgeDetector cannyEdgeDetector = new CannyEdgeDetectorImpl(this);
			cannyEdgeDetector.setSourceImage(sourceImage);
			cannyEdgeDetector.process();
			edgesImage = cannyEdgeDetector.getEdgesImage();
		}
	}

	void setTestImage(BufferedImage testImage) {
		this.sourceImage = testImage;
	}

	BufferedImage getSourceImage() {
		return sourceImage;
	}

	BufferedImage getEdgesImage() {
		return edgesImage;
	}

}
