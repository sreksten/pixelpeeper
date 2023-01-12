package com.threeamigos.imageviewer.interfaces.datamodel;

import java.awt.image.BufferedImage;

public interface CannyEdgeDetector {

	public void setSourceImage(BufferedImage sourceImage);

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
	
	public void process();
	
	public BufferedImage getEdgesImage();

}
