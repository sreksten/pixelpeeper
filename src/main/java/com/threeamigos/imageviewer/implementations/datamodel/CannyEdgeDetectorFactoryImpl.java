package com.threeamigos.imageviewer.implementations.datamodel;

import com.threeamigos.imageviewer.interfaces.datamodel.CannyEdgeDetector;
import com.threeamigos.imageviewer.interfaces.datamodel.CannyEdgeDetectorFactory;
import com.threeamigos.imageviewer.interfaces.preferences.CannyEdgeDetectorPreferences;

public class CannyEdgeDetectorFactoryImpl implements CannyEdgeDetectorFactory {

	private final CannyEdgeDetectorPreferences cannyEdgeDetectorPreferences;
	
	public CannyEdgeDetectorFactoryImpl(CannyEdgeDetectorPreferences cannyEdgeDetectorPreferences) {
		this.cannyEdgeDetectorPreferences = cannyEdgeDetectorPreferences;
	}

	@Override
	public CannyEdgeDetector getCannyEdgeDetector() {
		return new CannyEdgeDetectorImpl(cannyEdgeDetectorPreferences);
	}
	
	
}
