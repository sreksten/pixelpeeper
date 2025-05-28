package com.threeamigos.pixelpeeper.implementations.edgedetect;

import com.threeamigos.common.util.interfaces.messagehandler.ExceptionHandler;
import com.threeamigos.pixelpeeper.implementations.edgedetect.flavours.*;
import com.threeamigos.pixelpeeper.interfaces.edgedetect.EdgesDetector;
import com.threeamigos.pixelpeeper.interfaces.edgedetect.EdgesDetectorFactory;
import com.threeamigos.pixelpeeper.interfaces.edgedetect.flavours.ZXSpectrumEdgesDetector;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavours.*;

public class EdgesDetectorFactoryImpl implements EdgesDetectorFactory {

    private final EdgesDetectorPreferences edgesDetectorPreferences;
    private final CannyEdgesDetectorPreferences cannyEdgesDetectorPreferences;
    private final RomyJonaEdgesDetectorPreferences romyJonaEdgesDetectorPreferences;
    private final ZXSpectrumEdgesDetectorPreferences zxSpectrumEdgesDetectorPreferences;
    private final C64EdgesDetectorPreferences c64EdgesDetectorPreferences;
    private final ExceptionHandler exceptionHandler;

    public EdgesDetectorFactoryImpl(EdgesDetectorPreferences edgesDetectorPreferences,
                                    CannyEdgesDetectorPreferences cannyEdgesDetectorPreferences,
                                    RomyJonaEdgesDetectorPreferences romyJonaEdgesDetectorPreferences,
                                    ZXSpectrumEdgesDetectorPreferences zxSpectrumEdgesDetectorPreferences,
                                    C64EdgesDetectorPreferences c64EdgesDetectorPreferences,
                                    ExceptionHandler exceptionHandler) {
        this.edgesDetectorPreferences = edgesDetectorPreferences;
        this.cannyEdgesDetectorPreferences = cannyEdgesDetectorPreferences;
        this.romyJonaEdgesDetectorPreferences = romyJonaEdgesDetectorPreferences;
        this.zxSpectrumEdgesDetectorPreferences = zxSpectrumEdgesDetectorPreferences;
        this.c64EdgesDetectorPreferences = c64EdgesDetectorPreferences;
        this.exceptionHandler = exceptionHandler;
    }

    @Override
    public EdgesDetector getEdgesDetector() {
        switch (edgesDetectorPreferences.getEdgesDetectorFlavour()) {
            case CANNY_EDGES_DETECTOR:
                return new CannyEdgesDetectorImpl(cannyEdgesDetectorPreferences);
            case ROMY_JONA_EDGES_DETECTOR:
                return new RomyJonaEdgesDetectorImpl(romyJonaEdgesDetectorPreferences);
            case SOBEL_EDGES_DETECTOR:
                return new SobelEdgesDetectorImpl();
            case ZX_SPECTRUM_EDGES_DETECTOR:
                return new ZXSpectrumEdgesDetectorImpl(zxSpectrumEdgesDetectorPreferences, exceptionHandler);
            case C64_EDGES_DETECTOR:
                return new C64EdgesDetectorImpl(c64EdgesDetectorPreferences, exceptionHandler);
            default:
                throw new IllegalArgumentException();
        }
    }

}
