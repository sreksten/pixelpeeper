package com.threeamigos.imageviewer.implementations.ui;

import java.awt.Component;

import com.threeamigos.common.util.interfaces.MessageConsumer;
import com.threeamigos.imageviewer.interfaces.preferences.CannyEdgeDetectorPreferences;
import com.threeamigos.imageviewer.interfaces.preferences.WindowPreferences;
import com.threeamigos.imageviewer.interfaces.ui.CannyEdgeDetectorPreferencesSelector;
import com.threeamigos.imageviewer.interfaces.ui.CannyEdgeDetectorPreferencesSelectorFactory;

public class CannyEdgeDetectorPreferencesSelectorFactoryImpl implements CannyEdgeDetectorPreferencesSelectorFactory {

	private final WindowPreferences windowPreferences;
	private final CannyEdgeDetectorPreferences cannyEdgeDetectorPreferences;
	private final MessageConsumer messageConsumer;

	public CannyEdgeDetectorPreferencesSelectorFactoryImpl(WindowPreferences windowPreferences,
			CannyEdgeDetectorPreferences cannyEdgeDetectorPreferences, MessageConsumer messageConsumer) {
		this.windowPreferences = windowPreferences;
		this.cannyEdgeDetectorPreferences = cannyEdgeDetectorPreferences;
		this.messageConsumer = messageConsumer;
	}

	@Override
	public CannyEdgeDetectorPreferencesSelector createSelector(Component component) {
		return new CannyEdgeDetectorPreferencesSelectorImpl(windowPreferences, cannyEdgeDetectorPreferences, component,
				messageConsumer);
	}

}
