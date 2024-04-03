package com.threeamigos.pixelpeeper.interfaces.ui;

import com.threeamigos.pixelpeeper.data.PictureData;
import com.threeamigos.pixelpeeper.interfaces.datamodel.ExifTagsClassifier;

/**
 * Interface that provides an {@link InfoRenderer} object used to render information about a given image
 * onscreen.
 *
 * @author Stefano Reksten
 */
public interface InfoRendererFactory {

    /**
     * @param pictureData        a {@link PictureData} object
     * @param exifTagsClassifier an {@link ExifTagsClassifier} object
     * @return an InfoRenderer
     */
    InfoRenderer getInfoRenderer(PictureData pictureData, ExifTagsClassifier exifTagsClassifier);

}
