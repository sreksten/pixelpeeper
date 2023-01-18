package com.threeamigos.imageviewer.implementations.preferences.flavours;

import com.threeamigos.imageviewer.interfaces.edgedetect.EdgesDetectorFlavour;
import com.threeamigos.imageviewer.interfaces.persister.StatusTracker;
import com.threeamigos.imageviewer.interfaces.preferences.flavours.EdgesDetectorPreferences;

public class EdgesDetectorPreferencesStatusTracker implements StatusTracker<EdgesDetectorPreferences> {

	private boolean showEdgesAtStart;
	private int edgesTransparencyAtStart;
	private EdgesDetectorFlavour flavourAtStart;

	private final EdgesDetectorPreferences edgesDetectorPreferences;

	public EdgesDetectorPreferencesStatusTracker(EdgesDetectorPreferences edgesDetectorPreferences) {
		this.edgesDetectorPreferences = edgesDetectorPreferences;
	}

	@Override
	public void loadInitialValues() {
		showEdgesAtStart = edgesDetectorPreferences.isShowEdges();
		edgesTransparencyAtStart = edgesDetectorPreferences.getEdgesTransparency();
		flavourAtStart = edgesDetectorPreferences.getEdgesDetectorFlavour();
	}

	@Override
	public boolean hasChanged() {
		return edgesDetectorPreferences.isShowEdges() != showEdgesAtStart
				|| edgesDetectorPreferences.getEdgesTransparency() != edgesTransparencyAtStart
				|| edgesDetectorPreferences.getEdgesDetectorFlavour() != flavourAtStart;
	}

}
