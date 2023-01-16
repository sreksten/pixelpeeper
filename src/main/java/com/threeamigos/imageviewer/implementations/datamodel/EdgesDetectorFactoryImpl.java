package com.threeamigos.imageviewer.implementations.datamodel;

import com.threeamigos.imageviewer.interfaces.datamodel.EdgesDetector;
import com.threeamigos.imageviewer.interfaces.datamodel.EdgesDetectorFactory;
import com.threeamigos.imageviewer.interfaces.preferences.CannyEdgesDetectorPreferences;
import com.threeamigos.imageviewer.interfaces.preferences.EdgesDetectorPreferences;
import com.threeamigos.imageviewer.interfaces.preferences.RomyJonaEdgesDetectorPreferences;

public class EdgesDetectorFactoryImpl implements EdgesDetectorFactory {

	private final EdgesDetectorPreferences edgesDetectorPreferences;
	private final CannyEdgesDetectorPreferences cannyEdgesDetectorPreferences;
	private final RomyJonaEdgesDetectorPreferences romyJonaEdgesDetectorPreferences;

	public EdgesDetectorFactoryImpl(EdgesDetectorPreferences edgesDetectorPreferences,
			CannyEdgesDetectorPreferences cannyEdgesDetectorPreferences,
			RomyJonaEdgesDetectorPreferences romyJonaEdgesDetectorPreferences) {
		this.edgesDetectorPreferences = edgesDetectorPreferences;
		this.cannyEdgesDetectorPreferences = cannyEdgesDetectorPreferences;
		this.romyJonaEdgesDetectorPreferences = romyJonaEdgesDetectorPreferences;
	}

	@Override
	public EdgesDetector getEdgesDetector() {
		switch (edgesDetectorPreferences.getEdgesDetectorFlavour()) {
		case CANNY_EDGES_DETECTOR:
			return new CannyEdgesDetectorImpl(cannyEdgesDetectorPreferences);
		case ROMY_JONA_EDGES_DETECTOR:
			return new RomyJonaEdgesDetectorImpl(romyJonaEdgesDetectorPreferences);
		default:
			throw new IllegalArgumentException();
		}
	}

}
