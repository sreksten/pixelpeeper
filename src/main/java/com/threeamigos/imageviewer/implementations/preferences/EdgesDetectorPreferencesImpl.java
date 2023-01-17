package com.threeamigos.imageviewer.implementations.preferences;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import com.threeamigos.common.util.interfaces.ErrorMessageHandler;
import com.threeamigos.imageviewer.implementations.ui.AbstractPreferencesImpl;
import com.threeamigos.imageviewer.interfaces.datamodel.CommunicationMessages;
import com.threeamigos.imageviewer.interfaces.persister.Persister;
import com.threeamigos.imageviewer.interfaces.preferences.EdgesDetectorFlavour;
import com.threeamigos.imageviewer.interfaces.preferences.EdgesDetectorPreferences;
import com.threeamigos.imageviewer.interfaces.preferences.PropertyChangeAwareEdgesDetectorPreferences;

public class EdgesDetectorPreferencesImpl extends AbstractPreferencesImpl<EdgesDetectorPreferences>
		implements PropertyChangeAwareEdgesDetectorPreferences {

	private boolean showEdgesAtStart;
	private int edgesTransparencyAtStart;
	private EdgesDetectorFlavour flavourAtStart;

	private boolean showEdges;
	private int edgesTransparency;
	private EdgesDetectorFlavour flavour;

	private final PropertyChangeSupport propertyChangeSupport;

	@Override
	protected String getEntityDescription() {
		return "edges detector";
	}

	public EdgesDetectorPreferencesImpl(Persister<EdgesDetectorPreferences> persister,
			ErrorMessageHandler errorMessageHandler) {
		super(persister, errorMessageHandler);

		propertyChangeSupport = new PropertyChangeSupport(this);

		loadPostConstruct();
		copyPreferencesAtStart();
	}

	@Override
	public void setShowEdges(boolean showEdges) {
		this.showEdges = showEdges;
		propertyChangeSupport.firePropertyChange(CommunicationMessages.EDGES_VISIBILITY, !showEdges, showEdges);
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

	private void copyPreferencesAtStart() {
		showEdgesAtStart = showEdges;
		edgesTransparencyAtStart = edgesTransparency;
		flavourAtStart = flavour;
	}

	@Override
	public boolean hasChanged() {
		return showEdges != showEdgesAtStart || edgesTransparency != edgesTransparencyAtStart
				|| flavour != flavourAtStart;
	}

	@Override
	public void addPropertyChangeListener(PropertyChangeListener pcl) {
		propertyChangeSupport.addPropertyChangeListener(pcl);
	}

	@Override
	public void removePropertyChangeListener(PropertyChangeListener pcl) {
		propertyChangeSupport.removePropertyChangeListener(pcl);
	}

}
