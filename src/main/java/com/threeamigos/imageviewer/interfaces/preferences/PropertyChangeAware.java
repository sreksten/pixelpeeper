package com.threeamigos.imageviewer.interfaces.preferences;

import java.beans.PropertyChangeListener;

public interface PropertyChangeAware {

	public void addPropertyChangeListener(PropertyChangeListener pcl);

	public void removePropertyChangeListener(PropertyChangeListener pcl);

}
