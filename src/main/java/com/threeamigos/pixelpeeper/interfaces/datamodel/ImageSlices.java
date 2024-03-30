package com.threeamigos.pixelpeeper.interfaces.datamodel;

import com.threeamigos.pixelpeeper.data.PictureData;

import java.awt.*;
import java.beans.PropertyChangeListener;

/**
 * Tracks the image slices we see on screen
 *
 * @author Stefano Reksten
 */
public interface ImageSlices extends PropertyChangeListener {

    /**
     * Prepares to load new images
     */
    void clear();

    /**
     * Adds a new image
     *
     * @param pictureData a picture to be tracked
     */
    void add(PictureData pictureData);

    void sort();

    boolean isNotEmpty();

    /**
     * To be used when the main window is resized
     *
     * @param panelWidth
     * @param panelHeight
     */
    void reframe(int panelWidth, int panelHeight);

    void updateZoomLevel();

    void move(int deltaX, int deltaY, boolean allImages);

    void resetMovement();

    void setActiveSlice(int x, int y);

    void setNoActiveSlice();

    void startAnnotating();

    void addPoint(int x, int y);

    void stopAnnotating();

    void undoLastAnnotation();

    void clearAnnotations();

    /**
     * Asks all slices to recalculate edge images
     */
    void calculateEdges();

    void releaseEdges();

    void toggleAutorotation();

    void paint(Graphics2D graphics);

    // Communication part

    void addPropertyChangeListener(PropertyChangeListener listener);

    void removePropertyChangeListener(PropertyChangeListener listener);

}
