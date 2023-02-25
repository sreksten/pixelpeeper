package com.threeamigos.pixelpeeper;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.SwingUtilities;

import com.threeamigos.common.util.implementations.CompositeMessageHandler;
import com.threeamigos.common.util.implementations.ConsoleMessageHandler;
import com.threeamigos.common.util.implementations.SwingMessageHandler;
import com.threeamigos.common.util.interfaces.MessageHandler;
import com.threeamigos.common.util.preferences.filebased.implementations.RootPathProviderImpl;
import com.threeamigos.common.util.preferences.filebased.interfaces.RootPathProvider;
import com.threeamigos.pixelpeeper.implementations.datamodel.CropFactorRepositoryImpl;
import com.threeamigos.pixelpeeper.implementations.datamodel.CropFactorRepositoryManagerImpl;
import com.threeamigos.pixelpeeper.implementations.datamodel.DataModelImpl;
import com.threeamigos.pixelpeeper.implementations.datamodel.ExifCacheImpl;
import com.threeamigos.pixelpeeper.implementations.datamodel.ExifImageReaderImpl;
import com.threeamigos.pixelpeeper.implementations.datamodel.ExifReaderFactoryImpl;
import com.threeamigos.pixelpeeper.implementations.datamodel.NamePatternImpl;
import com.threeamigos.pixelpeeper.implementations.datamodel.ImageReaderFactoryImpl;
import com.threeamigos.pixelpeeper.implementations.datamodel.ImageSlicesImpl;
import com.threeamigos.pixelpeeper.implementations.datamodel.TagsClassifierImpl;
import com.threeamigos.pixelpeeper.implementations.edgedetect.EdgesDetectorFactoryImpl;
import com.threeamigos.pixelpeeper.implementations.edgedetect.ui.EdgesDetectorPreferencesSelectorFactoryImpl;
import com.threeamigos.pixelpeeper.implementations.persister.PersistablesHelper;
import com.threeamigos.pixelpeeper.implementations.preferences.flavours.CannyEdgesDetectorPreferencesImpl;
import com.threeamigos.pixelpeeper.implementations.preferences.flavours.CursorPreferencesImpl;
import com.threeamigos.pixelpeeper.implementations.preferences.flavours.DragAndDropWindowPreferencesImpl;
import com.threeamigos.pixelpeeper.implementations.preferences.flavours.DrawingPreferencesImpl;
import com.threeamigos.pixelpeeper.implementations.preferences.flavours.EdgesDetectorPreferencesImpl;
import com.threeamigos.pixelpeeper.implementations.preferences.flavours.ExifTagPreferencesImpl;
import com.threeamigos.pixelpeeper.implementations.preferences.flavours.GridPreferencesImpl;
import com.threeamigos.pixelpeeper.implementations.preferences.flavours.HintsPreferencesImpl;
import com.threeamigos.pixelpeeper.implementations.preferences.flavours.ImageHandlingPreferencesImpl;
import com.threeamigos.pixelpeeper.implementations.preferences.flavours.MainWindowPreferencesImpl;
import com.threeamigos.pixelpeeper.implementations.preferences.flavours.NamePatternPreferencesImpl;
import com.threeamigos.pixelpeeper.implementations.preferences.flavours.RomyJonaEdgesDetectorPreferencesImpl;
import com.threeamigos.pixelpeeper.implementations.preferences.flavours.SessionPreferencesImpl;
import com.threeamigos.pixelpeeper.implementations.ui.AboutWindowImpl;
import com.threeamigos.pixelpeeper.implementations.ui.ChainedInputConsumer;
import com.threeamigos.pixelpeeper.implementations.ui.CropFactorProviderImpl;
import com.threeamigos.pixelpeeper.implementations.ui.CursorManagerImpl;
import com.threeamigos.pixelpeeper.implementations.ui.DragAndDropWindowImpl;
import com.threeamigos.pixelpeeper.implementations.ui.ExifTagsFilterImpl;
import com.threeamigos.pixelpeeper.implementations.ui.FileSelectorImpl;
import com.threeamigos.pixelpeeper.implementations.ui.FontServiceImpl;
import com.threeamigos.pixelpeeper.implementations.ui.HintsCollectorImpl;
import com.threeamigos.pixelpeeper.implementations.ui.HintsWindowImpl;
import com.threeamigos.pixelpeeper.implementations.ui.MouseTrackerImpl;
import com.threeamigos.pixelpeeper.implementations.ui.NamePatternSelectorImpl;
import com.threeamigos.pixelpeeper.implementations.ui.imagedecorators.GridDecorator;
import com.threeamigos.pixelpeeper.implementations.ui.plugins.CursorPlugin;
import com.threeamigos.pixelpeeper.implementations.ui.plugins.EdgesDetectorPlugin;
import com.threeamigos.pixelpeeper.implementations.ui.plugins.ExifTagsPlugin;
import com.threeamigos.pixelpeeper.implementations.ui.plugins.GridPlugin;
import com.threeamigos.pixelpeeper.implementations.ui.plugins.ImageHandlingPlugin;
import com.threeamigos.pixelpeeper.interfaces.datamodel.CropFactorRepository;
import com.threeamigos.pixelpeeper.interfaces.datamodel.CropFactorRepositoryManager;
import com.threeamigos.pixelpeeper.interfaces.datamodel.DataModel;
import com.threeamigos.pixelpeeper.interfaces.datamodel.ExifCache;
import com.threeamigos.pixelpeeper.interfaces.datamodel.ExifImageReader;
import com.threeamigos.pixelpeeper.interfaces.datamodel.NamePattern;
import com.threeamigos.pixelpeeper.interfaces.datamodel.ImageReaderFactory;
import com.threeamigos.pixelpeeper.interfaces.datamodel.ImageSlices;
import com.threeamigos.pixelpeeper.interfaces.datamodel.TagsClassifier;
import com.threeamigos.pixelpeeper.interfaces.edgedetect.EdgesDetectorFactory;
import com.threeamigos.pixelpeeper.interfaces.edgedetect.ui.EdgesDetectorPreferencesSelectorFactory;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavours.CannyEdgesDetectorPreferences;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavours.CursorPreferences;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavours.DragAndDropWindowPreferences;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavours.DrawingPreferences;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavours.EdgesDetectorPreferences;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavours.ExifTagPreferences;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavours.GridPreferences;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavours.HintsPreferences;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavours.ImageHandlingPreferences;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavours.MainWindowPreferences;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavours.NamePatternPreferences;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavours.RomyJonaEdgesDetectorPreferences;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavours.SessionPreferences;
import com.threeamigos.pixelpeeper.interfaces.ui.CropFactorProvider;
import com.threeamigos.pixelpeeper.interfaces.ui.CursorManager;
import com.threeamigos.pixelpeeper.interfaces.ui.DragAndDropWindow;
import com.threeamigos.pixelpeeper.interfaces.ui.ExifTagsFilter;
import com.threeamigos.pixelpeeper.interfaces.ui.FileSelector;
import com.threeamigos.pixelpeeper.interfaces.ui.FontService;
import com.threeamigos.pixelpeeper.interfaces.ui.HintsCollector;
import com.threeamigos.pixelpeeper.interfaces.ui.HintsWindow;
import com.threeamigos.pixelpeeper.interfaces.ui.ImageDecorator;
import com.threeamigos.pixelpeeper.interfaces.ui.KeyRegistry;
import com.threeamigos.pixelpeeper.interfaces.ui.MainWindowPlugin;
import com.threeamigos.pixelpeeper.interfaces.ui.MouseTracker;
import com.threeamigos.pixelpeeper.interfaces.ui.NamePatternSelector;

