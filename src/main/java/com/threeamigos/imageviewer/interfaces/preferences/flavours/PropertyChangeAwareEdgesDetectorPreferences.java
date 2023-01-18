package com.threeamigos.imageviewer.interfaces.preferences.flavours;

import java.beans.PropertyChangeListener;

public interface PropertyChangeAwareEdgesDetectorPreferences extends EdgesDetectorPreferences {

	public void addPropertyChangeListener(PropertyChangeListener pcl);

	public void removePropertyChangeListener(PropertyChangeListener pcl);

}
