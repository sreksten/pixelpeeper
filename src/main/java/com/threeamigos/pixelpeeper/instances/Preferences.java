package com.threeamigos.pixelpeeper.instances;

import com.threeamigos.common.util.implementations.json.JsonBuilderImpl;
import com.threeamigos.common.util.implementations.json.JsonColorAdapter;
import com.threeamigos.common.util.implementations.messagehandler.SwingMessageHandler;
import com.threeamigos.common.util.implementations.persistence.JsonStatusTrackerFactory;
import com.threeamigos.common.util.implementations.persistence.file.JsonFilePreferencesCollector;
import com.threeamigos.common.util.implementations.persistence.file.rootpathprovider.RootPathProviderImpl;
import com.threeamigos.common.util.interfaces.json.Json;
import com.threeamigos.common.util.interfaces.messagehandler.MessageHandler;
import com.threeamigos.common.util.interfaces.persistence.StatusTrackerFactory;
import com.threeamigos.common.util.interfaces.persistence.file.RootPathProvider;
import com.threeamigos.common.util.interfaces.preferences.flavours.HintsPreferences;
import com.threeamigos.common.util.interfaces.preferences.flavours.MainWindowPreferences;
import com.threeamigos.pixelpeeper.Main;
import com.threeamigos.pixelpeeper.implementations.preferences.flavors.*;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavors.*;

import java.awt.*;

public class Preferences {

    public static final MainWindowPreferences MAIN_WINDOW;
    public static final DragAndDropWindowPreferences DRAG_AND_DROP_WINDOW;
    public static final ImageHandlingPreferences IMAGE_HANDLING;
    public static final SessionPreferences SESSION;
    public static final ExifTagsPreferences EXIF_TAG;
    public static final GridPreferences GRID;
    public static final CursorPreferences CURSOR;
    public static final FilterPreferences FILTER;
    public static final CannyEdgesDetectorFilterPreferences CANNY_EDGES_DETECTOR_FILTER;
    public static final RomyJonaFilterPreferences ROMY_JONA_FILTER;
    public static final ZXSpectrumPaletteFilterPreferences ZX_SPECTRUM_PALETTE_FILTER;
    public static final C64PaletteFilterPreferences C64_PALETTE_FILTER;
    public static final Windows311PaletteFilterPreferences WINDOWS_3_11_PALETTE_FILTER;
    public static final SharpnessHeatmapFilterPreferences SHARPNESS_HEATMAP_FILTER;
    public static final HistogramClippingDetectorFilterPreferences HISTOGRAM_CLIPPING_DETECTOR_FILTER;
    public static final NoiseEstimatorFilterPreferences NOISE_ESTIMATOR_FILTER;
    public static final VignettingProfileFilterPreferences VIGNETTING_PROFILE_FILTER;
    public static final DepthOfFieldFilterPreferences DEPTH_OF_FIELD_FILTER;
    public static final ChromaticAberrationFilterPreferences CHROMATIC_ABERRATION_FILTER;
    public static final DistortionMeasurementFilterPreferences DISTORTION_MEASUREMENT_FILTER;
    public static final BokehQualityFilterPreferences BOKEH_QUALITY_FILTER;
    public static final NamePatternPreferences NAME_PATTERN;
    public static final DoodlingPreferences DOODLING;
    public static final HintsPreferences HINTS;
    public static final ShortcutsWindowPreferences SHORTCUTS_WINDOW;