/**
 * Uses the Metadata Extractor from https://drewnoakes.com/code/exif/
 *
 * @author Stefano Reksten
 *
 */

public class Main {

	// BUGFIX: if the drag and drop window is open focus is switched to that. Well
	// maybe not quite a but but...

	// BUGFIX: the big pointer may flicker when changed

	// TODO: lens manufacturer

	// FOOD-FOR-THOUGHTS: prepare the image with edges instead of drawing it every
	// time?

	// FOOD-FOR-THOUGHTS: should ImageReader use also other image libraries?

	// FOOD-FOR-THOUGHTS: set up an unique queue for message processing?

	public Main() {

		// A way to show error/warning messages to the user

		MessageHandler messageHandler = new CompositeMessageHandler(new SwingMessageHandler(),
				new ConsoleMessageHandler());

		// The directory in which we store preferences and other files

		RootPathProvider rootPathProvider = new RootPathProviderImpl(this, messageHandler);
		if (rootPathProvider.shouldAbort()) {
			System.exit(0);
		}

		// Preferences that can be stored and retrieved in a subsequent run

		PersistablesHelper persistablesHelper = new PersistablesHelper(rootPathProvider, messageHandler);

		// Main Preferences

		MainWindowPreferences mainWindowPreferences = new MainWindowPreferencesImpl();
		persistablesHelper.register(mainWindowPreferences, "main_window.preferences");

		DragAndDropWindowPreferences dragAndDropWindowPreferences = new DragAndDropWindowPreferencesImpl();
		persistablesHelper.register(dragAndDropWindowPreferences, "drag_and_drop_window.preferences");

		ImageHandlingPreferences imageHandlingPreferences = new ImageHandlingPreferencesImpl();
		persistablesHelper.register(imageHandlingPreferences, "image_handling.preferences");

		SessionPreferences sessionPreferences = new SessionPreferencesImpl();
		persistablesHelper.register(sessionPreferences, "session.preferences");

		ExifTagPreferences exifTagsPreferences = new ExifTagPreferencesImpl();
		persistablesHelper.register(exifTagsPreferences, "exif_tag.preferences");

		// Decorators preferences

		GridPreferences gridPreferences = new GridPreferencesImpl();
		persistablesHelper.register(gridPreferences, "grid.preferences");

		CursorPreferences cursorPreferences = new CursorPreferencesImpl();
		persistablesHelper.register(cursorPreferences, "cursor.preferences");

		// Edges Detector and implementations preferences

		EdgesDetectorPreferences edgesDetectorPreferences = new EdgesDetectorPreferencesImpl();
		persistablesHelper.register(edgesDetectorPreferences, "edges_detector.preferences");

		CannyEdgesDetectorPreferences cannyEdgesDetectorPreferences = new CannyEdgesDetectorPreferencesImpl();
		persistablesHelper.register(cannyEdgesDetectorPreferences, "canny_edges_detector.preferences");

		RomyJonaEdgesDetectorPreferences romyJonaEdgesDetectorPreferences = new RomyJonaEdgesDetectorPreferencesImpl();
		persistablesHelper.register(romyJonaEdgesDetectorPreferences, "romy_jona_edge_detector.preferences");

		// Misc preferences

		NamePatternPreferences namePatternPreferences = new NamePatternPreferencesImpl();
		persistablesHelper.register(namePatternPreferences, "name_pattern.preferences");
		
		DrawingPreferences drawingPreferences = new DrawingPreferencesImpl();
		persistablesHelper.register(drawingPreferences, "drawing.preferences");

		HintsPreferences hintsPreferences = new HintsPreferencesImpl();
		persistablesHelper.register(hintsPreferences, "hints.preferences");

		// Data model

		// Since not all cameras provide the 35_mm_equivalent tag, we need a way to
		// retrieve this.
		// A simple file database provides the information to the CropFactoryProvider.
		CropFactorRepository cropFactorRepository = new CropFactorRepositoryImpl();
		CropFactorRepositoryManager cropFactorRepositoryManager = new CropFactorRepositoryManagerImpl(
				cropFactorRepository, "crop_factor.repository", "Crop factor repository", rootPathProvider,
				messageHandler);
		persistablesHelper.add(cropFactorRepositoryManager);

		CropFactorProvider cropFactorProvider = new CropFactorProviderImpl(cropFactorRepository);

		ImageReaderFactory imageReaderFactory = new ImageReaderFactoryImpl(imageHandlingPreferences);

		ExifCache exifCache = new ExifCacheImpl(new ExifReaderFactoryImpl(imageHandlingPreferences),
				cropFactorProvider);

		EdgesDetectorFactory edgesDetectorFactory = new EdgesDetectorFactoryImpl(edgesDetectorPreferences,
				cannyEdgesDetectorPreferences, romyJonaEdgesDetectorPreferences);

		ExifImageReader exifImageReader = new ExifImageReaderImpl(imageHandlingPreferences, imageReaderFactory,
				exifCache, edgesDetectorPreferences, edgesDetectorFactory, messageHandler);

		ExifTagsFilter exifTagsFilter = new ExifTagsFilterImpl(exifCache, messageHandler);

		TagsClassifier tagsClassifier = new TagsClassifierImpl();

		FontService fontService = new FontServiceImpl();

		ImageSlices imageSlices = new ImageSlicesImpl(tagsClassifier, exifTagsPreferences, imageHandlingPreferences,
				drawingPreferences, edgesDetectorPreferences, fontService);

		ChainedInputConsumer chainedInputConsumer = new ChainedInputConsumer();

		HintsCollector hintsCollector = new HintsCollectorImpl();
		hintsCollector.addHints(KeyRegistry.NO_KEY.getHints());

		DataModel dataModel = new DataModelImpl(tagsClassifier, imageSlices, imageHandlingPreferences,
				sessionPreferences, edgesDetectorPreferences, exifCache, exifImageReader, exifTagsFilter,
				messageHandler);
		chainedInputConsumer.addConsumer(dataModel.getInputConsumer(), ChainedInputConsumer.PRIORITY_LOW);
		hintsCollector.addHints(dataModel);
		imageHandlingPreferences.addPropertyChangeListener(dataModel);
		edgesDetectorPreferences.addPropertyChangeListener(dataModel);

		new Thread(() -> dataModel.loadLastFiles()).run();

		// User Interface

		EdgesDetectorPreferencesSelectorFactory edgesDetectorParametersSelectorFactory = new EdgesDetectorPreferencesSelectorFactoryImpl(
				edgesDetectorPreferences, cannyEdgesDetectorPreferences, romyJonaEdgesDetectorPreferences, dataModel,
				exifImageReader, messageHandler);

		FileSelector fileSelector = new FileSelectorImpl(sessionPreferences);
		
		NamePatternSelector namePatternSelector = new NamePatternSelectorImpl(namePatternPreferences);
		
		NamePattern namePattern = new NamePatternImpl(namePatternPreferences, exifCache);

		MouseTracker mouseTracker = new MouseTrackerImpl();
		chainedInputConsumer.addConsumer(mouseTracker.getInputConsumer(), ChainedInputConsumer.PRIORITY_HIGH);
		mouseTracker.addPropertyChangeListener(dataModel);

		DragAndDropWindow dragAndDropWindow = new DragAndDropWindowImpl(dragAndDropWindowPreferences, exifCache,
				fontService, messageHandler);
		hintsCollector.addHints(dragAndDropWindow);

		Collection<ImageDecorator> decorators = new ArrayList<>();

		GridDecorator gridDecorator = new GridDecorator(mainWindowPreferences, gridPreferences);
		decorators.add(gridDecorator);

		CursorManager cursorManager = new CursorManagerImpl(cursorPreferences);
		chainedInputConsumer.addConsumer(cursorManager.getInputConsumer(), ChainedInputConsumer.PRIORITY_HIGH);
		cursorPreferences.addPropertyChangeListener(cursorManager);
		hintsCollector.addHints(cursorManager);

		HintsWindow hintsWindow = new HintsWindowImpl(hintsPreferences, hintsCollector);

		List<MainWindowPlugin> plugins = new ArrayList<>();

		EdgesDetectorPlugin edgesDetectorPlugin = new EdgesDetectorPlugin(edgesDetectorPreferences,
				edgesDetectorParametersSelectorFactory);
		edgesDetectorPlugin.addPropertyChangeListener(dataModel);
		chainedInputConsumer.addConsumer(edgesDetectorPlugin.getInputConsumer(), ChainedInputConsumer.PRIORITY_LOW);
		edgesDetectorPreferences.addPropertyChangeListener(edgesDetectorPlugin);
		plugins.add(edgesDetectorPlugin);

		ImageHandlingPlugin imageHandlingPlugin = new ImageHandlingPlugin(imageHandlingPreferences);
		imageHandlingPlugin.addPropertyChangeListener(dataModel);
		chainedInputConsumer.addConsumer(imageHandlingPlugin.getInputConsumer(), ChainedInputConsumer.PRIORITY_LOW);
		imageHandlingPreferences.addPropertyChangeListener(imageHandlingPlugin);
		plugins.add(imageHandlingPlugin);

		GridPlugin gridPlugin = new GridPlugin(gridPreferences);
		gridPreferences.addPropertyChangeListener(gridPlugin);
		chainedInputConsumer.addConsumer(gridPlugin.getInputConsumer(), ChainedInputConsumer.PRIORITY_MEDIUM);
		hintsCollector.addHints(gridPlugin);
		plugins.add(gridPlugin);

		CursorPlugin cursorPlugin = new CursorPlugin(cursorPreferences, cursorManager);
		plugins.add(cursorPlugin);

		ExifTagsPlugin exifTagsPlugin = new ExifTagsPlugin(exifTagsPreferences);
		plugins.add(exifTagsPlugin);
		chainedInputConsumer.addConsumer(exifTagsPlugin.getInputConsumer(), ChainedInputConsumer.PRIORITY_LOW);

		JMenuBar menuBar = new JMenuBar();

		ImageViewerCanvas imageViewerCanvas = new ImageViewerCanvas(menuBar, mainWindowPreferences,
				dragAndDropWindowPreferences, dataModel, cursorManager, fileSelector, namePatternSelector, namePattern,
				chainedInputConsumer, decorators, new AboutWindowImpl(), hintsWindow, dragAndDropWindow, messageHandler,
				plugins);
		dataModel.addPropertyChangeListener(imageViewerCanvas);
		imageHandlingPreferences.addPropertyChangeListener(imageViewerCanvas);
		cursorPreferences.addPropertyChangeListener(imageViewerCanvas);
		gridPreferences.addPropertyChangeListener(imageViewerCanvas);
		exifTagsPreferences.addPropertyChangeListener(imageViewerCanvas);
		imageSlices.addPropertyChangeListener(imageViewerCanvas);

		ControlsPanel controlsPanel = new ControlsPanel(imageHandlingPreferences, drawingPreferences, dataModel);
		imageHandlingPreferences.addPropertyChangeListener(controlsPanel);
		dataModel.addPropertyChangeListener(controlsPanel);

		cursorManager.addPropertyChangeListener(imageViewerCanvas);

		JFrame jframe = prepareFrame(menuBar, imageViewerCanvas, controlsPanel, mainWindowPreferences);

		jframe.setVisible(true);

		if (hintsPreferences.isHintsVisibleAtStartup()) {
			hintsWindow.showHints(jframe);
		}
	}

	private JFrame prepareFrame(JMenuBar menuBar, ImageViewerCanvas imageViewerCanvas, ControlsPanel controlsPanel,
			MainWindowPreferences mainWindowPreferences) {

		JFrame jframe = new JFrame("3AM Pixel Peeper");
		jframe.setMinimumSize(new Dimension(800, 600));

		jframe.setJMenuBar(menuBar);
		jframe.add(imageViewerCanvas, BorderLayout.CENTER);
		jframe.add(controlsPanel, BorderLayout.SOUTH);

		jframe.pack();
		jframe.setResizable(true);
		jframe.setLocation(mainWindowPreferences.getX(), mainWindowPreferences.getY());

		jframe.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				imageViewerCanvas.reframeDataModel();
				mainWindowPreferences.setWidth(imageViewerCanvas.getWidth());
				mainWindowPreferences.setHeight(imageViewerCanvas.getHeight());
			}

			@Override
			public void componentMoved(ComponentEvent e) {
				mainWindowPreferences.setX(jframe.getX());
				mainWindowPreferences.setY(jframe.getY());
			}
		});

		return jframe;
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> new Main());
	}

}
