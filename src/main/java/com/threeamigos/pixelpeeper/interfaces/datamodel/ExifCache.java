package com.threeamigos.pixelpeeper.interfaces.datamodel;

import com.threeamigos.pixelpeeper.data.ExifMap;

import java.io.File;
import java.util.Optional;

/**
 * An interface used to retrieve {@link ExifMap}s. Each image file can have
 * its own EXIF map (if EXIF information was saved within the picture).
 *
 * @author Stefano Reksten
 */
public interface ExifCache {

    /**
     * Clears the cache.
     */
    void clear();

    /**
     * Returns the {@link ExifMap} bound to an image.
     */
    Optional<ExifMap> getExifMap(File file);

}
