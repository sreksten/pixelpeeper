package com.threeamigos.pixelpeeper.implementations.preferences.flavours;

import com.threeamigos.common.util.implementations.BasicPropertyChangeAware;
import com.threeamigos.pixelpeeper.interfaces.datamodel.CommunicationMessages;
import com.threeamigos.pixelpeeper.interfaces.edgedetect.EdgesDetectorFlavour;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavours.EdgesDetectorPreferences;

public class EdgesDetectorPreferencesImpl extends BasicPropertyChangeAware implements EdgesDetectorPreferences {

	private boolean showEdges;
	private int edgesTransparency;
	private EdgesDetectorFlavour edgesDetectorFlavour;

	@Override
	public void setShowEdges(boolean showEdges) {
		boolean oldShowEdges = this.showEdges;
		this.showEdges = showEdges;
		firePropertyChange(CommunicationMessages.EDGES_VISIBILITY_CHANGED, oldShowEdges, showEdges);
	}

	@Override
	public boolean isShowEdges() {
		return showEdges;
	}

	@Override
	public void setEdgesTransparency(int edgesTransparency) {
		int oldEdgesTransparency = this.edgesTransparency;
		this.edgesTransparency = edgesTransparency;
		firePropertyChange(CommunicationMessages.EDGES_TRANSPARENCY_CHANGED, oldEdgesTransparency, edgesTransparency);
	}

	@Override
	public int getEdgesTransparency() {
		return edgesTransparency;
	}

	@Override
	public void setEdgesDetectorFlavour(EdgesDetectorFlavour edgesDetectorFlavour) {
		EdgesDetectorFlavour oldEdgesDetectorFlavour = this.edgesDetectorFlavour;
		this.edgesDetectorFlavour = edgesDetectorFlavour;
		firePropertyChange(CommunicationMessages.EDGES_DETECTOR_FLAVOUR_CHANGED, oldEdgesDetectorFlavour,
				edgesDetectorFlavour);
	}

	@Override
	public EdgesDetectorFlavour getEdgesDetectorFlavour() {
		return edgesDetectorFlavour;
	}

	@Override
	public void loadDefaultValues() {
		showEdges = EdgesDetectorPreferences.SHOW_EDGES_DEFAULT;
		edgesTransparency = EdgesDetectorPreferences.EDGES_TRANSPARENCY_DEFAULT;
		edgesDetectorFlavour = EdgesDetectorPreferences.EDGES_DETECTOR_FLAVOUR_DEFAULT;
	}

	@Override
	public void validate() {
		if (edgesTransparency < NO_EDGES_TRANSPARENCY || edgesTransparency > TOTAL_EDGES_TRANSPARENCY) {
			throw new IllegalArgumentException(String.format("Invalid edges transparency: %d", edgesTransparency));
		}
	}
}
