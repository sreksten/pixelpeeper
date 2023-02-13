package com.threeamigos.pixelpeeper.interfaces.ui;

import java.awt.event.KeyListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelListener;

import javax.swing.event.MouseInputListener;

/**
 * An object that consumes user inputs.
 *
 * @author Stefano Reksten
 *
 */
public interface InputConsumer extends MouseWheelListener, MouseInputListener, MouseMotionListener, KeyListener {

}
