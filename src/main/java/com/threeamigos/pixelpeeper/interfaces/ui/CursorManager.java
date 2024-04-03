package com.threeamigos.pixelpeeper.interfaces.ui;

import com.threeamigos.common.util.interfaces.PropertyChangeAware;
import com.threeamigos.common.util.interfaces.ui.HintsProducer;
import com.threeamigos.common.util.interfaces.ui.InputConsumer;

import java.awt.*;
import java.beans.PropertyChangeListener;

/**
 * An interface that controls the cursor appearance. It extends the following interfaces:
 * <ul>
 *     <li>{@link PropertyChangeAware} to inform other components (e.g. any component that needs to be repainted
 *     after the end-user asked to change some of the cursor's characteristics)</li>
 *     <li>{@link PropertyChangeListener} to receive notifications about changed preferences (e.g. after the
 *     end-user selected a particular size for the cursor) </li>
 *     <li>{@link HintsProducer} to provide the end user with some hints on how to change the cursor's aspect
 *     or what the various cursor images mean)</li>
 * </ul>
 * A general behaviour may be the UI registering itself to receive notifications about the cursor variations,
 * and the CursorManager registering itself to some preferences about the cursor appearance.
 *
 * @author Stefano Reksten
 */
public interface CursorManager extends PropertyChangeAware, PropertyChangeListener, HintsProducer<String> {

    /**
     * Returns max cursor size in pixels
     */
    int getMaxCursorSize();

    /**
     * Returns the current cursor (depending on what the program is doing at the moment)
     */
    Cursor getCursor();

    /**
     * Returns the {@link InputConsumer} associated with this instance, in order to receive and manage UI events
     */
    InputConsumer getInputConsumer();

}
