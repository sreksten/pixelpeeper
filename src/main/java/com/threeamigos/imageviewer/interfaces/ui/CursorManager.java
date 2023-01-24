package com.threeamigos.imageviewer.interfaces.ui;

import java.awt.Cursor;
import java.beans.PropertyChangeListener;

public interface CursorManager extends PropertyChangeListener {

	public InputConsumer getInputConsumer();

	public int getMaxCursorSize();

	public Cursor getCursor();

	public void addPropertyChangeListener(PropertyChangeListener pcl);

	public void removePropertyChangeListener(PropertyChangeListener pcl);

}
