package com.threeamigos.pixelpeeper.interfaces.datamodel;

import java.awt.Graphics2D;
import java.beans.PropertyChangeListener;

import com.threeamigos.pixelpeeper.data.PictureData;

/**
 * Tracks the image slices we see on screen
 *
 * @author Stefano Reksten
 *
 */
public interface ImageSlices extends PropertyChangeListener {

	/**
	 * Prepares to load new images
	 */
	public void clear();

	/**
	 * Adds a new image
	 * 
	 * @param pictureData a picture to be tracked
	 */
	public void add(PictureData pictureData);

	public boolean isNotEmpty();

	/**
	 * To be used when the main window is resized
	 *
	 * @param panelWidth
	 * @param panelHeight
	 */
	public void reframe(int panelWidth, int panelHeight);

	public void updateZoomLevel();

	public void move(int deltaX, int deltaY, boolean allImages);

	public void resetMovement();

	public void setActiveSlice(int x, int y);

	public void setNoActiveSlice();

	public void startAnnotating();

	public void addPoint(int x, int y);

	public void stopAnnotating();

	public void undoLastAnnotation();

	public void clearAnnotations();

	/**
	 * Asks all slices to recalculate edge images
	 */
	public void calculateEdges();

	public void releaseEdges();

	public void toggleAutorotation();

	public void paint(Graphics2D graphics);

	// Communication part

	public void addPropertyChangeListener(PropertyChangeListener listener);

	public void removePropertyChangeListener(PropertyChangeListener listener);

}
