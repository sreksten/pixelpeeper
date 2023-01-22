package com.threeamigos.imageviewer.implementations.preferences.flavours;

import com.threeamigos.imageviewer.interfaces.preferences.flavours.CannyEdgesDetectorPreferences;

public class CannyEdgesDetectorPreferencesImpl implements CannyEdgesDetectorPreferences {

	private float lowThreshold;
	private float highThreshold;
	private float gaussianKernelRadius;
	private int gaussianKernelWidth;
	private boolean contrastNormalized;

	@Override
	public float getLowThreshold() {
		return lowThreshold;
	}

	@Override
	public void setLowThreshold(float lowThreshold) {
		this.lowThreshold = lowThreshold;
	}

	@Override
	public float getHighThreshold() {
		return highThreshold;
	}

	@Override
	public void setHighThreshold(float highThreshold) {
		this.highThreshold = highThreshold;
	}

	@Override
	public float getGaussianKernelRadius() {
		return gaussianKernelRadius;
	}

	@Override
	public void setGaussianKernelRadius(float gaussianKernelRadius) {
		this.gaussianKernelRadius = gaussianKernelRadius;
	}

	@Override
	public int getGaussianKernelWidth() {
		return gaussianKernelWidth;
	}

	@Override
	public void setGaussianKernelWidth(int gaussianKernelWidth) {
		this.gaussianKernelWidth = gaussianKernelWidth;
	}

	@Override
	public boolean isContrastNormalized() {
		return contrastNormalized;
	}

	@Override
	public void setContrastNormalized(boolean contrastNormalized) {
		this.contrastNormalized = contrastNormalized;
	}

	@Override
	public void loadDefaultValues() {
		lowThreshold = CannyEdgesDetectorPreferences.LOW_THRESHOLD_PREFERENCES_DEFAULT;
		highThreshold = CannyEdgesDetectorPreferences.HIGH_THRESHOLD_PREFERENCES_DEFAULT;
		gaussianKernelRadius = CannyEdgesDetectorPreferences.GAUSSIAN_KERNEL_RADIUS_DEFAULT;
		gaussianKernelWidth = CannyEdgesDetectorPreferences.GAUSSIAN_KERNEL_WIDTH_DEFAULT;
		contrastNormalized = CannyEdgesDetectorPreferences.CONTRAST_NORMALIZED_DEFAULT;
	}

	@Override
	public void validate() {
		// TODO Auto-generated method stub

	}

}
