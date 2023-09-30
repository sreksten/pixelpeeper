package com.threeamigos.pixelpeeper.implementations.edgedetect.ui;

import java.awt.Component;

import com.threeamigos.common.util.interfaces.messagehandler.ExceptionHandler;
import com.threeamigos.pixelpeeper.interfaces.datamodel.DataModel;
import com.threeamigos.pixelpeeper.interfaces.datamodel.ExifImageReader;
import com.threeamigos.pixelpeeper.interfaces.edgedetect.ui.EdgesDetectorPreferencesSelector;
import com.threeamigos.pixelpeeper.interfaces.edgedetect.ui.EdgesDetectorPreferencesSelectorFactory;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavours.CannyEdgesDetectorPreferences;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavours.EdgesDetectorPreferences;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavours.RomyJonaEdgesDetectorPreferences;

public class EdgesDetectorPreferencesSelectorFactoryImpl implements EdgesDetectorPreferencesSelectorFactory {

	private final EdgesDetectorPreferences edgesDetectorPreferences;
	private final CannyEdgesDetectorPreferences cannyEdgesDetectorPreferences;
	private final RomyJonaEdgesDetectorPreferences romyJonaEdgesDetectorPreferences;
	private final DataModel dataModel;
	private final ExifImageReader exifImageReader;
	private final ExceptionHandler exceptionHandler;

	public EdgesDetectorPreferencesSelectorFactoryImpl(EdgesDetectorPreferences edgesDetectorPreferences,
			CannyEdgesDetectorPreferences cannyEdgesDetectorPreferences,
			RomyJonaEdgesDetectorPreferences romyJonaEdgesDetectorPreferences, DataModel dataModel,
			ExifImageReader exifImageReader, ExceptionHandler exceptionHandler) {
		this.edgesDetectorPreferences = edgesDetectorPreferences;
		this.cannyEdgesDetectorPreferences = cannyEdgesDetectorPreferences;
		this.romyJonaEdgesDetectorPreferences = romyJonaEdgesDetectorPreferences;
		this.dataModel = dataModel;
		this.exifImageReader = exifImageReader;
		this.exceptionHandler = exceptionHandler;
	}

	@Override
	public EdgesDetectorPreferencesSelector createSelector(Component component) {
		switch (edgesDetectorPreferences.getEdgesDetectorFlavour()) {
		case CANNY_EDGES_DETECTOR:
			return new CannyEdgesDetectorPreferencesSelectorImpl(edgesDetectorPreferences,
					cannyEdgesDetectorPreferences, dataModel, exifImageReader, component, exceptionHandler);
		case ROMY_JONA_EDGES_DETECTOR:
			return new RomyJonaEdgesDetectorPreferencesSelectorImpl(edgesDetectorPreferences,
					romyJonaEdgesDetectorPreferences, dataModel, exifImageReader, component, exceptionHandler);
		case SOBEL_EDGES_DETECTOR:
			return new SobelEdgesDetectorPreferencesSelectorImpl(edgesDetectorPreferences, dataModel, exifImageReader,
					component, exceptionHandler);
		default:
			throw new IllegalArgumentException();
		}
	}

}
