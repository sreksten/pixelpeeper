package com.threeamigos.pixelpeeper.interfaces.ui;

import com.threeamigos.pixelpeeper.data.PictureData;
import com.threeamigos.pixelpeeper.interfaces.datamodel.ExifTagsClassifier;

public interface InfoRendererFactory {

    InfoRenderer getInfoRenderer(PictureData pictureData, ExifTagsClassifier exifTagsClassifier);

}
