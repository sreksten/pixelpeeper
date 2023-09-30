package com.threeamigos.pixelpeeper.implementations.preferences.flavours;

import com.threeamigos.common.util.implementations.PropertyChangeAwareImpl;
import com.threeamigos.pixelpeeper.interfaces.datamodel.CommunicationMessages;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavours.CannyEdgesDetectorPreferences;

public class CannyEdgesDetectorPreferencesImpl extends PropertyChangeAwareImpl
		implements CannyEdgesDetectorPreferences {

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
		firePropertyChange(CommunicationMessages.CANNY_LOW_THRESHOLD_CHANGED, oldLowThreshold, lowThreshold);
	}

	@Override
	public float getHighThreshold() {
		return highThreshold;
	}

	@Override
	public void setHighThreshold(float highThreshold) {
		float oldHighThreshold = this.highThreshold;
		this.highThreshold = highThreshold;
		firePropertyChange(CommunicationMessages.CANNY_HIGH_THRESHOLD_CHANGED, oldHighThreshold, highThreshold);
	}

	@Override
	public float getGaussianKernelRadius() {
		return gaussianKernelRadius;
	}

	@Override
	public void setGaussianKernelRadius(float gaussianKernelRadius) {
		float oldGaussianKernelRadius = this.gaussianKernelRadius;
		this.gaussianKernelRadius = gaussianKernelRadius;
		firePropertyChange(CommunicationMessages.CANNY_GAUSSIAN_KERNEL_RADIUS_CHANGED, oldGaussianKernelRadius,
				gaussianKernelRadius);
	}

	@Override
	public int getGaussianKernelWidth() {
		return gaussianKernelWidth;
	}

	@Override
	public void setGaussianKernelWidth(int gaussianKernelWidth) {
		float oldGaussianKernelWidth = this.gaussianKernelWidth;
		this.gaussianKernelWidth = gaussianKernelWidth;
		firePropertyChange(CommunicationMessages.CANNY_GAUSSIAN_KERNEL_WIDTH_CHANGED, oldGaussianKernelWidth,
				gaussianKernelWidth);
	}

	@Override
	public boolean isContrastNormalized() {
		return contrastNormalized;
	}

	@Override
	public void setContrastNormalized(boolean contrastNormalized) {
		boolean oldContrastNormalized = this.contrastNormalized;
		this.contrastNormalized = contrastNormalized;
		firePropertyChange(CommunicationMessages.CANNY_CONTRAST_NORMALIZED_CHANGED, oldContrastNormalized,
				contrastNormalized);
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
