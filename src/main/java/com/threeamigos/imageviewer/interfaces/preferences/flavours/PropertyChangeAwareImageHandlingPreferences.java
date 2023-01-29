package com.threeamigos.imageviewer.interfaces.preferences.flavours;

import java.beans.PropertyChangeListener;

public interface PropertyChangeAwareImageHandlingPreferences extends ImageHandlingPreferences {

	public void addPropertyChangeListener(PropertyChangeListener pcl);

	public void removePropertyChangeListener(PropertyChangeListener pcl);

}
