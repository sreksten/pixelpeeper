package com.threeamigos.imageviewer.implementations.preferences.flavours;

import com.threeamigos.imageviewer.interfaces.StatusTracker;
import com.threeamigos.imageviewer.interfaces.preferences.flavours.CannyEdgesDetectorPreferences;

public class CannyEdgesDetectorPreferencesStatusTracker implements StatusTracker<CannyEdgesDetectorPreferences> {

	private float lowThresholdAtStart;
	private float highThresholdAtStart;
	private float gaussianKernelRadiusAtStart;
	private int gaussianKernelWidthAtStart;
	private boolean contrastNormalizedAtStart;

	private final CannyEdgesDetectorPreferences cannyEdgesDetectorPreferences;

	public CannyEdgesDetectorPreferencesStatusTracker(CannyEdgesDetectorPreferences cannyEdgesDetectorPreferences) {
		this.cannyEdgesDetectorPreferences = cannyEdgesDetectorPreferences;
	}

	@Override
	public void loadInitialValues() {
		lowThresholdAtStart = cannyEdgesDetectorPreferences.getLowThreshold();
		highThresholdAtStart = cannyEdgesDetectorPreferences.getHighThreshold();
		gaussianKernelRadiusAtStart = cannyEdgesDetectorPreferences.getGaussianKernelRadius();
		gaussianKernelWidthAtStart = cannyEdgesDetectorPreferences.getGaussianKernelWidth();
		contrastNormalizedAtStart = cannyEdgesDetectorPreferences.isContrastNormalized();
	}

	@Override
	public boolean hasChanged() {
		return cannyEdgesDetectorPreferences.getLowThreshold() != lowThresholdAtStart
				|| cannyEdgesDetectorPreferences.getHighThreshold() != highThresholdAtStart
				|| cannyEdgesDetectorPreferences.getGaussianKernelRadius() != gaussianKernelRadiusAtStart
				|| cannyEdgesDetectorPreferences.getGaussianKernelWidth() != gaussianKernelWidthAtStart
				|| cannyEdgesDetectorPreferences.isContrastNormalized() != contrastNormalizedAtStart;
	}

}
