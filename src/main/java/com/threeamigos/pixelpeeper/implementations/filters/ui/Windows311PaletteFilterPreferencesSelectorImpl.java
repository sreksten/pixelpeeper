package com.threeamigos.pixelpeeper.implementations.filters.ui;

import com.threeamigos.common.util.interfaces.messagehandler.ExceptionHandler;
import com.threeamigos.pixelpeeper.interfaces.datamodel.DataModel;
import com.threeamigos.pixelpeeper.interfaces.datamodel.ExifImageReader;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavors.FilterPreferences;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavors.Windows311PaletteFilterPreferences;

public class Windows311PaletteFilterPreferencesSelectorImpl extends PaletteFilterPreferencesSelectorImpl {

    private final Windows311PaletteFilterPreferencesSelectorDataModel filterPreferencesSelectorDataModel;

    public Windows311PaletteFilterPreferencesSelectorImpl(FilterPreferences filterPreferences,
                                                          Windows311PaletteFilterPreferences windows311PaletteFilterPreferences,
                                                          DataModel dataModel, ExifImageReader exifImageReader, ExceptionHandler exceptionHandler) {
        super(filterPreferences, dataModel, exifImageReader, exceptionHandler);

        filterPreferencesSelectorDataModel = new Windows311PaletteFilterPreferencesSelectorDataModel(dataModel,
                filterPreferences, windows311PaletteFilterPreferences, testImageCanvas);
        filterPreferencesSelectorDataModel.setSourceImage(testImage);
        filterPreferencesSelectorDataModel.startFilterCalculation();
    }

    @Override
    protected Windows311PaletteFilterPreferencesSelectorDataModel getFilterPreferencesSelectorDataModel() {
        return filterPreferencesSelectorDataModel;
    }

    String getPreferencesDescription() {
        return "Windows 3.11 Palette Filter preferences";
    }

}
