package com.threeamigos.pixelpeeper.interfaces.datamodel;

import com.threeamigos.pixelpeeper.data.PictureData;

import java.io.File;

/**
 * Given an image file, produces a {@link PictureData} object.
 *
 * @author Stefano Reksten
 */
@FunctionalInterface
public interface ExifImageReader {

    PictureData readImage(File file);

}
