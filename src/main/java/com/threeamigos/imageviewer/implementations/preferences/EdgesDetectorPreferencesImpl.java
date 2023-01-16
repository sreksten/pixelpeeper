package com.threeamigos.imageviewer.implementations.preferences;

import com.threeamigos.common.util.interfaces.ErrorMessageHandler;
import com.threeamigos.imageviewer.implementations.ui.AbstractPreferencesImpl;
import com.threeamigos.imageviewer.interfaces.persister.Persister;
import com.threeamigos.imageviewer.interfaces.preferences.EdgesDetectorFlavour;
import com.threeamigos.imageviewer.interfaces.preferences.EdgesDetectorPreferences;

public class EdgesDetectorPreferencesImpl extends AbstractPreferencesImpl<EdgesDetectorPreferences>
		implements EdgesDetectorPreferences {

	private boolean showEdges;
	private int edgesTransparency;
	private EdgesDetectorFlavour flavour;

	@Override
	protected String getEntityDescription() {
		return "edges detector";
	}

	public EdgesDetectorPreferencesImpl(Persister<EdgesDetectorPreferences> persister,
			ErrorMessageHandler errorMessageHandler) {
		super(persister, errorMessageHandler);

		loadPostConstruct();
	}

	@Override
	public void setShowEdges(boolean showEdges) {
		this.showEdges = showEdges;
	}

	@Override
	public boolean isShowEdges() {
		return showEdges;
	}

	@Override
	public void setEdgesTransparency(int edgesTransparency) {
		this.edgesTransparency = edgesTransparency;
	}

	@Override
	public int getEdgesTransparency() {
		return edgesTransparency;
	}

	@Override
	public void setEdgesDetectorFlavour(EdgesDetectorFlavour flavour) {
		this.flavour = flavour;
	}

	@Override
	public EdgesDetectorFlavour getEdgesDetectorFlavour() {
		return flavour;
	}

	@Override
	protected void loadDefaultValues() {
		showEdges = EdgesDetectorPreferences.SHOW_EDGES_DEFAULT;
		edgesTransparency = EdgesDetectorPreferences.EDGES_TRANSPARENCY_DEFAULT;
		flavour = EdgesDetectorPreferences.EDGES_DETECTOR_FLAVOUR_DEFAULT;
	}

}
