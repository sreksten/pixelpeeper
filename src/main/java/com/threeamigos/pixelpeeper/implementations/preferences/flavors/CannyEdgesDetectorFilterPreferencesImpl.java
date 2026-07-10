package com.threeamigos.pixelpeeper.implementations.preferences.flavors;

import com.threeamigos.common.util.implementations.BasicPropertyChangeAware;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavors.CannyEdgesDetectorFilterPreferences;

public class CannyEdgesDetectorFilterPreferencesImpl extends BasicPropertyChangeAware
		implements CannyEdgesDetectorFilterPreferences {

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
		float oldLowThreshold = this.lowThreshold;
		this.lowThreshold = lowThreshold;
	}

	@Override
	public float getHighThreshold() {
		return highThreshold;
	}

	@Override
	public void setHighThreshold(float highThreshold) {
		float oldHighThreshold = this.highThreshold;
		this.highThreshold = highThreshold;
	}

	@Override
	public float getGaussianKernelRadius() {
		return gaussianKernelRadius;
	}

	@Override
	public void setGaussianKernelRadius(float gaussianKernelRadius) {
		float oldGaussianKernelRadius = this.gaussianKernelRadius;
		this.gaussianKernelRadius = gaussianKernelRadius;
	}

	@Override
	public int getGaussianKernelWidth() {
		return gaussianKernelWidth;
	}

	@Override
	public void setGaussianKernelWidth(int gaussianKernelWidth) {
		float oldGaussianKernelWidth = this.gaussianKernelWidth;
		this.gaussianKernelWidth = gaussianKernelWidth;
	}

	@Override
	public boolean isContrastNormalized() {
		return contrastNormalized;
	}

	@Override
	public void setContrastNormalized(boolean contrastNormalized) {
		boolean oldContrastNormalized = this.contrastNormalized;
		this.contrastNormalized = contrastNormalized;
	}

	@Override
	public void loadDefaultValues() {
		lowThreshold = CannyEdgesDetectorFilterPreferences.LOW_THRESHOLD_PREFERENCES_DEFAULT;
		highThreshold = CannyEdgesDetectorFilterPreferences.HIGH_THRESHOLD_PREFERENCES_DEFAULT;
		gaussianKernelRadius = CannyEdgesDetectorFilterPreferences.GAUSSIAN_KERNEL_RADIUS_DEFAULT;
		gaussianKernelWidth = CannyEdgesDetectorFilterPreferences.GAUSSIAN_KERNEL_WIDTH_DEFAULT;
		contrastNormalized = CannyEdgesDetectorFilterPreferences.CONTRAST_NORMALIZED_DEFAULT;
	}

	@Override
	public void validate() {
		if (lowThreshold < 0) {
			throw new IllegalArgumentException("Invalid low threshold");
		}
		if (highThreshold < 0) {
			throw new IllegalArgumentException("Invalid high threshold");
		}
		if (gaussianKernelRadius < 0.1f) {
			throw new IllegalArgumentException("Invalid Gaussian kernel radius");
		}
		if (gaussianKernelWidth < 2) {
			throw new IllegalArgumentException("Invalid Gaussian kernel width");
		}
	}

}
