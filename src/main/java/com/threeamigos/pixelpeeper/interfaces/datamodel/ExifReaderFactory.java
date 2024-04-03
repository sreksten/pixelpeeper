package com.threeamigos.pixelpeeper.interfaces.datamodel;

/**
 * An interface able to produce {@link ExifReader}s.
 *
 * @author Stefano Reksten
 */
@FunctionalInterface
public interface ExifReaderFactory {

    ExifReader getExifReader();

}
