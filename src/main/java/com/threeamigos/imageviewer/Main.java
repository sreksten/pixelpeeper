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
import com.threeamigos.common.util.preferences.filebased.implementations.PreferencesRootPathProviderImpl;
import com.threeamigos.common.util.preferences.filebased.interfaces.RootPathProvider;
import com.threeamigos.imageviewer.implementations.datamodel.CommonTagsHelperImpl;
import com.threeamigos.imageviewer.implementations.datamodel.DataModelImpl;
import com.threeamigos.imageviewer.implementations.datamodel.ExifImageReaderImpl;
import com.threeamigos.imageviewer.implementations.datamodel.ImageSlicesManagerImpl;
import com.threeamigos.imageviewer.implementations.edgedetect.EdgesDetectorFactoryImpl;
import com.threeamigos.imageviewer.implementations.edgedetect.ui.EdgesDetectorPreferencesSelectorFactoryImpl;
import com.threeamigos.imageviewer.implementations.persister.PersistableHelperImpl;
import com.threeamigos.imageviewer.implementations.persister.TextFileBigPointerPreferencesPersister;
import com.threeamigos.imageviewer.implementations.persister.TextFileCannyEdgesDetectorPreferencesPersister;
import com.threeamigos.imageviewer.implementations.persister.TextFileEdgesDetectorPreferencesPersister;
import com.threeamigos.imageviewer.implementations.persister.TextFileExifTagPreferencesPersister;
import com.threeamigos.imageviewer.implementations.persister.TextFileGridPreferencesPersister;
import com.threeamigos.imageviewer.implementations.persister.TextFilePathPreferencesPersister;
import com.threeamigos.imageviewer.implementations.persister.TextFileRomyJonaEdgesDetectorPreferencesPersister;
import com.threeamigos.imageviewer.implementations.persister.TextFileWindowPreferencesPersister;
import com.threeamigos.imageviewer.implementations.preferences.PreferencesManagerImpl;
import com.threeamigos.imageviewer.implementations.preferences.flavours.BigPointerPreferencesImpl;
import com.threeamigos.imageviewer.implementations.preferences.flavours.BigPointerPreferencesStatusTracker;
import com.threeamigos.imageviewer.implementations.preferences.flavours.CannyEdgesDetectorPreferencesImpl;
import com.threeamigos.imageviewer.implementations.preferences.flavours.CannyEdgesDetectorPreferencesStatusTracker;
import com.threeamigos.imageviewer.implementations.preferences.flavours.EdgesDetectorPreferencesImpl;
import com.threeamigos.imageviewer.implementations.preferences.flavours.EdgesDetectorPreferencesStatusTracker;
import com.threeamigos.imageviewer.implementations.preferences.flavours.ExifTagPreferencesImpl;
import com.threeamigos.imageviewer.implementations.preferences.flavours.ExifTagPreferencesStatusTracker;
import com.threeamigos.imageviewer.implementations.preferences.flavours.GridPreferencesImpl;
import com.threeamigos.imageviewer.implementations.preferences.flavours.GridPreferencesStatusTracker;
import com.threeamigos.imageviewer.implementations.preferences.flavours.PathPreferencesImpl;
import com.threeamigos.imageviewer.implementations.preferences.flavours.PathPreferencesStatusTracker;
import com.threeamigos.imageviewer.implementations.preferences.flavours.RomyJonaEdgesDetectorPreferencesImpl;
import com.threeamigos.imageviewer.implementations.preferences.flavours.RomyJonaEdgesDetectorPreferencesStatusTracker;
import com.threeamigos.imageviewer.implementations.preferences.flavours.WindowPreferencesImpl;
import com.threeamigos.imageviewer.implementations.preferences.flavours.WindowPreferencesStatusTracker;
import com.threeamigos.imageviewer.implementations.ui.AboutWindowImpl;
import com.threeamigos.imageviewer.implementations.ui.ChainedInputConsumer;
import com.threeamigos.imageviewer.implementations.ui.DragAndDropWindowImpl;
import com.threeamigos.imageviewer.implementations.ui.ExifTagsFilterImpl;
import com.threeamigos.imageviewer.implementations.ui.FileSelectorImpl;
import com.threeamigos.imageviewer.implementations.ui.FontServiceImpl;
import com.threeamigos.imageviewer.implementations.ui.MouseTrackerImpl;
import com.threeamigos.imageviewer.implementations.ui.imagedecorators.BigPointerDecorator;
import com.threeamigos.imageviewer.implementations.ui.imagedecorators.GridDecorator;
import com.threeamigos.imageviewer.interfaces.StatusTracker;
import com.threeamigos.imageviewer.interfaces.datamodel.CommonTagsHelper;
import com.threeamigos.imageviewer.interfaces.datamodel.DataModel;
import com.threeamigos.imageviewer.interfaces.datamodel.ExifImageReader;
import com.threeamigos.imageviewer.interfaces.datamodel.ImageSlicesManager;
import com.threeamigos.imageviewer.interfaces.edgedetect.EdgesDetectorFactory;
import com.threeamigos.imageviewer.interfaces.edgedetect.ui.EdgesDetectorPreferencesSelectorFactory;
import com.threeamigos.imageviewer.interfaces.persister.PersistableHelper;
import com.threeamigos.imageviewer.interfaces.persister.Persister;
import com.threeamigos.imageviewer.interfaces.preferences.Preferences;
import com.threeamigos.imageviewer.interfaces.preferences.PreferencesManager;
import com.threeamigos.imageviewer.interfaces.preferences.flavours.BigPointerPreferences;
import com.threeamigos.imageviewer.interfaces.preferences.flavours.CannyEdgesDetectorPreferences;
import com.threeamigos.imageviewer.interfaces.preferences.flavours.EdgesDetectorPreferences;
import com.threeamigos.imageviewer.interfaces.preferences.flavours.ExifTagPreferences;
import com.threeamigos.imageviewer.interfaces.preferences.flavours.GridPreferences;
import com.threeamigos.imageviewer.interfaces.preferences.flavours.PathPreferences;
import com.threeamigos.imageviewer.interfaces.preferences.flavours.PropertyChangeAwareEdgesDetectorPreferences;
import com.threeamigos.imageviewer.interfaces.preferences.flavours.RomyJonaEdgesDetectorPreferences;
import com.threeamigos.imageviewer.interfaces.preferences.flavours.WindowPreferences;
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

	// 193-197-200

	// TODO: lens manufacturer

	// TODO: when the edges preference window is changed to a non-dialog window, the
	// menu should be
	// switched off (or the window itself should be shut down and called once again)

	// TODO: ExifImageReader should be split in two separate classes
	// TODO: ImageReader should use java default, apache or other image libraries

	// TODO: set up an unique queue for message processing?

	// BUGFIX: empty messages if preferences files are empty/not valid

	// TODO: prepare the image with edges instead of drawing it every time

	// TODO: scrolling in percentage

	// TODO: miniature showing current position

	// TODO evidenziatore

	// TODO: zoom ?

	public Main() {

		// A way to show error/warning messages to the user

		MessageHandler messageHandler = new CompositeMessageHandler(new SwingMessageHandler(),
				new ConsoleMessageHandler());

		// Preferences that can be stored and retrieved in a subsequent run

		RootPathProvider preferencesRootPathProvider = new PreferencesRootPathProviderImpl(this, messageHandler);
		if (preferencesRootPathProvider.shouldAbort()) {
			System.exit(0);
		}

		PersistableHelper<PreferencesManager<? extends Preferences>> persistableHelper = new PersistableHelperImpl<>();

		// Main Preferences

		WindowPreferences windowPreferences = new WindowPreferencesImpl();
		StatusTracker<WindowPreferences> windowPreferencesStatusTracker = new WindowPreferencesStatusTracker(
				windowPreferences);
		Persister<WindowPreferences> windowPreferencesPersister = new TextFileWindowPreferencesPersister(
				preferencesRootPathProvider, messageHandler);
		PreferencesManager<WindowPreferences> windowPreferencesManager = new PreferencesManagerImpl<>(windowPreferences,
				windowPreferencesStatusTracker, windowPreferencesPersister, messageHandler);
		persistableHelper.add(windowPreferencesManager);

		PathPreferences pathPreferences = new PathPreferencesImpl();
		StatusTracker<PathPreferences> pathPreferencesStatusTracker = new PathPreferencesStatusTracker(pathPreferences);
		Persister<PathPreferences> pathPreferencesPersister = new TextFilePathPreferencesPersister(
				preferencesRootPathProvider, messageHandler);
		PreferencesManager<PathPreferences> pathPreferencesManager = new PreferencesManagerImpl<>(pathPreferences,
				pathPreferencesStatusTracker, pathPreferencesPersister, messageHandler);
		persistableHelper.add(pathPreferencesManager);

		ExifTagPreferences exifTagPreferences = new ExifTagPreferencesImpl();
		StatusTracker<ExifTagPreferences> exifTagPreferencesStatusTracker = new ExifTagPreferencesStatusTracker(
				exifTagPreferences);
		Persister<ExifTagPreferences> exifTagPreferencesPersister = new TextFileExifTagPreferencesPersister(
				preferencesRootPathProvider, messageHandler);
		PreferencesManager<ExifTagPreferences> exifTagPreferencesManager = new PreferencesManagerImpl<>(
				exifTagPreferences, exifTagPreferencesStatusTracker, exifTagPreferencesPersister, messageHandler);
		persistableHelper.add(exifTagPreferencesManager);

		// Decorators preferences

		GridPreferences gridPreferences = new GridPreferencesImpl();
		StatusTracker<GridPreferences> gridPreferencesStatusTracker = new GridPreferencesStatusTracker(gridPreferences);
		Persister<GridPreferences> gridPreferencesPersister = new TextFileGridPreferencesPersister(
				preferencesRootPathProvider, messageHandler);
		PreferencesManager<GridPreferences> gridPreferencesManager = new PreferencesManagerImpl<>(gridPreferences,
				gridPreferencesStatusTracker, gridPreferencesPersister, messageHandler);
		persistableHelper.add(gridPreferencesManager);

		BigPointerPreferences bigPointerPreferences = new BigPointerPreferencesImpl();
		StatusTracker<BigPointerPreferences> bigPointerPreferencesStatusTracker = new BigPointerPreferencesStatusTracker(
				bigPointerPreferences);
		Persister<BigPointerPreferences> bigPointerPreferencesPersister = new TextFileBigPointerPreferencesPersister(
				preferencesRootPathProvider, messageHandler);
		PreferencesManager<BigPointerPreferences> bigPointerPreferencesManager = new PreferencesManagerImpl<>(
				bigPointerPreferences, bigPointerPreferencesStatusTracker, bigPointerPreferencesPersister,
				messageHandler);
		persistableHelper.add(bigPointerPreferencesManager);

		// Edges Detector and implementations preferences

		PropertyChangeAwareEdgesDetectorPreferences edgesDetectorPreferences = new EdgesDetectorPreferencesImpl();
		StatusTracker<EdgesDetectorPreferences> edgesDetectorPreferencesStatusTracker = new EdgesDetectorPreferencesStatusTracker(
				edgesDetectorPreferences);
		Persister<EdgesDetectorPreferences> edgesDetectorPreferencesPersister = new TextFileEdgesDetectorPreferencesPersister(
				preferencesRootPathProvider, messageHandler);
		PreferencesManager<EdgesDetectorPreferences> edgesDetectorPreferencesManager = new PreferencesManagerImpl<>(
				edgesDetectorPreferences, edgesDetectorPreferencesStatusTracker, edgesDetectorPreferencesPersister,
				messageHandler);
		persistableHelper.add(edgesDetectorPreferencesManager);

		CannyEdgesDetectorPreferences cannyEdgesDetectorPreferences = new CannyEdgesDetectorPreferencesImpl();
		StatusTracker<CannyEdgesDetectorPreferences> cannyEdgesDetectorPreferencesStatusTracker = new CannyEdgesDetectorPreferencesStatusTracker(
				cannyEdgesDetectorPreferences);
		Persister<CannyEdgesDetectorPreferences> cannyEdgesDetectorPreferencesPersister = new TextFileCannyEdgesDetectorPreferencesPersister(
				preferencesRootPathProvider, messageHandler);
		PreferencesManager<CannyEdgesDetectorPreferences> cannyEdgesDetectorPreferencesManager = new PreferencesManagerImpl<>(
				cannyEdgesDetectorPreferences, cannyEdgesDetectorPreferencesStatusTracker,
				cannyEdgesDetectorPreferencesPersister, messageHandler);
		persistableHelper.add(cannyEdgesDetectorPreferencesManager);

		RomyJonaEdgesDetectorPreferences romyJonaEdgesDetectorPreferences = new RomyJonaEdgesDetectorPreferencesImpl();
		StatusTracker<RomyJonaEdgesDetectorPreferences> romyJonaEdgesDetectorPreferencesStatusTracker = new RomyJonaEdgesDetectorPreferencesStatusTracker(
				romyJonaEdgesDetectorPreferences);
		Persister<RomyJonaEdgesDetectorPreferences> romyJonaEdgesDetectorPreferencesPersister = new TextFileRomyJonaEdgesDetectorPreferencesPersister(
				preferencesRootPathProvider, messageHandler);
		PreferencesManager<RomyJonaEdgesDetectorPreferences> romyJonaEdgesDetectorPreferencesManager = new PreferencesManagerImpl<>(
				romyJonaEdgesDetectorPreferences, romyJonaEdgesDetectorPreferencesStatusTracker,
				romyJonaEdgesDetectorPreferencesPersister, messageHandler);
		persistableHelper.add(romyJonaEdgesDetectorPreferencesManager);

		// Data model

		ChainedInputConsumer chainedInputConsumer = new ChainedInputConsumer();

		EdgesDetectorFactory edgesDetectorFactory = new EdgesDetectorFactoryImpl(edgesDetectorPreferences,
				cannyEdgesDetectorPreferences, romyJonaEdgesDetectorPreferences);

		FontService fontService = new FontServiceImpl();

		ExifImageReader exifImageReader = new ExifImageReaderImpl(windowPreferences, edgesDetectorFactory,
				messageHandler);

		CommonTagsHelper commonTagsHelper = new CommonTagsHelperImpl();

		ImageSlicesManager imageSlicesManager = new ImageSlicesManagerImpl(commonTagsHelper, exifTagPreferences,
				edgesDetectorPreferences, fontService);

		ExifTagsFilter exifTagsFilter = new ExifTagsFilterImpl();

		DataModel dataModel = new DataModelImpl(exifTagsFilter, commonTagsHelper, imageSlicesManager, windowPreferences,
				pathPreferences, edgesDetectorPreferences, exifImageReader);
		chainedInputConsumer.addConsumer(dataModel.getInputConsumer(), 1);

		// User Interface

		EdgesDetectorPreferencesSelectorFactory edgesDetectorParametersSelectorFactory = new EdgesDetectorPreferencesSelectorFactoryImpl(
				edgesDetectorPreferences, cannyEdgesDetectorPreferences, romyJonaEdgesDetectorPreferences, dataModel,
				exifImageReader, messageHandler);

		FileSelector fileSelector = new FileSelectorImpl(pathPreferences);

		MouseTracker mouseTracker = new MouseTrackerImpl(dataModel);

		DragAndDropWindow dragAndDropWindow = new DragAndDropWindowImpl(windowPreferences, fontService, messageHandler);

		Collection<ImageDecorator> decorators = new ArrayList<>();

		BigPointerDecorator bigPointerDecorator = new BigPointerDecorator(bigPointerPreferences, mouseTracker);
		chainedInputConsumer.addConsumer(bigPointerDecorator.getInputConsumer(), 2);
		decorators.add(bigPointerDecorator);

		GridDecorator gridDecorator = new GridDecorator(windowPreferences, gridPreferences);
		chainedInputConsumer.addConsumer(gridDecorator.getInputConsumer(), 2);
		decorators.add(gridDecorator);

		ImageViewerCanvas imageViewerCanvas = new ImageViewerCanvas(windowPreferences, gridPreferences,
				bigPointerPreferences, exifTagPreferences, dataModel, persistableHelper, mouseTracker, fileSelector,
				edgesDetectorPreferences, edgesDetectorParametersSelectorFactory, chainedInputConsumer, decorators,
				new AboutWindowImpl(), dragAndDropWindow, messageHandler);

		bigPointerDecorator.addPropertyChangeListener(imageViewerCanvas);
		gridDecorator.addPropertyChangeListener(imageViewerCanvas);

		JMenuBar menuBar = new JMenuBar();
		imageViewerCanvas.addMenus(menuBar);

		JFrame jframe = prepareFrame(menuBar, imageViewerCanvas, windowPreferences, persistableHelper);

		jframe.setVisible(true);

	}

	private JFrame prepareFrame(JMenuBar menuBar, ImageViewerCanvas canvas, WindowPreferences windowPreferences,
			PersistableHelper<PreferencesManager<? extends Preferences>> preferencesPersisterHelper) {

		JFrame jframe = new JFrame("3AM Image Viewer");
		jframe.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

		jframe.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				preferencesPersisterHelper.persist();
				System.exit(0);
			}
		});

		jframe.setJMenuBar(menuBar);

		Container container = jframe.getContentPane();
		container.setPreferredSize(new Dimension(canvas.getWidth(), canvas.getHeight()));
		jframe.add(canvas, BorderLayout.CENTER);

		jframe.pack();
		jframe.setResizable(true);
		jframe.setLocation(windowPreferences.getMainWindowX(), windowPreferences.getMainWindowY());

		jframe.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				canvas.reframe();
				windowPreferences.setMainWindowWidth(canvas.getWidth());
				windowPreferences.setMainWindowHeight(canvas.getHeight());
			}

			@Override
			public void componentMoved(ComponentEvent e) {
				windowPreferences.setMainWindowX(jframe.getX());
				windowPreferences.setMainWindowY(jframe.getY());
			}
		});

		return jframe;
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> new Main());
	}

}
