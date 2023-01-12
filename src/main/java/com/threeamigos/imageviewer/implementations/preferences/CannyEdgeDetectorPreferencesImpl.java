package com.threeamigos.imageviewer.implementations.preferences;

import com.threeamigos.common.util.interfaces.MessageConsumer;
import com.threeamigos.imageviewer.implementations.ui.AbstractPreferencesImpl;
import com.threeamigos.imageviewer.interfaces.persister.Persister;
import com.threeamigos.imageviewer.interfaces.preferences.CannyEdgeDetectorPreferences;

public class CannyEdgeDetectorPreferencesImpl extends AbstractPreferencesImpl<CannyEdgeDetectorPreferences> implements CannyEdgeDetectorPreferences {

	private float lowThreshold = 2.5f;
	private float highThreshold = 7.5f;
	private float gaussianKernelRadius = 2f;
	private int gaussianKernelWidth = 16;
	private boolean contrastNormalized = false;

	@Override
	protected String getEntityDescription() {
		return "canny edge detector";
	}

	public CannyEdgeDetectorPreferencesImpl(Persister<CannyEdgeDetectorPreferences> persister, MessageConsumer messageConsumer) {
		super(persister, messageConsumer);

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
		lowThreshold = 2.5f;
		highThreshold = 7.5f;
		gaussianKernelRadius = 2f;
		gaussianKernelWidth = 16;
		contrastNormalized = false;
	}
	
}
