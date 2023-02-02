package com.threeamigos.imageviewer.implementations.preferences.flavours;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import com.threeamigos.imageviewer.interfaces.datamodel.CommunicationMessages;
import com.threeamigos.imageviewer.interfaces.preferences.flavours.PropertyChangeAwareGridPreferences;

public class GridPreferencesImpl implements PropertyChangeAwareGridPreferences {

	private boolean gridVisible;
	private int gridSpacing;

	// transient to make Gson serializer ignore this
	private final transient PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

	@Override
	public void setGridVisible(boolean gridVisible) {
		this.gridVisible = gridVisible;
		propertyChangeSupport.firePropertyChange(CommunicationMessages.GRID_VISIBILITY_CHANGE, null, null);
	}

	@Override
	public boolean isGridVisible() {
		return gridVisible;
	}

	@Override
	public void setGridSpacing(int gridSpacing) {
		this.gridSpacing = gridSpacing;
		propertyChangeSupport.firePropertyChange(CommunicationMessages.GRID_SIZE_CHANGED, null, null);
	}

	@Override
	public int getGridSpacing() {
		return gridSpacing;
	}

	@Override
	public void loadDefaultValues() {
		gridVisible = GRID_VISIBLE_DEFAULT;
		gridSpacing = GRID_SPACING_DEFAULT;
	}

	@Override
	public void validate() {
		if (gridSpacing < GRID_SPACING_MIN || gridSpacing > GRID_SPACING_MAX) {
			throw new IllegalArgumentException("Invalid grid spacing");
		}
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
