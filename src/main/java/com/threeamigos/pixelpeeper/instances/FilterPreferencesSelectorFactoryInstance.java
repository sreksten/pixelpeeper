package com.threeamigos.pixelpeeper.instances;

import com.threeamigos.pixelpeeper.implementations.filters.ui.FilterPreferencesSelectorFactoryImpl;
import com.threeamigos.pixelpeeper.interfaces.filters.ui.FilterPreferencesSelectorFactory;

public class FilterPreferencesSelectorFactoryInstance {

    private static final FilterPreferencesSelectorFactory instance =
            new FilterPreferencesSelectorFactoryImpl(Preferences.FILTER,
                    Preferences.CANNY_EDGES_DETECTOR_FILTER, Preferences.ROMY_JONA_FILTER,
                    Preferences.ZX_SPECTRUM_PALETTE_FILTER, Preferences.C64_PALETTE_FILTER,
                    Preferences.WINDOWS_3_11_PALETTE_FILTER, Preferences.SHARPNESS_HEATMAP_FILTER,
                    Preferences.HISTOGRAM_CLIPPING_DETECTOR_FILTER,
                    Preferences.NOISE_ESTIMATOR_FILTER,
                    Preferences.VIGNETTING_PROFILE_FILTER,
                    Preferences.DEPTH_OF_FIELD_FILTER,
                    Preferences.CHROMATIC_ABERRATION_FILTER,
                    Preferences.DISTORTION_MEASUREMENT_FILTER,
                    Preferences.BOKEH_QUALITY_FILTER,
                    DataModelInstance.get(), ExifImageReaderInstance.get(), MessageHandlerInstance.get());

    public static FilterPreferencesSelectorFactory get() {
        return instance;
    }

    private FilterPreferencesSelectorFactoryInstance() {
    }
}
