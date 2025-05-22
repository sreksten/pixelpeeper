package com.threeamigos.pixelpeeper.implementations.ui;

import com.threeamigos.common.util.interfaces.ui.FontService;
import com.threeamigos.pixelpeeper.data.PictureData;
import com.threeamigos.pixelpeeper.implementations.ui.inforenderers.InfoRendererShadow;
import com.threeamigos.pixelpeeper.implementations.ui.inforenderers.InfoRendererSimpleBorder;
import com.threeamigos.pixelpeeper.interfaces.datamodel.ExifTagsClassifier;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavors.ExifTagsPreferences;
import com.threeamigos.pixelpeeper.interfaces.ui.InfoRenderer;
import com.threeamigos.pixelpeeper.interfaces.ui.InfoRendererFactory;

public class InfoRendererFactoryImpl implements InfoRendererFactory {

    private final FontService fontService;
    private final ExifTagsPreferences exifTagsPreferences;

    public InfoRendererFactoryImpl(FontService fontService, ExifTagsPreferences exifTagsPreferences) {
        this.fontService = fontService;
        this.exifTagsPreferences = exifTagsPreferences;
    }

    @Override
    public InfoRenderer getInfoRenderer(PictureData pictureData, ExifTagsClassifier exifTagsClassifier) {
        if (exifTagsPreferences.getBorderThickness() == ExifTagsPreferences.BORDER_THICKNESS_LINE) {
            return new InfoRendererSimpleBorder(fontService, pictureData, exifTagsPreferences, exifTagsClassifier);
        } else {
            return new InfoRendererShadow(fontService, pictureData, exifTagsPreferences, exifTagsClassifier);
        }
    }
}
