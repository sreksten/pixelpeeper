package com.threeamigos.pixelpeeper.interfaces.ui;

import java.awt.Cursor;
import java.beans.PropertyChangeListener;

import com.threeamigos.common.util.interfaces.PropertyChangeAware;
import com.threeamigos.common.util.interfaces.ui.HintsProducer;
import com.threeamigos.common.util.interfaces.ui.InputConsumer;

public interface CursorManager extends PropertyChangeAware, PropertyChangeListener, HintsProducer<String> {

	public int getMaxCursorSize();

	public Cursor getCursor();

	public InputConsumer getInputConsumer();

}
