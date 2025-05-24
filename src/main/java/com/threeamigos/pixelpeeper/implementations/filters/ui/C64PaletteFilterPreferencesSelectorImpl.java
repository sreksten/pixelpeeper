package com.threeamigos.pixelpeeper.implementations.filters.ui;

import com.threeamigos.common.util.interfaces.messagehandler.ExceptionHandler;
import com.threeamigos.pixelpeeper.interfaces.datamodel.DataModel;
import com.threeamigos.pixelpeeper.interfaces.datamodel.ExifImageReader;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavors.C64PaletteFilterPreferences;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavors.FilterPreferences;

import javax.swing.*;
import java.awt.*;
import java.util.Properties;

public class C64PaletteFilterPreferencesSelectorImpl extends ColorClashPaletteFilterPreferencesSelectorImpl {

    private final C64PaletteFilterPreferencesSelectorDataModel c64PaletteFilterPreferencesSelectorDataModel;

    public C64PaletteFilterPreferencesSelectorImpl(FilterPreferences filterPreferences,
                                                   C64PaletteFilterPreferences c64PaletteFilterPreferences,
                                                   DataModel dataModel, ExifImageReader exifImageReader, ExceptionHandler exceptionHandler) {
        super(filterPreferences, dataModel, exifImageReader, exceptionHandler);

        c64PaletteFilterPreferencesSelectorDataModel = new C64PaletteFilterPreferencesSelectorDataModel(dataModel,
                filterPreferences, c64PaletteFilterPreferences, testImageCanvas);
        c64PaletteFilterPreferencesSelectorDataModel.setSourceImage(testImage);
        c64PaletteFilterPreferencesSelectorDataModel.startFilterCalculation();
    }

    public C64PaletteFilterPreferencesSelectorDataModel getFilterPreferencesSelectorDataModel() {
        return c64PaletteFilterPreferencesSelectorDataModel;
    }

    String getPreferencesDescription() {
        return "C64 Palette Filter preferences";
    }
}
