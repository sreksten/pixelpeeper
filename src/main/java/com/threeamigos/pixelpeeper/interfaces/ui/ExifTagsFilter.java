package com.threeamigos.pixelpeeper.interfaces.ui;

import com.threeamigos.pixelpeeper.data.ExifTag;

import java.awt.*;
import java.io.File;
import java.util.Collection;

/**
 * An interface that provides filtering capabilities to other components.
 * Given a collection of image files with their collection of {@link ExifTag}, this component should filter
 * only the images whose EXIF tags match with those known by this interface.<br/>
 * This means that in order to set the tags, either
 * <ul>
 *     <li>this interface must be extended adding some setters</li>
 *     <li>this interface is implemented by a UI that provides the end-user a meaningful way to select them</li>
 * </ul>
 * Given a collection of files, once they are filtered they can be also grouped and ordered:
 * <ul>
 *     <li>An EXIF tag should be used to group the files (e.g. camera manufacturer, focal length, ISO).
 *      The grouping operation should try to put as many files as it can within a certain group. While this is
 *      easy for certain tags (camera manufacturer and model), other settings can be trickier; for example,
 *      the focal length when images were taken using a zoom lens.<br/>
 *      In this case the component should try to group as many files as possible considering their proximity. E.g.
 *      images with a focal length of 49mm and 55mm could be grouped with images with a focal length of 50mm.
 *      This should be done considering a given tolerance (for example, plus or minus 5mm).</li>
 *     <li>An EXIF tag should be given in order to sort images within the same group.</li>
 * </ul>
 *
 * @author Stefano Reksten
 */
public interface ExifTagsFilter {

    /**
     * Max number of images that can be put in each group after the filter operation
     */
    int MAX_SELECTABLE_FILES_PER_GROUP = 9;

    /**
     * Filters a collection of files by their EXIF tags
     *
     * @param component parent component if this interface is implemented by a UI
     * @param files     original colection of files
     * @return filtered files
     */
    Collection<File> filterByTags(Component component, Collection<File> files);

    /**
     * The {@link ExifTag} used to group the files
     */
    ExifTag getTagToGroupBy();

    /**
     * The tolerance to be used when grouping files
     */
    int getTolerance();

    /**
     * The {@link ExifTag} used to order files within a group
     */
    ExifTag getTagToOrderBy();

}
