package com.threeamigos.pixelpeeper.implementations.filters;

import com.threeamigos.common.util.interfaces.messagehandler.ExceptionHandler;
import com.threeamigos.pixelpeeper.implementations.filters.flavors.*;
import com.threeamigos.pixelpeeper.interfaces.filters.Filter;
import com.threeamigos.pixelpeeper.interfaces.filters.FilterFactory;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavors.*;

public class FilterFactoryImpl implements FilterFactory {

    private final FilterPreferences filterPreferences;
    private final CannyEdgesDetectorFilterPreferences cannyEdgesDetectorFilterPreferences;
    private final RomyJonaFilterPreferences romyJonaFilterPreferences;
    private final ZXSpectrumPaletteFilterPreferences zxSpectrumPaletteFilterPreferences;
    private final C64PaletteFilterPreferences c64PaletteFilterPreferences;
    private final ExceptionHandler exceptionHandler;

    public FilterFactoryImpl(FilterPreferences filterPreferences,
                             CannyEdgesDetectorFilterPreferences cannyEdgesDetectorFilterPreferences,
                             RomyJonaFilterPreferences romyJonaFilterPreferences,
                             ZXSpectrumPaletteFilterPreferences zxSpectrumPaletteFilterPreferences,
                             C64PaletteFilterPreferences c64PaletteFilterPreferences,
                             ExceptionHandler exceptionHandler) {
        this.filterPreferences = filterPreferences;
        this.cannyEdgesDetectorFilterPreferences = cannyEdgesDetectorFilterPreferences;
        this.romyJonaFilterPreferences = romyJonaFilterPreferences;
        this.zxSpectrumPaletteFilterPreferences = zxSpectrumPaletteFilterPreferences;
        this.c64PaletteFilterPreferences = c64PaletteFilterPreferences;
        this.exceptionHandler = exceptionHandler;
    }

    @Override
    public Filter getFilter() {
        switch (filterPreferences.getFilterFlavor()) {
            case CANNY_EDGES_DETECTOR:
                return new CannyEdgesDetectorFilterImpl(cannyEdgesDetectorFilterPreferences);
            case ROMY_JONA:
                return new RomyJonaFilterImpl(romyJonaFilterPreferences);
            case SOBEL_EDGES_DETECTOR:
                return new SobelEdgesDetectorFilterImpl();
            case ZX_SPECTRUM_PALETTE:
                return new ZXSpectrumPaletteFilterImpl(zxSpectrumPaletteFilterPreferences, exceptionHandler);
            case C64_PALETTE:
                return new C64PaletteFilterImpl(c64PaletteFilterPreferences, exceptionHandler);
            default:
                throw new IllegalArgumentException();
        }
    }

}
