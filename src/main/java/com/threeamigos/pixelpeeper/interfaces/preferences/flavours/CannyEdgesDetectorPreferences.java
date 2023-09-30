package com.threeamigos.pixelpeeper.interfaces.preferences.flavours;

import com.threeamigos.common.util.interfaces.preferences.Preferences;

public interface CannyEdgesDetectorPreferences extends Preferences {

	public static final float LOW_THRESHOLD_PREFERENCES_DEFAULT = 2.5f;
	public static final float HIGH_THRESHOLD_PREFERENCES_DEFAULT = 7.5f;
	public static final float GAUSSIAN_KERNEL_RADIUS_DEFAULT = 2f;
	public static final int GAUSSIAN_KERNEL_WIDTH_DEFAULT = 16;
	public static final boolean CONTRAST_NORMALIZED_DEFAULT = false;

	default String getDescription() {
		return "Canny Edges Detector preferences";
	}

	public float getLowThreshold();

	public void setLowThreshold(float lowThreshold);

	public float getHighThreshold();

	public void setHighThreshold(float highThreshold);

	public float getGaussianKernelRadius();

	public void setGaussianKernelRadius(float gaussianKernelRadius);

	public int getGaussianKernelWidth();

	public void setGaussianKernelWidth(int gaussianKernelWidth);

	public boolean isContrastNormalized();

	public void setContrastNormalized(boolean contrastNormalized);

}
