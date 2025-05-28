package com.threeamigos.pixelpeeper.implementations.edgedetect.ui;

import com.threeamigos.common.util.interfaces.messagehandler.ExceptionHandler;
import com.threeamigos.pixelpeeper.interfaces.datamodel.DataModel;
import com.threeamigos.pixelpeeper.interfaces.datamodel.ExifImageReader;
import com.threeamigos.pixelpeeper.interfaces.edgedetect.ui.EdgesDetectorPreferencesSelector;
import com.threeamigos.pixelpeeper.interfaces.edgedetect.ui.EdgesDetectorPreferencesSelectorFactory;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavours.*;

import java.awt.*;

public class EdgesDetectorPreferencesSelectorFactoryImpl implements EdgesDetectorPreferencesSelectorFactory {

    private final EdgesDetectorPreferences edgesDetectorPreferences;
    private final CannyEdgesDetectorPreferences cannyEdgesDetectorPreferences;
    private final RomyJonaEdgesDetectorPreferences romyJonaEdgesDetectorPreferences;
    private final ZXSpectrumEdgesDetectorPreferences zxSpectrumEdgesDetectorPreferences;
    private final C64EdgesDetectorPreferences c64EdgesDetectorPreferences;
    private final DataModel dataModel;
    private final ExifImageReader exifImageReader;
    private final ExceptionHandler exceptionHandler;

    public EdgesDetectorPreferencesSelectorFactoryImpl(EdgesDetectorPreferences edgesDetectorPreferences,
                                                       CannyEdgesDetectorPreferences cannyEdgesDetectorPreferences,
                                                       RomyJonaEdgesDetectorPreferences romyJonaEdgesDetectorPreferences,
                                                       ZXSpectrumEdgesDetectorPreferences zxSpectrumEdgesDetectorPreferences,
                                                       C64EdgesDetectorPreferences c64EdgesDetectorPreferences,
                                                       DataModel dataModel,
                                                       ExifImageReader exifImageReader, ExceptionHandler exceptionHandler) {
        this.edgesDetectorPreferences = edgesDetectorPreferences;
        this.cannyEdgesDetectorPreferences = cannyEdgesDetectorPreferences;
        this.romyJonaEdgesDetectorPreferences = romyJonaEdgesDetectorPreferences;
        this.zxSpectrumEdgesDetectorPreferences = zxSpectrumEdgesDetectorPreferences;
        this.c64EdgesDetectorPreferences = c64EdgesDetectorPreferences;
        this.dataModel = dataModel;
        this.exifImageReader = exifImageReader;
        this.exceptionHandler = exceptionHandler;
    }

    @Override
    public EdgesDetectorPreferencesSelector createSelector(Component component) {
        switch (edgesDetectorPreferences.getEdgesDetectorFlavour()) {
            case CANNY_EDGES_DETECTOR:
                return new CannyEdgesDetectorPreferencesSelectorImpl(edgesDetectorPreferences,
                        cannyEdgesDetectorPreferences, dataModel, exifImageReader, exceptionHandler);
            case ROMY_JONA_EDGES_DETECTOR:
                return new RomyJonaEdgesDetectorPreferencesSelectorImpl(edgesDetectorPreferences,
                        romyJonaEdgesDetectorPreferences, dataModel, exifImageReader, exceptionHandler);
            case SOBEL_EDGES_DETECTOR:
                return new SobelEdgesDetectorPreferencesSelectorImpl(edgesDetectorPreferences, dataModel,
                        exifImageReader, exceptionHandler);
            case ZX_SPECTRUM_EDGES_DETECTOR:
                return new ZXSpectrumEdgesDetectorPreferencesSelectorImpl(edgesDetectorPreferences,
                        zxSpectrumEdgesDetectorPreferences, dataModel,
                        exifImageReader, exceptionHandler);
            case C64_EDGES_DETECTOR:
                return new C64EdgesDetectorPreferencesSelectorImpl(edgesDetectorPreferences,
                        c64EdgesDetectorPreferences, dataModel,
                        exifImageReader, exceptionHandler);
            default:
                throw new IllegalArgumentException();
        }
    }

}
