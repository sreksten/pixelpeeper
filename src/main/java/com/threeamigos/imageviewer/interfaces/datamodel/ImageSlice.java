package com.threeamigos.imageviewer.interfaces.datamodel;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.beans.PropertyChangeListener;

import com.threeamigos.imageviewer.data.PictureData;

/**
 * A portion of the screen that depicts part of an image
 *
 * @author Stefano Reksten
 *
 */
public interface ImageSlice {

	/**
	 * The onscreen location of this slice
	 *
	 * @param location
	 */
	public void setLocation(Rectangle location);

	/**
	 * The onscreen location of this slice
	 */
	public Rectangle getLocation();

	/**
	 * Used to unserstand if the mouse is hovering over this slice
	 *
	 * @param x mouse coordinate
	 * @param y mouse coordinate
	 * @return trhe if the mouse is over this slice
	 */
	public boolean contains(int x, int y);

	/**
	 * Used when the user is dragging the mouse, to keep track of the slice where
	 * the mouse was clicked
	 *
	 * @param selected
	 */
	public void setSelected(boolean selected);

	/**
	 * The image along with the Exif tags
	 *
	 * @return
	 */
	public PictureData getPictureData();

	/**
	 * Move the image around the slice
	 *
	 * @param deltaX pixels to shift the upper X coordinate of the viewable part of
	 *               the picture
	 * @param deltaY pixels to shift the upper X coordinate of the viewable part of
	 *               the picture
	 */
	public void move(double deltaX, double deltaY);

	/**
	 * To clear the image shifting when loading a new image. Image is centered on
	 * the screen.
	 */
	public void resetMovement();

	/**
	 * To adjust for zoom level
	 */
	public void changeZoomLevel(float zoomLevel);

	public void paint(Graphics2D g2d);

	/**
	 * Rotates the image if needed
	 */
	public void adjustRotation(boolean autorotation);

	/**
	 * Asks to recalculate the edge images
	 */
	public void startEdgesCalculation();

	/**
	 * Asks to clear the edges image and release the memory
	 */
	public void releaseEdges();

	// Communication part

	public void addPropertyChangeListener(PropertyChangeListener listener);

	public void removePropertyChangeListener(PropertyChangeListener listener);

}
