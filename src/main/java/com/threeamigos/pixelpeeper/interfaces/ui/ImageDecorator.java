package com.threeamigos.pixelpeeper.interfaces.ui;

import java.awt.*;

/**
 * An interface that given a Graphics2D, decorates it by drawing additional objects atop. It may be
 * a miniature showing which part of the original image we are seeing at the moment, some doodles, etc.
 *
 * @author Stefano Reksten
 */
public interface ImageDecorator {

    void paint(Graphics2D graphics);

}
