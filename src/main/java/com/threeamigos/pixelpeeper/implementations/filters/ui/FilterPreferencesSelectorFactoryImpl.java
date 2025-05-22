package com.threeamigos.pixelpeeper.implementations.filters.ui;

import com.threeamigos.common.util.interfaces.messagehandler.ExceptionHandler;
import com.threeamigos.pixelpeeper.interfaces.datamodel.DataModel;
import com.threeamigos.pixelpeeper.interfaces.datamodel.ExifImageReader;
import com.threeamigos.pixelpeeper.interfaces.filters.ui.FilterPreferencesSelector;
import com.threeamigos.pixelpeeper.interfaces.filters.ui.FilterPreferencesSelectorFactory;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavors.*;

import java.awt.*;

public class FilterPreferencesSelectorFactoryImpl implements FilterPreferencesSelectorFactory {

    private final FilterPreferences filterPreferences;
    private final CannyEdgesDetectorFilterPreferences cannyEdgesDetectorFilterPreferences;
    private final RomyJonaFilterPreferences romyJonaFilterPreferences;
    private final ZXSpectrumPaletteFilterPreferences zxSpectrumPaletteFilterPreferences;
    private final C64PaletteFilterPreferences c64PaletteFilterPreferences;
    private final DataModel dataModel;
    private final ExifImageReader exifImageReader;
    private final ExceptionHandler exceptionHandler;

    public FilterPreferencesSelectorFactoryImpl(FilterPreferences filterPreferences,
                                                CannyEdgesDetectorFilterPreferences cannyEdgesDetectorFilterPreferences,
                                                RomyJonaFilterPreferences romyJonaFilterPreferences,
                                                ZXSpectrumPaletteFilterPreferences zxSpectrumPaletteFilterPreferences,
                                                C64PaletteFilterPreferences c64PaletteFilterPreferences,
                                                DataModel dataModel,
                                                ExifImageReader exifImageReader, ExceptionHandler exceptionHandler) {
        this.filterPreferences = filterPreferences;
        this.cannyEdgesDetectorFilterPreferences = cannyEdgesDetectorFilterPreferences;
        this.romyJonaFilterPreferences = romyJonaFilterPreferences;
        this.zxSpectrumPaletteFilterPreferences = zxSpectrumPaletteFilterPreferences;
        this.c64PaletteFilterPreferences = c64PaletteFilterPreferences;
        this.dataModel = dataModel;
        this.exifImageReader = exifImageReader;
        this.exceptionHandler = exceptionHandler;
    }

    @Override
    public FilterPreferencesSelector createSelector(Component component) {
        switch (filterPreferences.getFilterFlavor()) {
            case CANNY_EDGES_DETECTOR:
                return new CannyFilterPreferencesSelectorImpl(filterPreferences,
                        cannyEdgesDetectorFilterPreferences, dataModel, exifImageReader, exceptionHandler);
            case ROMY_JONA:
                return new RomyJonaFilterPreferencesSelectorImpl(filterPreferences,
                        romyJonaFilterPreferences, dataModel, exifImageReader, exceptionHandler);
            case SOBEL_EDGES_DETECTOR:
                return new SobelFilterPreferencesSelectorImpl(filterPreferences, dataModel,
                        exifImageReader, exceptionHandler);
            case ZX_SPECTRUM_PALETTE:
                return new ZXSpectrumFilterPreferencesSelectorImpl(filterPreferences,
                        zxSpectrumPaletteFilterPreferences, dataModel,
                        exifImageReader, exceptionHandler);
            case C64_PALETTE:
                return new C64FilterPreferencesSelectorImpl(filterPreferences,
                        c64PaletteFilterPreferences, dataModel,
                        exifImageReader, exceptionHandler);
            default:
                throw new IllegalArgumentException();
        }
    }

}
