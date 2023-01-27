package com.threeamigos.imageviewer.interfaces.datamodel;

import java.awt.Graphics2D;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Collection;

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

	public void loadFiles(Collection<File> files);

	public boolean hasLoadedImages();

	// Communication part

	public void addPropertyChangeListener(PropertyChangeListener listener);

	public void removePropertyChangeListener(PropertyChangeListener listener);

	public InputConsumer getInputConsumer();

}
