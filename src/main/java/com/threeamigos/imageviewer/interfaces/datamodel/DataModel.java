package com.threeamigos.imageviewer.interfaces.datamodel;

import java.awt.Graphics2D;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.List;

public interface DataModel extends PropertyChangeListener {

	// Preferences part

	public boolean isAutorotation();

	public void toggleAutorotation();

	public boolean isMovementAppliedToAllImages();

	public void toggleMovementAppliedToAllImages();

	public boolean isMovementAppliedToAllImagesTemporarilyInverted();

	public void setMovementAppliedToAllImagesTemporarilyInverted(
			boolean isMovementAppliedToAllImagesTemporarilyInverted);

	public boolean isShowEdges();

	public void toggleShowingEdges();

	public void calculateEdges();

	// Graphics part

	public void reframe(int width, int height);

	public void repaint(Graphics2D graphics);

	public void setMovementAppliedToAllImages(boolean movementAppliesToAllFrames);

	public void move(int deltaX, int deltaY);

	public void resetMovement();

	public void setActiveSlice(int x, int y);

	public void resetActiveSlice();

	// Data part

	public void loadFiles(List<File> files);

	public void browseDirectory(File directory);

	public boolean hasLoadedImages();

	// Communication part

	public void addPropertyChangeListener(PropertyChangeListener listener);

	public void removePropertyChangeListener(PropertyChangeListener listener);

}
