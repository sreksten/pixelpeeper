package com.threeamigos.pixelpeeper.interfaces.datamodel;

import java.io.File;

/**
 * An interface used to massively rename files, using one or more EXIF tags in order to
 * compose the new file name. According to a name pattern, it will substitute parts contained
 * within the left and right delimiter with the actual value of EXIF tags found within the image
 * itself. E.g. if specifying {FILENAME}_shot_with_{CAMERA_MODEL} it could for example produce
 * IMG0000_shot_with_Panasonic GX9.jpg.
 * See also {@link FileRenamer}.
 *
 * @author Stefano Reksten
 */
public interface NamePattern {

    /**
     * Left delimiter
     */
    String LEFT_BRACKET = "{";

    /**
     * Right delimiter
     */
    String RIGHT_BRACKET = "}";

    boolean rename(File file);

}
