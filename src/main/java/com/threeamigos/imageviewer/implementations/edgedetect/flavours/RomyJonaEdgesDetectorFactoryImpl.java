package com.threeamigos.imageviewer.implementations.edgedetect.flavours;

import com.threeamigos.imageviewer.interfaces.edgedetect.flavours.RomyJonaEdgesDetector;
import com.threeamigos.imageviewer.interfaces.edgedetect.flavours.RomyJonaEdgesDetectorFactory;
import com.threeamigos.imageviewer.interfaces.preferences.flavours.RomyJonaEdgesDetectorPreferences;

public class RomyJonaEdgesDetectorFactoryImpl implements RomyJonaEdgesDetectorFactory {

	private final RomyJonaEdgesDetectorPreferences romyJonaEdgesDetectorPreferences;

	public RomyJonaEdgesDetectorFactoryImpl(RomyJonaEdgesDetectorPreferences romyJonaEdgesDetectorPreferences) {
		this.romyJonaEdgesDetectorPreferences = romyJonaEdgesDetectorPreferences;
	}

	@Override
	public RomyJonaEdgesDetector getRomyJonaEdgesDetector() {
		return new RomyJonaEdgesDetectorImpl(romyJonaEdgesDetectorPreferences);
	}

}
