package com.threeamigos.pixelpeeper.interfaces.ui;

import java.awt.Cursor;
import java.beans.PropertyChangeListener;

import com.threeamigos.common.util.interfaces.PropertyChangeAware;

public interface CursorManager extends PropertyChangeAware, PropertyChangeListener, HintsProducer {

	public int getMaxCursorSize();

	public Cursor getCursor();

	public InputConsumer getInputConsumer();

}
