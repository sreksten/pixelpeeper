package com.threeamigos.imageviewer;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import com.threeamigos.common.util.implementations.CompositeMessageHandler;
import com.threeamigos.common.util.implementations.ConsoleMessageHandler;
import com.threeamigos.common.util.implementations.SwingMessageHandler;
import com.threeamigos.common.util.interfaces.MessageHandler;
import com.threeamigos.common.util.preferences.filebased.implementations.RootPathProviderImpl;
import com.threeamigos.common.util.preferences.filebased.interfaces.RootPathProvider;
import com.threeamigos.imageviewer.implementations.datamodel.CommonTagsHelperImpl;
import com.threeamigos.imageviewer.implementations.datamodel.DataModelImpl;
import com.threeamigos.imageviewer.implementations.datamodel.ExifImageReaderImpl;
import com.threeamigos.imageviewer.implementations.datamodel.ExifReaderFactoryImpl;
import com.threeamigos.imageviewer.implementations.datamodel.ImageReaderFactoryImpl;
import com.threeamigos.imageviewer.implementations.datamodel.ImageSlicesManagerImpl;
import com.threeamigos.imageviewer.implementations.edgedetect.EdgesDetectorFactoryImpl;
import com.threeamigos.imageviewer.implementations.edgedetect.ui.EdgesDetectorPreferencesSelectorFactoryImpl;
import com.threeamigos.imageviewer.implementations.persister.PreferencesHelper;
import com.threeamigos.imageviewer.implementations.preferences.flavours.BigPointerPreferencesImpl;
import com.threeamigos.imageviewer.implementations.preferences.flavours.CannyEdgesDetectorPreferencesImpl;
import com.threeamigos.imageviewer.implementations.preferences.flavours.DragAndDropWindowPreferencesImpl;
import com.threeamigos.imageviewer.implementations.preferences.flavours.EdgesDetectorPreferencesImpl;
import com.threeamigos.imageviewer.implementations.preferences.flavours.ExifTagPreferencesImpl;
import com.threeamigos.imageviewer.implementations.preferences.flavours.GridPreferencesImpl;
import com.threeamigos.imageviewer.implementations.preferences.flavours.ImageHandlingPreferencesImpl;
import com.threeamigos.imageviewer.implementations.preferences.flavours.MainWindowPreferencesImpl;
import com.threeamigos.imageviewer.implementations.preferences.flavours.PathPreferencesImpl;
import com.threeamigos.imageviewer.implementations.preferences.flavours.RomyJonaEdgesDetectorPreferencesImpl;
import com.threeamigos.imageviewer.implementations.ui.AboutWindowImpl;
import com.threeamigos.imageviewer.implementations.ui.ChainedInputConsumer;
import com.threeamigos.imageviewer.implementations.ui.CursorManagerImpl;
import com.threeamigos.imageviewer.implementations.ui.DragAndDropWindowImpl;
import com.threeamigos.imageviewer.implementations.ui.ExifTagsFilterImpl;
import com.threeamigos.imageviewer.implementations.ui.FileSelectorImpl;
import com.threeamigos.imageviewer.implementations.ui.FontServiceImpl;
import com.threeamigos.imageviewer.implementations.ui.MouseTrackerImpl;
import com.threeamigos.imageviewer.implementations.ui.imagedecorators.GridDecorator;
import com.threeamigos.imageviewer.interfaces.datamodel.CommonTagsHelper;
import com.threeamigos.imageviewer.interfaces.datamodel.DataModel;
import com.threeamigos.imageviewer.interfaces.datamodel.ExifImageReader;
import com.threeamigos.imageviewer.interfaces.datamodel.ExifReaderFactory;
import com.threeamigos.imageviewer.interfaces.datamodel.ImageReaderFactory;
import com.threeamigos.imageviewer.interfaces.datamodel.ImageSlicesManager;
import com.threeamigos.imageviewer.interfaces.edgedetect.EdgesDetectorFactory;
import com.threeamigos.imageviewer.interfaces.edgedetect.ui.EdgesDetectorPreferencesSelectorFactory;
import com.threeamigos.imageviewer.interfaces.persister.Persistable;
import com.threeamigos.imageviewer.interfaces.preferences.flavours.CannyEdgesDetectorPreferences;
import com.threeamigos.imageviewer.interfaces.preferences.flavours.DragAndDropWindowPreferences;
import com.threeamigos.imageviewer.interfaces.preferences.flavours.ExifTagPreferences;
import com.threeamigos.imageviewer.interfaces.preferences.flavours.GridPreferences;
import com.threeamigos.imageviewer.interfaces.preferences.flavours.ImageHandlingPreferences;
import com.threeamigos.imageviewer.interfaces.preferences.flavours.MainWindowPreferences;
import com.threeamigos.imageviewer.interfaces.preferences.flavours.PathPreferences;
import com.threeamigos.imageviewer.interfaces.preferences.flavours.PropertyChangeAwareBigPointerPreferences;
import com.threeamigos.imageviewer.interfaces.preferences.flavours.PropertyChangeAwareEdgesDetectorPreferences;
import com.threeamigos.imageviewer.interfaces.preferences.flavours.RomyJonaEdgesDetectorPreferences;
import com.threeamigos.imageviewer.interfaces.ui.CursorManager;
import com.threeamigos.imageviewer.interfaces.ui.DragAndDropWindow;
import com.threeamigos.imageviewer.interfaces.ui.ExifTagsFilter;
import com.threeamigos.imageviewer.interfaces.ui.FileSelector;
import com.threeamigos.imageviewer.interfaces.ui.FontService;
import com.threeamigos.imageviewer.interfaces.ui.ImageDecorator;
import com.threeamigos.imageviewer.interfaces.ui.MouseTracker;

