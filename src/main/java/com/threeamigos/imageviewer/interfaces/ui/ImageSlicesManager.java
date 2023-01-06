package com.threeamigos.imageviewer.interfaces.ui;

import java.util.Collection;

/**
 * Keeps track of the various image slices we see oncreen
 *
 * @author Stefano Reksten
 *
 */
public interface ImageSlicesManager extends ImageSliceFactory {

	/**
	 * Clears all associated slices to load new images
	 */
	public void clear();

	public boolean hasLoadedImages();

	public void addImageSlice(ImageSlice slice);

	public Collection<ImageSlice> getImageSlices();

	/**
	 * Returns the slice the mouse is over
	 *
	 * @param x mouse coordinate
	 * @param y mouse coordinate
	 * @return the slice on which the mouse is hovering, if any
	 */
	public ImageSlice findImageSlice(int x, int y);

	/**
	 * To be used when the main window is resized
	 *
	 * @param panelWidth
	 * @param panelHeight
	 */
	public void reframeImageSlices(int panelWidth, int panelHeight);

}
