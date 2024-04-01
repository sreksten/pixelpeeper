package com.threeamigos.pixelpeeper.interfaces.ui;

import com.threeamigos.common.util.interfaces.PropertyChangeAware;
import com.threeamigos.common.util.interfaces.ui.HintsProducer;
import com.threeamigos.common.util.interfaces.ui.InputConsumer;

import java.awt.*;
import java.beans.PropertyChangeListener;

public interface CursorManager extends PropertyChangeAware, PropertyChangeListener, HintsProducer<String> {

    int getMaxCursorSize();

    Cursor getCursor();

    InputConsumer getInputConsumer();

}
