package com.threeamigos.pixelpeeper.interfaces.datamodel;

import com.threeamigos.pixelpeeper.data.PictureData;
import com.threeamigos.pixelpeeper.implementations.datamodel.DoodlesPersistenceService;

import java.awt.*;
import java.util.Collection;

/**
 * Tracks the image slices we see on screen
 *
 * @author Stefano Reksten
 */
public interface ImageSlices {

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

    /**
     * Returns all loaded images
     */
    Collection<PictureData> getLoadedImages();

    void sort();

    boolean isNotEmpty();

    /**
     * To be used when the main window is resized
     */
    void reframe(int panelWidth, int panelHeight);

    void updateZoomLevel();

    void move(int deltaX, int deltaY, boolean allImages);

    void resetMovement();

    void setActiveSlice(int x, int y);

    void setNoActiveSlice();

    void startDoodling();

    void addVertex(int x, int y);

    void stopDoodling();

    void undoLastDoodle();

    void clearDoodles();

    /**
     * For each loaded image: if it has doodles, saves them to a sidecar file;
     * if it has no doodles but a sidecar exists, deletes it.
     */
    void persistDoodles(DoodlesPersistenceService persistenceService);

    /**
     * For the most recently added image slice, attempts to load its doodles
     * from the sidecar file if one exists.
     */
    void loadDoodlesForLastSlice(DoodlesPersistenceService persistenceService);

    /**
     * Asks all slices to recalculate filter
     */
    void startFilterCalculation();

    void releaseFilter();

    void toggleAutorotation();

    void paint(Graphics2D graphics);

}
