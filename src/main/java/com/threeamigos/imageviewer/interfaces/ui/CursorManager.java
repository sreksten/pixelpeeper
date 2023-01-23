package com.threeamigos.imageviewer.interfaces.ui;

import java.awt.Cursor;
import java.beans.PropertyChangeListener;

public interface CursorManager {

	public InputConsumer getInputConsumer();

	public Cursor getCursor();

	public void addPropertyChangeListener(PropertyChangeListener pcl);

	public void removePropertyChangeListener(PropertyChangeListener pcl);

}
