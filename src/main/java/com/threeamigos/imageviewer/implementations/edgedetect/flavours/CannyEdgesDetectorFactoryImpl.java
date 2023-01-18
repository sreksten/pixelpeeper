package com.threeamigos.imageviewer.implementations.edgedetect.flavours;

import com.threeamigos.imageviewer.interfaces.edgedetect.flavours.CannyEdgesDetector;
import com.threeamigos.imageviewer.interfaces.edgedetect.flavours.CannyEdgesDetectorFactory;
import com.threeamigos.imageviewer.interfaces.preferences.flavours.CannyEdgesDetectorPreferences;

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
