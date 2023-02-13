package com.threeamigos.pixelpeeper.interfaces.edgedetect.flavours;

import com.threeamigos.pixelpeeper.interfaces.edgedetect.EdgesDetector;

public interface CannyEdgesDetector extends EdgesDetector {

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
