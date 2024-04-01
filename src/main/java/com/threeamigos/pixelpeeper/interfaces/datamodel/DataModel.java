package com.threeamigos.pixelpeeper.interfaces.datamodel;

import com.threeamigos.common.util.interfaces.PropertyChangeAware;
import com.threeamigos.common.util.interfaces.ui.HintsProducer;
import com.threeamigos.common.util.interfaces.ui.InputConsumer;
import com.threeamigos.pixelpeeper.data.ExifTag;
import com.threeamigos.pixelpeeper.data.ExifValue;

import java.awt.*;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Collection;
import java.util.Optional;

/**
 * The whole data model of the application.
 *
 * @author Stefano Reksten
 */

//TODO check if is it safe to remove all those methods
//TODO document all this thing
public interface DataModel extends PropertyChangeListener, PropertyChangeAware, HintsProducer<String> {

    // Preferences part

    boolean isAutorotation();

    /**
     * Sets or unsets the autorotation feature. If this is active, when an image is loaded that has not
     * a correct orientation, it will be translated to an equivalent image that can be seen without the
     * end user being force to tilt his/her head.
     */
    void toggleAutorotation();

    boolean isMovementAppliedToAllImages();

    void toggleMovementAppliedToAllImages();

    boolean isMovementAppliedToAllImagesTemporarilyInverted();

    boolean isShowEdges();

    void toggleShowingEdges();

    /**
     * Starts the edge-detection algorithm
     */
    void calculateEdges();

    // Graphics part

    /**
     * When the window is resized, this will recalculate
     * the visible part for loaded images.
     */
    void reframe(int width, int height);

    /**
     * Paints all objects on screen (images, decorators, tags, ...).
     *
     * @param graphics
     */
    void repaint(Graphics2D graphics);

    /**
     * Asks the UI to repaint itself.
     */
    void requestRepaint();

    void setMovementAppliedToAllImages(boolean movementAppliesToAllFrames);

    /**
     * If the image is bigger than the screen, this function shifts it around.
     */
    void move(int deltaX, int deltaY);

    /**
     * Resets the movement and centers all images.
     */
    void resetMovement();

    /**
     * Changes zoom level for all images.
     */
    void changeZoomLevel();

    /**
     * When the end user clicks on the screen, the image placed under the
     * cursor is considered the active one, for resizing, moving or doodling
     * purposes.
     */
    void setActiveSlice(int x, int y);

    /**
     * Clears the reference to the active slice.
     */
    void resetActiveSlice();

    // Data part

    /**
     * Loads the files that were used last during the previous session.
     */
    void loadLastFiles();

    /**
     * Loads some user selected files.
     */
    void loadFiles(Collection<File> files);

    /**
     * Loads some files and groups them. The end user can then browse through these groups via the UI.
     *
     * @param files               files to be loaded
     * @param tagToGroupBy        one EXIF tag used to group files (e.g. camera manufacturer, lens model).
     * @param tolerance           a value used to group pictures by focal length. As the focal length, in case of
     *                            zoom lenses, can't be extremely precise, we accept a tolerance (e.g. pictures
     *                            with a focal length of 34, 35 and 37mm can end in the same group while pictures
     *                            with a focal length of 49, 50 and 51mm can end in a second group.
     * @param tagToOrderBy        a tag used to specify the ordering of grouped images.
     * @param preferredGroupIndex index of the first group that will be used when showing grouped images.
     */
    void loadFiles(Collection<File> files, ExifTag tagToGroupBy, int tolerance, ExifTag tagToOrderBy,
                   int preferredGroupIndex);

    /**
     * Given a directory, the application browse all files searching for images.
     */
    void browseDirectory(File directory, Component component);

    /**
     * If images were grouped according to a tag, this returns he total number of groups that
     * all pictures were grouped in.
     */
    int getGroupsCount();

    /**
     * Returns the index of the current group of pictures
     */
    int getCurrentGroup();

    /**
     * When grouping pictures by a specific EXIF tag, this function
     * returns what the current value of that tag is.
     */
    Optional<ExifValue> getCurrentExifValue();

    /**
     * When grouping pictures by a specific EXIF tag,
     * moves to the next group of pictures.
     */
    void moveToNextGroup();

    /**
     * When grouping pictures by a specific EXIF tag,
     * moves to the previous group of pictures.
     */
    void moveToPreviousGroup();

    /**
     * Tells us if the application has loaded any images yet.
     */
    boolean hasLoadedImages();

    // Communication part

    /**
     * Returns the InputConsumer for the DataModel only.
     * This is used in order to set up a proper chain of input consumers, so that
     * consumers with high priority receive the user inputs first (and maybe consume them
     * so that lower-priority consumers won't even receive them).
     */
    InputConsumer getInputConsumer();

}
