package com.threeamigos.pixelpeeper.implementations.edgedetect;

import com.threeamigos.pixelpeeper.implementations.edgedetect.flavours.CannyEdgesDetectorImpl;
import com.threeamigos.pixelpeeper.implementations.edgedetect.flavours.RomyJonaEdgesDetectorImpl;
import com.threeamigos.pixelpeeper.implementations.edgedetect.flavours.SobelEdgesDetectorImpl;
import com.threeamigos.pixelpeeper.interfaces.edgedetect.EdgesDetector;
import com.threeamigos.pixelpeeper.interfaces.edgedetect.EdgesDetectorFactory;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavours.CannyEdgesDetectorPreferences;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavours.EdgesDetectorPreferences;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavours.RomyJonaEdgesDetectorPreferences;

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
		case SOBEL_EDGES_DETECTOR:
			return new SobelEdgesDetectorImpl();
		default:
			throw new IllegalArgumentException();
		}
	}

}
