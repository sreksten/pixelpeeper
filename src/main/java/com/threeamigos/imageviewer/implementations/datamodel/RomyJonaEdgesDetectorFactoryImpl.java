package com.threeamigos.imageviewer.implementations.datamodel;

import com.threeamigos.imageviewer.interfaces.datamodel.RomyJonaEdgesDetector;
import com.threeamigos.imageviewer.interfaces.datamodel.RomyJonaEdgesDetectorFactory;
import com.threeamigos.imageviewer.interfaces.preferences.RomyJonaEdgesDetectorPreferences;

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
