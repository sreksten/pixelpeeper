package com.threeamigos.imageviewer.interfaces.datamodel;

import java.awt.Component;

import com.threeamigos.imageviewer.interfaces.persister.Persistable;
import com.threeamigos.imageviewer.interfaces.preferences.EdgesDetectorFlavour;
import com.threeamigos.imageviewer.interfaces.ui.EdgesDetectorPreferencesSelector;
import com.threeamigos.imageviewer.interfaces.ui.EdgesDetectorPreferencesSelectorFactory;

public interface EdgesDetectorRegistry {

	public void register(EdgesDetectorFlavour flavour, Persistable preferences,
			EdgesDetectorPreferencesSelectorFactory preferencesSelectorFactory,
			EdgesDetectorFactory edgesDetectorFactory);

	public Persistable getPreferences(EdgesDetectorFlavour flavour);

	public EdgesDetectorPreferencesSelector getPreferencesSelector(EdgesDetectorFlavour flavour, Component component);

	public EdgesDetector getDetector(EdgesDetectorFlavour flavour);

}
