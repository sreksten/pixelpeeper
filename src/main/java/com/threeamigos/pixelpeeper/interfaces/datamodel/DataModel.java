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

public interface DataModel extends PropertyChangeListener, PropertyChangeAware, HintsProducer<String> {

    // Preferences part

    boolean isAutorotation();

    void toggleAutorotation();

    boolean isMovementAppliedToAllImages();

    void toggleMovementAppliedToAllImages();

    boolean isMovementAppliedToAllImagesTemporarilyInverted();

    boolean isShowEdges();

    void toggleShowingEdges();

    void calculateEdges();

    // Graphics part

    void reframe(int width, int height);

    void repaint(Graphics2D graphics);

    void requestRepaint();

    void setMovementAppliedToAllImages(boolean movementAppliesToAllFrames);

    void move(int deltaX, int deltaY);

    void resetMovement();

    void changeZoomLevel();

    void setActiveSlice(int x, int y);

    void resetActiveSlice();

    // Data part

    void loadLastFiles();

    void loadFiles(Collection<File> files);

    void loadFiles(Collection<File> files, ExifTag tagToGroupBy, int tolerance, ExifTag tagToOrderBy,
                   int preferredGroupIndex);

    void browseDirectory(File directory, Component component);

    int getGroupsCount();

    int getCurrentGroup();

    Optional<ExifValue> getCurrentExifValue();

    void moveToNextGroup();

    void moveToPreviousGroup();

    boolean hasLoadedImages();

    // Communication part

    InputConsumer getInputConsumer();

}