    static {

        MessageHandler messageHandler = new SwingMessageHandler();

        RootPathProvider rootPathProvider = new RootPathProviderImpl(Main.class, messageHandler);

        // Preferences that can be stored and retrieved in a later run

        Json<com.threeamigos.common.util.interfaces.preferences.Preferences> preferencesJson = new JsonBuilderImpl().registerAdapter(Color.class, new JsonColorAdapter())
                .build(com.threeamigos.common.util.interfaces.preferences.Preferences.class);
        StatusTrackerFactory<com.threeamigos.common.util.interfaces.preferences.Preferences> preferencesStatusTrackerFactory = new JsonStatusTrackerFactory<>(
                preferencesJson);
        JsonFilePreferencesCollector<com.threeamigos.common.util.interfaces.preferences.Preferences> preferencesCollector = new JsonFilePreferencesCollector<>(
                rootPathProvider, messageHandler, preferencesStatusTrackerFactory, preferencesJson);

        // Main Preferences

        MAIN_WINDOW = new MainWindowPreferencesImpl();
        preferencesCollector.add(MAIN_WINDOW, "main_window.preferences");

        DRAG_AND_DROP_WINDOW = new DragAndDropWindowPreferencesImpl();
        preferencesCollector.add(DRAG_AND_DROP_WINDOW, "drag_and_drop_window.preferences");

        IMAGE_HANDLING = new ImageHandlingPreferencesImpl();
        preferencesCollector.add(IMAGE_HANDLING, "image_handling.preferences");

        SESSION = new SessionPreferencesImpl();
        preferencesCollector.add(SESSION, "session.preferences");

        EXIF_TAG = new ExifTagPreferencesImpl();
        preferencesCollector.add(EXIF_TAG, "exif_tag.preferences");

        // Decorators preferences

        GRID = new GridPreferencesImpl();
        preferencesCollector.add(GRID, "grid.preferences");

        CURSOR = new CursorPreferencesImpl();
        preferencesCollector.add(CURSOR, "cursor.preferences");

        // Edges Detector and other filters implementations preferences

        FILTER = new FilterPreferencesImpl();
        preferencesCollector.add(FILTER, "filter.preferences");

        CANNY_EDGES_DETECTOR_FILTER = new CannyEdgesDetectorFilterPreferencesImpl();
        preferencesCollector.add(CANNY_EDGES_DETECTOR_FILTER, "canny_edges_detector_filter.preferences");

        ROMY_JONA_FILTER = new RomyJonaFilterPreferencesImpl();
        preferencesCollector.add(ROMY_JONA_FILTER, "romy_jona_filter.preferences");

        ZX_SPECTRUM_PALETTE_FILTER = new ZXSpectrumPaletteFilterPreferencesImpl();
        preferencesCollector.add(ZX_SPECTRUM_PALETTE_FILTER, "zx_spectrum_palette_filter.preferences");

        C64_PALETTE_FILTER = new C64PaletteFilterPreferencesImpl();
        preferencesCollector.add(C64_PALETTE_FILTER, "c64_palette_filter.preferences");

        WINDOWS_3_11_PALETTE_FILTER = new Windows311PaletteFilterPreferencesImpl();
        preferencesCollector.add(WINDOWS_3_11_PALETTE_FILTER, "windows_311_palette_filter.preferences");

        SHARPNESS_HEATMAP_FILTER = new SharpnessHeatmapFilterPreferencesImpl();
        preferencesCollector.add(SHARPNESS_HEATMAP_FILTER, "sharpness_heatmap_filter.preferences");

        HISTOGRAM_CLIPPING_DETECTOR_FILTER = new HistogramClippingDetectorFilterPreferencesImpl();
        preferencesCollector.add(HISTOGRAM_CLIPPING_DETECTOR_FILTER, "histogram_clipping_detector_filter.preferences");

        NOISE_ESTIMATOR_FILTER = new NoiseEstimatorFilterPreferencesImpl();
        preferencesCollector.add(NOISE_ESTIMATOR_FILTER, "noise_estimator_filter.preferences");

        VIGNETTING_PROFILE_FILTER = new VignettingProfileFilterPreferencesImpl();
        preferencesCollector.add(VIGNETTING_PROFILE_FILTER, "vignetting_profile_filter.preferences");

        DEPTH_OF_FIELD_FILTER = new DepthOfFieldFilterPreferencesImpl();
        preferencesCollector.add(DEPTH_OF_FIELD_FILTER, "depth_of_field_filter.preferences");

        CHROMATIC_ABERRATION_FILTER = new ChromaticAberrationFilterPreferencesImpl();
        preferencesCollector.add(CHROMATIC_ABERRATION_FILTER, "chromatic_aberration_filter.preferences");

        DISTORTION_MEASUREMENT_FILTER = new DistortionMeasurementFilterPreferencesImpl();
        preferencesCollector.add(DISTORTION_MEASUREMENT_FILTER, "distortion_measurement_filter.preferences");

        BOKEH_QUALITY_FILTER = new BokehQualityFilterPreferencesImpl();
        preferencesCollector.add(BOKEH_QUALITY_FILTER, "bokeh_quality_filter.preferences");

        // Misc preferences

        NAME_PATTERN = new NamePatternPreferencesImpl();
        preferencesCollector.add(NAME_PATTERN, "name_pattern.preferences");

        DOODLING = new DoodlingPreferencesImpl();
        preferencesCollector.add(DOODLING, "drawing.preferences");

        HINTS = new HintsPreferencesImpl();
        preferencesCollector.add(HINTS, "hints.preferences");

        SHORTCUTS_WINDOW = new ShortcutsWindowPreferencesImpl();
        preferencesCollector.add(SHORTCUTS_WINDOW, "shortcuts.preferences");
    }

    private Preferences() {
    }
}
