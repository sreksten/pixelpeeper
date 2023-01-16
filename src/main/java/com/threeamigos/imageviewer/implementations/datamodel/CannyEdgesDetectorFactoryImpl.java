package com.threeamigos.imageviewer.implementations.datamodel;

import com.threeamigos.imageviewer.interfaces.datamodel.CannyEdgesDetector;
import com.threeamigos.imageviewer.interfaces.datamodel.CannyEdgesDetectorFactory;
import com.threeamigos.imageviewer.interfaces.preferences.CannyEdgesDetectorPreferences;

public class CannyEdgesDetectorFactoryImpl implements CannyEdgesDetectorFactory {

	private final CannyEdgesDetectorPreferences cannyEdgesDetectorPreferences;

	public CannyEdgesDetectorFactoryImpl(CannyEdgesDetectorPreferences cannysEdgesDetectorPreferences) {
		this.cannyEdgesDetectorPreferences = cannysEdgesDetectorPreferences;
	}

	@Override
	public CannyEdgesDetector getCannyEdgesDetector() {
		return new CannyEdgesDetectorImpl(cannyEdgesDetectorPreferences);
	}

}
