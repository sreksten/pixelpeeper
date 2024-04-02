package com.threeamigos.pixelpeeper.interfaces.datamodel;

import com.threeamigos.pixelpeeper.data.ExifMap;

import java.io.File;
import java.util.Optional;

/**
 * An interface able to retrieve {@link com.threeamigos.pixelpeeper.data.ExifTag}s from
 * image files. There is no guarantee we can retrieve EXIF tags as the image could have been
 * post-processed and saved without the EXIF tags.<br/>
 * This interface will retrieve the EXIF tags <b>only</b>, and will <b>not</b> load the whole image.
 * For that, see {@link ExifImageReader}.
 *
 * @author Stefano Reksten
 */
@FunctionalInterface
public interface ExifReader {

    Optional<ExifMap> readMetadata(File file);

}
