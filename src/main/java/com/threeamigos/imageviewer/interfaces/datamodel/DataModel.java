package com.threeamigos.imageviewer.interfaces.datamodel;

import java.awt.Graphics2D;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Collection;
import java.util.Optional;

import com.threeamigos.imageviewer.data.ExifTag;
import com.threeamigos.imageviewer.data.ExifValue;
import com.threeamigos.imageviewer.interfaces.ui.HintsProducer;
import com.threeamigos.imageviewer.interfaces.ui.InputConsumer;

public interface DataModel extends PropertyChangeListener, HintsProducer {

	// Preferences part

	public boolean isAutorotation();

	public void toggleAutorotation();

	public boolean isMovementAppliedToAllImages();

	public void toggleMovementAppliedToAllImages();

	public boolean isMovementAppliedToAllImagesTemporarilyInverted();

	public boolean isShowEdges();

	public void toggleShowingEdges();

	public void calculateEdges();

	// Graphics part

	public void reframe(int width, int height);

	public void repaint(Graphics2D graphics);

	public void requestRepaint();

	public void setMovementAppliedToAllImages(boolean movementAppliesToAllFrames);

	public void move(int deltaX, int deltaY);

	public void resetMovement();

	public void changeZoomLevel();

	public void setActiveSlice(int x, int y);

	public void resetActiveSlice();

	// Data part

	public void loadLastFiles();

	public void loadFiles(Collection<File> files);

	public void loadFiles(Collection<File> files, ExifTag tagToGroupBy, int preferredGroupIndex);

	public int getGroupsCount();

	public int getCurrentGroup();

	public Optional<ExifValue> getCurrentExifValue();

	public void moveToNextGroup();

	public void moveToPreviousGroup();

	public boolean hasLoadedImages();

	// Communication part

	public void addPropertyChangeListener(PropertyChangeListener listener);

	public void removePropertyChangeListener(PropertyChangeListener listener);

	public InputConsumer getInputConsumer();

}
