package com.threeamigos.imageviewer;

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
import com.threeamigos.imageviewer.implementations.datamodel.CropFactorRepositoryImpl;
import com.threeamigos.imageviewer.implementations.datamodel.CropFactorRepositoryManagerImpl;
import com.threeamigos.imageviewer.implementations.datamodel.DataModelImpl;
import com.threeamigos.imageviewer.implementations.datamodel.ExifCacheImpl;
import com.threeamigos.imageviewer.implementations.datamodel.ExifImageReaderImpl;
import com.threeamigos.imageviewer.implementations.datamodel.ExifReaderFactoryImpl;
import com.threeamigos.imageviewer.implementations.datamodel.ImageReaderFactoryImpl;
import com.threeamigos.imageviewer.implementations.datamodel.ImageSlicesManagerImpl;
import com.threeamigos.imageviewer.implementations.datamodel.TagsClassifierImpl;
import com.threeamigos.imageviewer.implementations.edgedetect.EdgesDetectorFactoryImpl;
import com.threeamigos.imageviewer.implementations.edgedetect.ui.EdgesDetectorPreferencesSelectorFactoryImpl;
import com.threeamigos.imageviewer.implementations.persister.PersistablesHelper;
import com.threeamigos.imageviewer.implementations.preferences.flavours.BigPointerPreferencesImpl;
import com.threeamigos.imageviewer.implementations.preferences.flavours.CannyEdgesDetectorPreferencesImpl;
import com.threeamigos.imageviewer.implementations.preferences.flavours.DragAndDropWindowPreferencesImpl;
import com.threeamigos.imageviewer.implementations.preferences.flavours.EdgesDetectorPreferencesImpl;
import com.threeamigos.imageviewer.implementations.preferences.flavours.ExifTagPreferencesImpl;
import com.threeamigos.imageviewer.implementations.preferences.flavours.GridPreferencesImpl;
import com.threeamigos.imageviewer.implementations.preferences.flavours.HintsPreferencesImpl;
import com.threeamigos.imageviewer.implementations.preferences.flavours.ImageHandlingPreferencesImpl;
import com.threeamigos.imageviewer.implementations.preferences.flavours.MainWindowPreferencesImpl;
import com.threeamigos.imageviewer.implementations.preferences.flavours.RomyJonaEdgesDetectorPreferencesImpl;
import com.threeamigos.imageviewer.implementations.preferences.flavours.SessionPreferencesImpl;
import com.threeamigos.imageviewer.implementations.ui.AboutWindowImpl;
import com.threeamigos.imageviewer.implementations.ui.ChainedInputConsumer;
import com.threeamigos.imageviewer.implementations.ui.CropFactorProviderImpl;
import com.threeamigos.imageviewer.implementations.ui.CursorManagerImpl;
import com.threeamigos.imageviewer.implementations.ui.DragAndDropWindowImpl;
import com.threeamigos.imageviewer.implementations.ui.ExifTagsFilterImpl;
import com.threeamigos.imageviewer.implementations.ui.FileSelectorImpl;
import com.threeamigos.imageviewer.implementations.ui.FontServiceImpl;
import com.threeamigos.imageviewer.implementations.ui.HintsCollectorImpl;
import com.threeamigos.imageviewer.implementations.ui.HintsWindowImpl;
import com.threeamigos.imageviewer.implementations.ui.MouseTrackerImpl;
import com.threeamigos.imageviewer.implementations.ui.imagedecorators.GridDecorator;
import com.threeamigos.imageviewer.implementations.ui.plugins.BigPointerPlugin;
import com.threeamigos.imageviewer.implementations.ui.plugins.EdgesDetectorPlugin;
import com.threeamigos.imageviewer.implementations.ui.plugins.ExifTagsPlugin;
import com.threeamigos.imageviewer.implementations.ui.plugins.GridPlugin;
import com.threeamigos.imageviewer.implementations.ui.plugins.ImageHandlingPlugin;
import com.threeamigos.imageviewer.interfaces.datamodel.CropFactorRepository;
import com.threeamigos.imageviewer.interfaces.datamodel.CropFactorRepositoryManager;
import com.threeamigos.imageviewer.interfaces.datamodel.DataModel;
import com.threeamigos.imageviewer.interfaces.datamodel.ExifCache;
import com.threeamigos.imageviewer.interfaces.datamodel.ExifImageReader;
import com.threeamigos.imageviewer.interfaces.datamodel.ExifReaderFactory;
import com.threeamigos.imageviewer.interfaces.datamodel.ImageReaderFactory;
import com.threeamigos.imageviewer.interfaces.datamodel.ImageSlicesManager;
import com.threeamigos.imageviewer.interfaces.datamodel.TagsClassifier;
import com.threeamigos.imageviewer.interfaces.edgedetect.EdgesDetectorFactory;
import com.threeamigos.imageviewer.interfaces.edgedetect.ui.EdgesDetectorPreferencesSelectorFactory;
import com.threeamigos.imageviewer.interfaces.preferences.flavours.BigPointerPreferences;
import com.threeamigos.imageviewer.interfaces.preferences.flavours.CannyEdgesDetectorPreferences;
import com.threeamigos.imageviewer.interfaces.preferences.flavours.DragAndDropWindowPreferences;
import com.threeamigos.imageviewer.interfaces.preferences.flavours.EdgesDetectorPreferences;
import com.threeamigos.imageviewer.interfaces.preferences.flavours.ExifTagPreferences;
import com.threeamigos.imageviewer.interfaces.preferences.flavours.GridPreferences;
import com.threeamigos.imageviewer.interfaces.preferences.flavours.HintsPreferences;
import com.threeamigos.imageviewer.interfaces.preferences.flavours.ImageHandlingPreferences;
import com.threeamigos.imageviewer.interfaces.preferences.flavours.MainWindowPreferences;
import com.threeamigos.imageviewer.interfaces.preferences.flavours.RomyJonaEdgesDetectorPreferences;
import com.threeamigos.imageviewer.interfaces.preferences.flavours.SessionPreferences;
import com.threeamigos.imageviewer.interfaces.ui.CropFactorProvider;
import com.threeamigos.imageviewer.interfaces.ui.CursorManager;
import com.threeamigos.imageviewer.interfaces.ui.DragAndDropWindow;
import com.threeamigos.imageviewer.interfaces.ui.ExifTagsFilter;
import com.threeamigos.imageviewer.interfaces.ui.FileSelector;
import com.threeamigos.imageviewer.interfaces.ui.FontService;
import com.threeamigos.imageviewer.interfaces.ui.HintsCollector;
import com.threeamigos.imageviewer.interfaces.ui.HintsWindow;
import com.threeamigos.imageviewer.interfaces.ui.ImageDecorator;
import com.threeamigos.imageviewer.interfaces.ui.MainWindowPlugin;
import com.threeamigos.imageviewer.interfaces.ui.MouseTracker;

