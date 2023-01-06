package com.threeamigos.imageviewer.interfaces.datamodel;

import java.util.Collection;

import com.threeamigos.imageviewer.data.PictureData;

/**
 * Keeps track of the various image slices we see oncreen
 *
 * @author Stefano Reksten
 *
 */
public interface ImageSlicesManager {

	/**
	 * Clears all associated slices to load new images
	 */
	public void clear();

	public boolean hasLoadedImages();

	public ImageSlice createImageSlice(PictureData pictureData);

	public Collection<ImageSlice> getImageSlices();

	/**
	 * To be used when the main window is resized
	 *
	 * @param panelWidth
	 * @param panelHeight
	 */
	public void reframeImageSlices(int panelWidth, int panelHeight);

	public void move(int deltaX, int deltaY, boolean allImages);

	public void resetMovement();

	public void setActiveSlice(int x, int y);

	public void resetActiveSlice();

}
