package com.threeamigos.pixelpeeper.implementations.filters.ui;

import com.threeamigos.common.util.interfaces.messagehandler.ThrowableHandler;
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
    private final Windows311PaletteFilterPreferences windows311PaletteFilterPreferences;
    private final SharpnessHeatmapFilterPreferences sharpnessHeatmapFilterPreferences;
    private final HistogramClippingDetectorFilterPreferences histogramClippingDetectorFilterPreferences;
    private final NoiseEstimatorFilterPreferences noiseEstimatorFilterPreferences;
    private final VignettingProfileFilterPreferences vignettingProfileFilterPreferences;
    private final DepthOfFieldFilterPreferences depthOfFieldFilterPreferences;
    private final ChromaticAberrationFilterPreferences chromaticAberrationFilterPreferences;
    private final DistortionMeasurementFilterPreferences distortionMeasurementFilterPreferences;
    private final BokehQualityFilterPreferences bokehQualityFilterPreferences;
    private final DataModel dataModel;
    private final ExifImageReader exifImageReader;
    private final ThrowableHandler exceptionHandler;

    public FilterPreferencesSelectorFactoryImpl(FilterPreferences filterPreferences,
                                                CannyEdgesDetectorFilterPreferences cannyEdgesDetectorFilterPreferences,
                                                RomyJonaFilterPreferences romyJonaFilterPreferences,
                                                ZXSpectrumPaletteFilterPreferences zxSpectrumPaletteFilterPreferences,
                                                C64PaletteFilterPreferences c64PaletteFilterPreferences,
                                                Windows311PaletteFilterPreferences windows311PaletteFilterPreferences,
                                                SharpnessHeatmapFilterPreferences sharpnessHeatmapFilterPreferences,
                                                HistogramClippingDetectorFilterPreferences histogramClippingDetectorFilterPreferences,
                                                NoiseEstimatorFilterPreferences noiseEstimatorFilterPreferences,
                                                VignettingProfileFilterPreferences vignettingProfileFilterPreferences,
                                                DepthOfFieldFilterPreferences depthOfFieldFilterPreferences,
                                                ChromaticAberrationFilterPreferences chromaticAberrationFilterPreferences,
                                                DistortionMeasurementFilterPreferences distortionMeasurementFilterPreferences,
                                                BokehQualityFilterPreferences bokehQualityFilterPreferences,
                                                DataModel dataModel,
                                                ExifImageReader exifImageReader, ThrowableHandler exceptionHandler) {
        this.filterPreferences = filterPreferences;
        this.cannyEdgesDetectorFilterPreferences = cannyEdgesDetectorFilterPreferences;
        this.romyJonaFilterPreferences = romyJonaFilterPreferences;
        this.zxSpectrumPaletteFilterPreferences = zxSpectrumPaletteFilterPreferences;
        this.c64PaletteFilterPreferences = c64PaletteFilterPreferences;
        this.windows311PaletteFilterPreferences = windows311PaletteFilterPreferences;
        this.sharpnessHeatmapFilterPreferences = sharpnessHeatmapFilterPreferences;
        this.histogramClippingDetectorFilterPreferences = histogramClippingDetectorFilterPreferences;
        this.noiseEstimatorFilterPreferences = noiseEstimatorFilterPreferences;
        this.vignettingProfileFilterPreferences = vignettingProfileFilterPreferences;
        this.depthOfFieldFilterPreferences = depthOfFieldFilterPreferences;
        this.chromaticAberrationFilterPreferences = chromaticAberrationFilterPreferences;
        this.distortionMeasurementFilterPreferences = distortionMeasurementFilterPreferences;
        this.bokehQualityFilterPreferences = bokehQualityFilterPreferences;
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
                return new ZXSpectrumPaletteFilterPreferencesSelectorImpl(filterPreferences,
                        zxSpectrumPaletteFilterPreferences, dataModel,
                        exifImageReader, exceptionHandler);
            case C64_PALETTE:
                return new C64PaletteFilterPreferencesSelectorImpl(filterPreferences,
                        c64PaletteFilterPreferences, dataModel,
                        exifImageReader, exceptionHandler);
                case WINDOWS_3_11_PALETTE:
                    return new Windows311PaletteFilterPreferencesSelectorImpl(filterPreferences,
                            windows311PaletteFilterPreferences, dataModel,
                            exifImageReader, exceptionHandler);
            case SHARPNESS_HEATMAP:
                return new SharpnessHeatmapFilterPreferencesSelectorImpl(filterPreferences,
                        sharpnessHeatmapFilterPreferences, dataModel,
                        exifImageReader, exceptionHandler);
            case HISTOGRAM_CLIPPING_DETECTOR:
                return new HistogramClippingDetectorFilterPreferencesSelectorImpl(filterPreferences,
                        histogramClippingDetectorFilterPreferences, dataModel,
                        exifImageReader, exceptionHandler);
            case NOISE_ESTIMATOR:
                return new NoiseEstimatorFilterPreferencesSelectorImpl(filterPreferences,
                        noiseEstimatorFilterPreferences, dataModel,
                        exifImageReader, exceptionHandler);
            case VIGNETTING_PROFILE:
                return new VignettingProfileFilterPreferencesSelectorImpl(filterPreferences,
                        vignettingProfileFilterPreferences, dataModel,
                        exifImageReader, exceptionHandler);
            case DEPTH_OF_FIELD:
                return new DepthOfFieldFilterPreferencesSelectorImpl(filterPreferences,
                        depthOfFieldFilterPreferences, dataModel,
                        exifImageReader, exceptionHandler);
            case EQUIVALENT_EXPOSURE:
                return new EquivalentExposureFilterPreferencesSelectorImpl(filterPreferences,
                        dataModel, exifImageReader, exceptionHandler);
            case CHROMATIC_ABERRATION:
                return new ChromaticAberrationFilterPreferencesSelectorImpl(filterPreferences,
                        chromaticAberrationFilterPreferences, dataModel, exifImageReader, exceptionHandler);
            case DISTORTION_MEASUREMENT:
                return new DistortionMeasurementFilterPreferencesSelectorImpl(filterPreferences,
                        distortionMeasurementFilterPreferences, dataModel, exifImageReader, exceptionHandler);
            case BOKEH_QUALITY:
                return new BokehQualityFilterPreferencesSelectorImpl(filterPreferences,
                        bokehQualityFilterPreferences, dataModel, exifImageReader, exceptionHandler);
            default:
                throw new IllegalArgumentException();
        }
    }

}
