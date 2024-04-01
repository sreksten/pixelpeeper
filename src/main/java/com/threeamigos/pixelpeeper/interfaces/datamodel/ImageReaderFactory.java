package com.threeamigos.pixelpeeper.interfaces.datamodel;

/**
 * An interface able to produce {@link ImageReader}s.
 *
 * @author Stefano Reksten
 */
@FunctionalInterface
public interface ImageReaderFactory {

    ImageReader getImageReader();

}
