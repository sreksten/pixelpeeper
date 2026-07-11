package com.threeamigos.pixelpeeper.interfaces.filters;

import com.threeamigos.pixelpeeper.data.ExifMap;

/**
 * Implemented by {@link Filter}s that require EXIF metadata in addition to the raw pixel data.
 *
 * <p>When {@code PictureData} starts a filter calculation it checks whether the created filter
 * implements this interface. If it does, {@link #setExifMap(ExifMap)} is called with the
 * picture's {@code ExifMap} <em>before</em> {@link Filter#process()} is invoked.</p>
 *
 * <p>Implementations must tolerate a {@code null} argument (e.g. when used in the preferences
 * preview dialog where no real EXIF data is available) and degrade gracefully by showing
 * placeholder text.</p>
 */
public interface ExifAwareFilter extends Filter {

    /**
     * Supplies the EXIF metadata for the image about to be processed.
     * May be {@code null} when no EXIF data is available (preview context).
     *
     * @param exifMap the image's EXIF map, or {@code null}
     */
    void setExifMap(ExifMap exifMap);
}
