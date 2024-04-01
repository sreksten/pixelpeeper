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
import com.threeamigos.pixelpeeper.implementations.preferences.flavours.*;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavours.*;

import java.awt.*;

public class Preferences {

    public static final MainWindowPreferences MAIN_WINDOW;
    public static final DragAndDropWindowPreferences DRAG_AND_DROP_WINDOW;
    public static final ImageHandlingPreferences IMAGE_HANDLING;
    public static final SessionPreferences SESSION;
    public static final ExifTagsPreferences EXIF_TAG;
    public static final GridPreferences GRID;
    public static final CursorPreferences CURSOR;
    public static final EdgesDetectorPreferences EDGES_DETECTOR;
    public static final CannyEdgesDetectorPreferences CANNY_EDGES_DETECTOR;
    public static final RomyJonaEdgesDetectorPreferences ROMY_JONA_EDGES_DETECTOR;
    public static final NamePatternPreferences NAME_PATTERN;
    public static final DrawingPreferences DRAWING;
    public static final HintsPreferences HINTS;

    static {

        MessageHandler messageHandler = new SwingMessageHandler();

        RootPathProvider rootPathProvider = new RootPathProviderImpl(Main.class, messageHandler);

        // Preferences that can be stored and retrieved in a subsequent run

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

        // Edges Detector and implementations preferences

        EDGES_DETECTOR = new EdgesDetectorPreferencesImpl();
        preferencesCollector.add(EDGES_DETECTOR, "edges_detector.preferences");

        CANNY_EDGES_DETECTOR = new CannyEdgesDetectorPreferencesImpl();
        preferencesCollector.add(CANNY_EDGES_DETECTOR, "canny_edges_detector.preferences");

        ROMY_JONA_EDGES_DETECTOR = new RomyJonaEdgesDetectorPreferencesImpl();
        preferencesCollector.add(ROMY_JONA_EDGES_DETECTOR, "romy_jona_edge_detector.preferences");

        // Misc preferences

        NAME_PATTERN = new NamePatternPreferencesImpl();
        preferencesCollector.add(NAME_PATTERN, "name_pattern.preferences");

        DRAWING = new DrawingPreferencesImpl();
        preferencesCollector.add(DRAWING, "drawing.preferences");

        HINTS = new HintsPreferencesImpl();
        preferencesCollector.add(HINTS, "hints.preferences");
    }

    private Preferences() {
    }
}
