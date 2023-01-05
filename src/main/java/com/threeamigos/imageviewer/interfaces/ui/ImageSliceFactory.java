package com.threeamigos.imageviewer.interfaces.ui;

import com.threeamigos.imageviewer.data.PictureData;

/**
 * Creates a slice object associated to an image
 *
 * @author Stefano Reksten
 *
 */
public interface ImageSliceFactory {

	public ImageSlice createImageSlice(PictureData pictureData);

}
