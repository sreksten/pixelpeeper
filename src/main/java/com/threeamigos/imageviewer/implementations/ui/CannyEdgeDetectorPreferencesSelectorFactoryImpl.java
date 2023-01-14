package com.threeamigos.imageviewer.implementations.ui;

import java.awt.Component;

import com.threeamigos.common.util.interfaces.ExceptionHandler;
import com.threeamigos.imageviewer.interfaces.datamodel.ExifImageReader;
import com.threeamigos.imageviewer.interfaces.preferences.CannyEdgeDetectorPreferences;
import com.threeamigos.imageviewer.interfaces.ui.CannyEdgeDetectorPreferencesSelector;
import com.threeamigos.imageviewer.interfaces.ui.CannyEdgeDetectorPreferencesSelectorFactory;

public class CannyEdgeDetectorPreferencesSelectorFactoryImpl implements CannyEdgeDetectorPreferencesSelectorFactory {

	private final CannyEdgeDetectorPreferences cannyEdgeDetectorPreferences;
	private final ExifImageReader exifImageReader;
	private final ExceptionHandler exceptionHandler;

	public CannyEdgeDetectorPreferencesSelectorFactoryImpl(CannyEdgeDetectorPreferences cannyEdgeDetectorPreferences,
			ExifImageReader exifImageReader, ExceptionHandler exceptionHandler) {
		this.cannyEdgeDetectorPreferences = cannyEdgeDetectorPreferences;
		this.exifImageReader = exifImageReader;
		this.exceptionHandler = exceptionHandler;
	}

	@Override
	public CannyEdgeDetectorPreferencesSelector createSelector(Component component) {
		return new CannyEdgeDetectorPreferencesSelectorImpl(cannyEdgeDetectorPreferences, exifImageReader, component,
				exceptionHandler);
	}

}
