package com.threeamigos.imageviewer.interfaces.preferences;

import com.threeamigos.imageviewer.interfaces.persister.Persistable;

public interface CannyEdgeDetectorPreferences extends Persistable {

	public static final boolean SHOWING_EDGE_IMAGES_DEFAULT = false;
	public static final int EDGE_IMAGES_TRANSPARENCY_DEFAULT = 30;

	public static final float LOW_THRESHOLD_PREFERENCES_DEFAULT = 2.5f;
	public static final float HIGH_THRESHOLD_PREFERENCES_DEFAULT = 7.5f;
	public static final float GAUSSIAN_KERNEL_RADIUS_DEFAULT = 2f;
	public static final int GAUSSIAN_KERNEL_WIDTH_DEFAULT = 16;
	public static final boolean CONTRAST_NORMALIZED_DEFAULT = false;

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

	public void setShowEdgeImages(boolean showEdgeImages);

	public boolean isShowEdgeImages();

	public void setEdgeImagesTransparency(int edgeImagesTransparency);

	public int getEdgeImagesTransparency();

}
