package com.threeamigos.imageviewer.implementations.preferences;

import com.threeamigos.common.util.interfaces.ErrorMessageHandler;
import com.threeamigos.imageviewer.implementations.ui.AbstractPreferencesImpl;
import com.threeamigos.imageviewer.interfaces.persister.Persister;
import com.threeamigos.imageviewer.interfaces.preferences.CannyEdgesDetectorPreferences;

public class CannyEdgesDetectorPreferencesImpl extends AbstractPreferencesImpl<CannyEdgesDetectorPreferences>
		implements CannyEdgesDetectorPreferences {

	private float lowThreshold;
	private float highThreshold;
	private float gaussianKernelRadius;
	private int gaussianKernelWidth;
	private boolean contrastNormalized;

	@Override
	protected String getEntityDescription() {
		return "canny edge detector";
	}

	public CannyEdgesDetectorPreferencesImpl(Persister<CannyEdgesDetectorPreferences> persister,
			ErrorMessageHandler errorMessageHandler) {
		super(persister, errorMessageHandler);

		loadPostConstruct();
	}

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
	protected void loadDefaultValues() {
		lowThreshold = CannyEdgesDetectorPreferences.LOW_THRESHOLD_PREFERENCES_DEFAULT;
		highThreshold = CannyEdgesDetectorPreferences.HIGH_THRESHOLD_PREFERENCES_DEFAULT;
		gaussianKernelRadius = CannyEdgesDetectorPreferences.GAUSSIAN_KERNEL_RADIUS_DEFAULT;
		gaussianKernelWidth = CannyEdgesDetectorPreferences.GAUSSIAN_KERNEL_WIDTH_DEFAULT;
		contrastNormalized = CannyEdgesDetectorPreferences.CONTRAST_NORMALIZED_DEFAULT;
	}

}
