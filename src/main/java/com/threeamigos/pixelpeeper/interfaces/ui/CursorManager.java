package com.threeamigos.pixelpeeper.interfaces.ui;

import java.awt.Cursor;
import java.beans.PropertyChangeListener;

import com.threeamigos.pixelpeeper.interfaces.preferences.PropertyChangeAware;

public interface CursorManager extends PropertyChangeAware, PropertyChangeListener, HintsProducer {

	public int getMaxCursorSize();

	public Cursor getCursor();

	public InputConsumer getInputConsumer();

}