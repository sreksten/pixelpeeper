package com.threeamigos.imageviewer.interfaces.preferences.flavours;

import java.beans.PropertyChangeListener;

public interface PropertyChangeAwareBigPointerPreferences extends BigPointerPreferences {

	public void addPropertyChangeListener(PropertyChangeListener pcl);

	public void removePropertyChangeListener(PropertyChangeListener pcl);

}