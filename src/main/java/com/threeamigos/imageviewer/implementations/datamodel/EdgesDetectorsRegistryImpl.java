package com.threeamigos.imageviewer.implementations.datamodel;

import java.awt.Component;
import java.util.EnumMap;
import java.util.Map;

import com.threeamigos.imageviewer.interfaces.datamodel.EdgesDetector;
import com.threeamigos.imageviewer.interfaces.datamodel.EdgesDetectorFactory;
import com.threeamigos.imageviewer.interfaces.datamodel.EdgesDetectorRegistry;
import com.threeamigos.imageviewer.interfaces.persister.Persistable;
import com.threeamigos.imageviewer.interfaces.preferences.EdgesDetectorFlavour;
import com.threeamigos.imageviewer.interfaces.ui.EdgesDetectorPreferencesSelector;
import com.threeamigos.imageviewer.interfaces.ui.EdgesDetectorPreferencesSelectorFactory;

public class EdgesDetectorsRegistryImpl implements EdgesDetectorRegistry {

	private final Map<EdgesDetectorFlavour, Persistable> mapToPreferences = new EnumMap<>(EdgesDetectorFlavour.class);
	private final Map<EdgesDetectorFlavour, EdgesDetectorPreferencesSelectorFactory> mapToPreferencesSelectorFactory = new EnumMap<>(
			EdgesDetectorFlavour.class);
	private final Map<EdgesDetectorFlavour, EdgesDetectorFactory> mapToDetectorFactory = new EnumMap<>(
			EdgesDetectorFlavour.class);

	@Override
	public void register(EdgesDetectorFlavour flavour, Persistable preferences,
			EdgesDetectorPreferencesSelectorFactory preferencesSelectorFactory,
			EdgesDetectorFactory edgesDetectorFactory) {
		mapToPreferences.put(flavour, preferences);
		mapToPreferencesSelectorFactory.put(flavour, preferencesSelectorFactory);
		mapToDetectorFactory.put(flavour, edgesDetectorFactory);
	}

	@Override
	public Persistable getPreferences(EdgesDetectorFlavour flavour) {
		return mapToPreferences.get(flavour);
	}

	@Override
	public EdgesDetectorPreferencesSelector getPreferencesSelector(EdgesDetectorFlavour flavour, Component component) {
		return mapToPreferencesSelectorFactory.get(flavour).createSelector(component);
	}

	@Override
	public EdgesDetector getDetector(EdgesDetectorFlavour flavour) {
		return mapToDetectorFactory.get(flavour).getEdgesDetector();
	}

}
