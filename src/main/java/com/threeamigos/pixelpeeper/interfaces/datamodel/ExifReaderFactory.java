package com.threeamigos.pixelpeeper.interfaces.datamodel;

/**
 * An interface able to produce EXIF readers
 *
 * @author Stefano Reksten
 */
@FunctionalInterface
public interface ExifReaderFactory {

    ExifReader getExifReader();

}