/**
 * Uses the Metadata Extractor from https://drewnoakes.com/code/exif/
 *
 * @author Stefano Reksten
 *
 */

public class Main {

	// BUGFIX: empty messages if preferences files are empty/not valid

	// TODO highlight function

	// TODO: the browse directory should show how many files match the selection

	// TODO: image grouping

	// TODO: prepare the image with edges instead of drawing it every time ?

	// TODO: ImageReader should use also other image libraries

	// TODO: set up an unique queue for message processing?

	// TODO: when the edges preference window is changed to a non-dialog window, the
	// menu should be switched off (or the window itself should be shut down and
	// called once again)

	// TODO: drag and drop window with an image instead of text (or both)

	// TODO: lens manufacturer

	public Main() {

		// A way to show error/warning messages to the user

		MessageHandler messageHandler = new CompositeMessageHandler(new SwingMessageHandler(),
				new ConsoleMessageHandler());

		// Preferences that can be stored and retrieved in a subsequent run

		RootPathProvider rootPathProvider = new RootPathProviderImpl(this, messageHandler);
		if (rootPathProvider.shouldAbort()) {
			System.exit(0);
		}

		PreferencesHelper preferencesHelper = new PreferencesHelper(rootPathProvider, messageHandler);

		// Main Preferences

		MainWindowPreferences mainWindowPreferences = new MainWindowPreferencesImpl();
		preferencesHelper.register(mainWindowPreferences, "main_window.preferences");

		DragAndDropWindowPreferences dragAndDropWindowPreferences = new DragAndDropWindowPreferencesImpl();
		preferencesHelper.register(dragAndDropWindowPreferences, "drag_and_drop_window.preferences");

		ImageHandlingPreferences imageHandlingPreferences = new ImageHandlingPreferencesImpl();
		preferencesHelper.register(imageHandlingPreferences, "image_handling.preferences");

		PathPreferences pathPreferences = new PathPreferencesImpl();
		preferencesHelper.register(pathPreferences, "path.preferences");

		ExifTagPreferences exifTagPreferences = new ExifTagPreferencesImpl();
		preferencesHelper.register(exifTagPreferences, "exif_tag.preferences");

		// Decorators preferences

		GridPreferences gridPreferences = new GridPreferencesImpl();
		preferencesHelper.register(gridPreferences, "grid.preferences");

		PropertyChangeAwareBigPointerPreferences bigPointerPreferences = new BigPointerPreferencesImpl();
		preferencesHelper.register(bigPointerPreferences, "pointer.preferences");

		// Edges Detector and implementations preferences

		PropertyChangeAwareEdgesDetectorPreferences edgesDetectorPreferences = new EdgesDetectorPreferencesImpl();
		preferencesHelper.register(edgesDetectorPreferences, "edges_detector.preferences");

		CannyEdgesDetectorPreferences cannyEdgesDetectorPreferences = new CannyEdgesDetectorPreferencesImpl();
		preferencesHelper.register(cannyEdgesDetectorPreferences, "canny_edges_detector.preferences");

		RomyJonaEdgesDetectorPreferences romyJonaEdgesDetectorPreferences = new RomyJonaEdgesDetectorPreferencesImpl();
		preferencesHelper.register(romyJonaEdgesDetectorPreferences, "romy_jona_edge_detector.preferences");

		// Data model

		ImageReaderFactory imageReaderFactory = new ImageReaderFactoryImpl(imageHandlingPreferences);

		ExifReaderFactory exifReaderFactory = new ExifReaderFactoryImpl(imageHandlingPreferences);

		EdgesDetectorFactory edgesDetectorFactory = new EdgesDetectorFactoryImpl(edgesDetectorPreferences,
				cannyEdgesDetectorPreferences, romyJonaEdgesDetectorPreferences);

		ExifImageReader exifImageReader = new ExifImageReaderImpl(imageHandlingPreferences, imageReaderFactory,
				exifReaderFactory, edgesDetectorPreferences, edgesDetectorFactory, messageHandler);

		CommonTagsHelper commonTagsHelper = new CommonTagsHelperImpl();

		ChainedInputConsumer chainedInputConsumer = new ChainedInputConsumer();

		FontService fontService = new FontServiceImpl();

		ImageSlicesManager imageSlicesManager = new ImageSlicesManagerImpl(commonTagsHelper, exifTagPreferences,
				imageHandlingPreferences, edgesDetectorPreferences, fontService);

		ExifTagsFilter exifTagsFilter = new ExifTagsFilterImpl();

		DataModel dataModel = new DataModelImpl(exifTagsFilter, commonTagsHelper, imageSlicesManager,
				imageHandlingPreferences, pathPreferences, edgesDetectorPreferences, exifImageReader);
		chainedInputConsumer.addConsumer(dataModel.getInputConsumer(), ChainedInputConsumer.PRIORITY_LOW);

		// User Interface

		EdgesDetectorPreferencesSelectorFactory edgesDetectorParametersSelectorFactory = new EdgesDetectorPreferencesSelectorFactoryImpl(
				edgesDetectorPreferences, cannyEdgesDetectorPreferences, romyJonaEdgesDetectorPreferences, dataModel,
				exifImageReader, messageHandler);

		FileSelector fileSelector = new FileSelectorImpl(pathPreferences);

		MouseTracker mouseTracker = new MouseTrackerImpl(dataModel);

		DragAndDropWindow dragAndDropWindow = new DragAndDropWindowImpl(dragAndDropWindowPreferences, fontService,
				messageHandler);

		Collection<ImageDecorator> decorators = new ArrayList<>();

		GridDecorator gridDecorator = new GridDecorator(mainWindowPreferences, gridPreferences);
		chainedInputConsumer.addConsumer(gridDecorator.getInputConsumer(), ChainedInputConsumer.PRIORITY_MEDIUM);
		decorators.add(gridDecorator);

		CursorManager cursorManager = new CursorManagerImpl(bigPointerPreferences);
		chainedInputConsumer.addConsumer(cursorManager.getInputConsumer(), ChainedInputConsumer.PRIORITY_HIGH);
		bigPointerPreferences.addPropertyChangeListener(cursorManager);

		ImageViewerCanvas imageViewerCanvas = new ImageViewerCanvas(mainWindowPreferences, dragAndDropWindowPreferences,
				imageHandlingPreferences, gridPreferences, bigPointerPreferences, exifTagPreferences, dataModel,
				preferencesHelper, mouseTracker, cursorManager, fileSelector, edgesDetectorPreferences,
				edgesDetectorParametersSelectorFactory, chainedInputConsumer, decorators, new AboutWindowImpl(),
				dragAndDropWindow, messageHandler);

		cursorManager.addPropertyChangeListener(imageViewerCanvas);
		gridDecorator.addPropertyChangeListener(imageViewerCanvas);

		JMenuBar menuBar = new JMenuBar();
		imageViewerCanvas.addMenus(menuBar);

		JFrame jframe = prepareFrame(menuBar, imageViewerCanvas, mainWindowPreferences, preferencesHelper);

		jframe.setVisible(true);
	}

	private JFrame prepareFrame(JMenuBar menuBar, ImageViewerCanvas canvas, MainWindowPreferences mainWindowPreferences,
			Persistable persistable) {

		JFrame jframe = new JFrame("3AM Image Viewer");
		jframe.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

		jframe.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				persistable.persist();
				System.exit(0);
			}
		});

		jframe.setJMenuBar(menuBar);

		Container container = jframe.getContentPane();
		container.setPreferredSize(new Dimension(canvas.getWidth(), canvas.getHeight()));
		jframe.add(canvas, BorderLayout.CENTER);

		jframe.pack();
		jframe.setResizable(true);
		jframe.setLocation(mainWindowPreferences.getX(), mainWindowPreferences.getY());

		jframe.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				canvas.reframe();
				mainWindowPreferences.setWidth(canvas.getWidth());
				mainWindowPreferences.setHeight(canvas.getHeight());
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
