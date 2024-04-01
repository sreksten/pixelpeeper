package com.threeamigos.pixelpeeper.implementations.ui;

import com.threeamigos.common.util.interfaces.ui.InputConsumer;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

/**
 * A class that implements a generic InputConsumer ignoring all events.
 * This adapter can be extended implementing only the needed methods.
 */
public class InputAdapter implements InputConsumer {

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        // Empty method
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        // Empty method
    }

    @Override
    public void mousePressed(MouseEvent e) {
        // Empty method
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        // Empty method
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        // Empty method
    }

    @Override
    public void mouseExited(MouseEvent e) {
        // Empty method
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        // Empty method
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        // Empty method
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // Empty method
    }

    @Override
    public void keyPressed(KeyEvent e) {
        // Empty method
    }

    @Override
    public void keyReleased(KeyEvent e) {
        // Empty method
    }

}
