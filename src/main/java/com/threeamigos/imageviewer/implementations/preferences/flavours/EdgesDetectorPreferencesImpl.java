package com.threeamigos.imageviewer.implementations.preferences.flavours;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import com.threeamigos.imageviewer.interfaces.datamodel.CommunicationMessages;
import com.threeamigos.imageviewer.interfaces.edgedetect.EdgesDetectorFlavour;
import com.threeamigos.imageviewer.interfaces.preferences.flavours.EdgesDetectorPreferences;
import com.threeamigos.imageviewer.interfaces.preferences.flavours.PropertyChangeAwareEdgesDetectorPreferences;

public class EdgesDetectorPreferencesImpl implements PropertyChangeAwareEdgesDetectorPreferences {

	private boolean showEdges;
	private int edgesTransparency;
	private EdgesDetectorFlavour edgesDetectorFlavour;

	// transient to make Gson serializer ignore this
	private final transient PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

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
		this.edgesDetectorFlavour = flavour;
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
	public void addPropertyChangeListener(PropertyChangeListener pcl) {
		propertyChangeSupport.addPropertyChangeListener(pcl);
	}

	@Override
	public void removePropertyChangeListener(PropertyChangeListener pcl) {
		propertyChangeSupport.removePropertyChangeListener(pcl);
	}

	@Override
	public void validate() {
		// TODO Auto-generated method stub

	}

}
