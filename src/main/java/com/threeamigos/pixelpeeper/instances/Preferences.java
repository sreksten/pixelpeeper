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
