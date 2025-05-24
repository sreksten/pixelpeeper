package com.threeamigos.pixelpeeper.implementations.filters.ui;

import com.threeamigos.common.util.interfaces.messagehandler.ExceptionHandler;
import com.threeamigos.pixelpeeper.interfaces.datamodel.DataModel;
import com.threeamigos.pixelpeeper.interfaces.datamodel.ExifImageReader;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavors.FilterPreferences;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavors.ZXSpectrumPaletteFilterPreferences;

public class ZXSpectrumPaletteFilterPreferencesSelectorImpl extends ColorClashPaletteFilterPreferencesSelectorImpl {

    private final ZXSpectrumPaletteFilterPreferencesSelectorDataModel filterPreferencesSelectorDataModel;

    public ZXSpectrumPaletteFilterPreferencesSelectorImpl(FilterPreferences filterPreferences,
                                                          ZXSpectrumPaletteFilterPreferences zxSpectrumPaletteFilterPreferences,
                                                          DataModel dataModel, ExifImageReader exifImageReader, ExceptionHandler exceptionHandler) {
        super(filterPreferences, dataModel, exifImageReader, exceptionHandler);

        filterPreferencesSelectorDataModel = new ZXSpectrumPaletteFilterPreferencesSelectorDataModel(dataModel,
                filterPreferences, zxSpectrumPaletteFilterPreferences, testImageCanvas);
        filterPreferencesSelectorDataModel.setSourceImage(testImage);
        filterPreferencesSelectorDataModel.startFilterCalculation();
    }

    @Override
    protected ZXSpectrumPaletteFilterPreferencesSelectorDataModel getFilterPreferencesSelectorDataModel() {
        return filterPreferencesSelectorDataModel;
    }

    String getPreferencesDescription() {
        return "ZX Spectrum Palette Filter Preferences";
    }

}
