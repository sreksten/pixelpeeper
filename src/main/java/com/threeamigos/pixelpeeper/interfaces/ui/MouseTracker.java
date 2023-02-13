package com.threeamigos.pixelpeeper.interfaces.ui;

import java.beans.PropertyChangeListener;

/**
 * Listens to mouse movements to update the images' shift
 *
 * @author Stefano Reksten
 *
 */
public interface MouseTracker {

	public InputConsumer getInputConsumer();

	public void addPropertyChangeListener(PropertyChangeListener pcl);

	public void removePropertyChangeListener(PropertyChangeListener pcl);

}
