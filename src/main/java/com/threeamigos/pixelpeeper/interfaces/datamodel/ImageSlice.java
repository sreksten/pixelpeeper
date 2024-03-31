package com.threeamigos.pixelpeeper.interfaces.datamodel;

import com.threeamigos.pixelpeeper.data.PictureData;

import java.awt.*;
import java.beans.PropertyChangeListener;

/**
 * A portion of the screen that depicts part of an image
 *
 * @author Stefano Reksten
 */
public interface ImageSlice extends PropertyChangeListener {

    /**
     * The onscreen location of this slice
     */
    void setLocation(Rectangle location);

    /**
     * The onscreen location of this slice
     */
    Rectangle getLocation();

    /**
     * Used to unserstand if the mouse is hovering over this slice
     *
     * @param x mouse coordinate
     * @param y mouse coordinate
     * @return true if the mouse is over this slice
     */
    boolean contains(int x, int y);

    /**
     * Used when the user is dragging the mouse, to keep track of the slice where
     * the mouse was clicked
     */
    void setSelected(boolean selected);

    /**
     * The image along with the Exif tags
     */
    PictureData getPictureData();

    /**
     * Move the image around the slice
     *
     * @param deltaX pixels to shift the upper X coordinate of the viewable part of
     *               the picture
     * @param deltaY pixels to shift the upper X coordinate of the viewable part of
     *               the picture
     */
    void move(double deltaX, double deltaY);

    void startDrawing();

    void addVertex(int x, int y);

    void stopDrawing();

    void undoLastDrawing();

    void clearDrawings();

    /**
     * To clear the image shifting when loading a new image. Image is centered on
     * the screen.
     */
    void resetMovement();

    /**
     * To adjust for zoom level
     */
    void changeZoomLevel(float zoomLevel);

    float getZoomLevel();

    void paint(Graphics2D g2d);

    /**
     * Rotates the image if needed
     */
    void adjustRotation(boolean autorotation);

    /**
     * Asks to recalculate the edge images
     */
    void startEdgesCalculation();

    /**
     * Asks to clear the edges image and release the memory
     */
    void releaseEdges();

    // Communication part

    void addPropertyChangeListener(PropertyChangeListener listener);

    void removePropertyChangeListener(PropertyChangeListener listener);

}
