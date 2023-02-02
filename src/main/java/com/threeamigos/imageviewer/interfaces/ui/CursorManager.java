package com.threeamigos.imageviewer.interfaces.ui;

import java.awt.Cursor;
import java.beans.PropertyChangeListener;

import com.threeamigos.imageviewer.interfaces.preferences.PropertyChangeAware;

public interface CursorManager extends PropertyChangeAware, PropertyChangeListener, HintsProducer {

	public int getMaxCursorSize();

	public Cursor getCursor();

	public InputConsumer getInputConsumer();

}
