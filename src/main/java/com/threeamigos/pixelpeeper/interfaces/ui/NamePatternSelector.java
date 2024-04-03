package com.threeamigos.pixelpeeper.interfaces.ui;

import com.threeamigos.pixelpeeper.data.ExifTag;

import java.awt.*;

/**
 * The end-user may want to massively rename files using certain {@link ExifTag} values. The NamePatternSelector
 * provides a UI that assists the end-user to compose the filename suggesting EXIF tag names. Later this pattern
 * will be used to rename the files, by subsituting the EXIF tags with their values to compose the new file name.
 *
 * @author Stefano Reksten
 */
public interface NamePatternSelector {

    void selectNamePattern(Component component);

}
