package com.threeamigos.imageviewer.implementations.ui;

import java.awt.Component;

import com.threeamigos.common.util.interfaces.MessageHandler;
import com.threeamigos.imageviewer.interfaces.datamodel.ExifImageReader;
import com.threeamigos.imageviewer.interfaces.preferences.CannyEdgeDetectorPreferences;
import com.threeamigos.imageviewer.interfaces.ui.CannyEdgeDetectorPreferencesSelector;
import com.threeamigos.imageviewer.interfaces.ui.CannyEdgeDetectorPreferencesSelectorFactory;

public class CannyEdgeDetectorPreferencesSelectorFactoryImpl implements CannyEdgeDetectorPreferencesSelectorFactory {

	private final CannyEdgeDetectorPreferences cannyEdgeDetectorPreferences;
	private final ExifImageReader exifImageReader;
	private final MessageHandler messageConsumer;

	public CannyEdgeDetectorPreferencesSelectorFactoryImpl(CannyEdgeDetectorPreferences cannyEdgeDetectorPreferences,
			ExifImageReader exifImageReader, MessageHandler messageConsumer) {
		this.cannyEdgeDetectorPreferences = cannyEdgeDetectorPreferences;
		this.exifImageReader = exifImageReader;
		this.messageConsumer = messageConsumer;
	}

	@Override
	public CannyEdgeDetectorPreferencesSelector createSelector(Component component) {
		return new CannyEdgeDetectorPreferencesSelectorImpl(cannyEdgeDetectorPreferences, exifImageReader, component,
				messageConsumer);
	}

}