/**
 * Uses the Metadata Extractor from https://drewnoakes.com/code/exif/
 *
 * @author Stefano Reksten
 *
 */

public class Main {

	// BUGFIX: the big pointer may flicker when changed

	// TODO: highlight function

	// TODO: drag and drop window with an image instead of text (or both)

	// TODO: lens manufacturer

	// FOOD-FOR-THOUGHTS: prepare the image with edges instead of drawing it every
	// time?

	// FOOD-FOR-THOUGHTS: should ImageReader use also other image libraries?

	// FOOD-FOR-THOUGHTS: set up an unique queue for message processing?

	public Main() {

		// A way to show error/warning messages to the user

		MessageHandler messageHandler = new CompositeMessageHandler(new SwingMessageHandler(),
				new ConsoleMessageHandler());

		// The directory in which we store preferences and other persistables

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

		ExifTagPreferences exifTagPreferences = new ExifTagPreferencesImpl();
		persistablesHelper.register(exifTagPreferences, "exif_tag.preferences");

		// Decorators preferences

		GridPreferences gridPreferences = new GridPreferencesImpl();
		persistablesHelper.register(gridPreferences, "grid.preferences");

		BigPointerPreferences bigPointerPreferences = new BigPointerPreferencesImpl();
		persistablesHelper.register(bigPointerPreferences, "pointer.preferences");

		// Edges Detector and implementations preferences

		EdgesDetectorPreferences edgesDetectorPreferences = new EdgesDetectorPreferencesImpl();
		persistablesHelper.register(edgesDetectorPreferences, "edges_detector.preferences");

		CannyEdgesDetectorPreferences cannyEdgesDetectorPreferences = new CannyEdgesDetectorPreferencesImpl();
		persistablesHelper.register(cannyEdgesDetectorPreferences, "canny_edges_detector.preferences");

		RomyJonaEdgesDetectorPreferences romyJonaEdgesDetectorPreferences = new RomyJonaEdgesDetectorPreferencesImpl();
		persistablesHelper.register(romyJonaEdgesDetectorPreferences, "romy_jona_edge_detector.preferences");

		// Misc preferences

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

		ExifReaderFactory exifReaderFactory = new ExifReaderFactoryImpl(imageHandlingPreferences);

		ExifCache exifCache = new ExifCacheImpl(exifReaderFactory, cropFactorProvider);

		EdgesDetectorFactory edgesDetectorFactory = new EdgesDetectorFactoryImpl(edgesDetectorPreferences,
				cannyEdgesDetectorPreferences, romyJonaEdgesDetectorPreferences);

		ExifImageReader exifImageReader = new ExifImageReaderImpl(imageHandlingPreferences, imageReaderFactory,
				exifCache, edgesDetectorPreferences, edgesDetectorFactory, messageHandler);

		ExifTagsFilter exifTagsFilter = new ExifTagsFilterImpl(exifCache, messageHandler);

		TagsClassifier tagsClassifier = new TagsClassifierImpl();

		FontService fontService = new FontServiceImpl();

		ImageSlicesManager imageSlicesManager = new ImageSlicesManagerImpl(tagsClassifier, exifTagPreferences,
				imageHandlingPreferences, edgesDetectorPreferences, fontService);

		ChainedInputConsumer chainedInputConsumer = new ChainedInputConsumer();

		HintsCollector hintsCollector = new HintsCollectorImpl();

		DataModel dataModel = new DataModelImpl(tagsClassifier, imageSlicesManager, imageHandlingPreferences,
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

		MouseTracker mouseTracker = new MouseTrackerImpl();
		chainedInputConsumer.addConsumer(mouseTracker.getInputConsumer(), ChainedInputConsumer.PRIORITY_HIGH);
		mouseTracker.addPropertyChangeListener(dataModel);

		DragAndDropWindow dragAndDropWindow = new DragAndDropWindowImpl(dragAndDropWindowPreferences, exifReaderFactory,
				exifCache, fontService, messageHandler);
		hintsCollector.addHints(dragAndDropWindow);

		Collection<ImageDecorator> decorators = new ArrayList<>();

		GridDecorator gridDecorator = new GridDecorator(mainWindowPreferences, gridPreferences);
		decorators.add(gridDecorator);

		CursorManager cursorManager = new CursorManagerImpl(bigPointerPreferences);
		chainedInputConsumer.addConsumer(cursorManager.getInputConsumer(), ChainedInputConsumer.PRIORITY_HIGH);
		bigPointerPreferences.addPropertyChangeListener(cursorManager);
		hintsCollector.addHints(cursorManager);

		HintsWindow hintsWindow = new HintsWindowImpl(hintsPreferences, hintsCollector);

		List<MainWindowPlugin> plugins = new ArrayList<>();

		EdgesDetectorPlugin edgesDetectorPlugin = new EdgesDetectorPlugin(edgesDetectorPreferences,
				edgesDetectorParametersSelectorFactory);
		edgesDetectorPlugin.addPropertyChangeListener(dataModel);
		edgesDetectorPreferences.addPropertyChangeListener(edgesDetectorPlugin);
		plugins.add(edgesDetectorPlugin);

		ImageHandlingPlugin imageHandlingPlugin = new ImageHandlingPlugin(imageHandlingPreferences);
		imageHandlingPlugin.addPropertyChangeListener(dataModel);
		imageHandlingPreferences.addPropertyChangeListener(imageHandlingPlugin);
		plugins.add(imageHandlingPlugin);

		GridPlugin gridPlugin = new GridPlugin(gridPreferences);
		gridPreferences.addPropertyChangeListener(gridPlugin);
		chainedInputConsumer.addConsumer(gridPlugin.getInputConsumer(), ChainedInputConsumer.PRIORITY_MEDIUM);
		hintsCollector.addHints(gridPlugin);
		plugins.add(gridPlugin);

		BigPointerPlugin bigPointerPlugin = new BigPointerPlugin(bigPointerPreferences, cursorManager);
		plugins.add(bigPointerPlugin);

		ExifTagsPlugin exifTagsPlugin = new ExifTagsPlugin(exifTagPreferences);
		plugins.add(exifTagsPlugin);

		JMenuBar menuBar = new JMenuBar();

		ImageViewerCanvas imageViewerCanvas = new ImageViewerCanvas(menuBar, mainWindowPreferences,
				dragAndDropWindowPreferences, imageHandlingPreferences, dataModel, cursorManager, fileSelector,
				chainedInputConsumer, decorators, new AboutWindowImpl(), hintsWindow, dragAndDropWindow, messageHandler,
				plugins);
		hintsCollector.addHints(imageViewerCanvas);
		dataModel.addPropertyChangeListener(imageViewerCanvas);
		imageHandlingPreferences.addPropertyChangeListener(imageViewerCanvas);
		bigPointerPreferences.addPropertyChangeListener(imageViewerCanvas);
		gridPreferences.addPropertyChangeListener(imageViewerCanvas);
		exifTagsPlugin.addPropertyChangeListener(imageViewerCanvas);

		ControlsPanel controlsPanel = new ControlsPanel(imageHandlingPreferences, dataModel);
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

		JFrame jframe = new JFrame("3AM Image Viewer");
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
