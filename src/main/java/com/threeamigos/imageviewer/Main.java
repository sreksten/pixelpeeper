package com.threeamigos.imageviewer;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import com.threeamigos.common.util.implementations.CompositeMessageHandler;
import com.threeamigos.common.util.implementations.ConsoleMessageHandler;
import com.threeamigos.common.util.implementations.SwingMessageHandler;
import com.threeamigos.common.util.interfaces.MessageHandler;
import com.threeamigos.common.util.preferences.filebased.implementations.PreferencesRootPathProviderImpl;
import com.threeamigos.common.util.preferences.filebased.interfaces.PreferencesRootPathProvider;
import com.threeamigos.imageviewer.implementations.datamodel.CommonTagsHelperImpl;
import com.threeamigos.imageviewer.implementations.datamodel.DataModelImpl;
import com.threeamigos.imageviewer.implementations.datamodel.EdgesDetectorFactoryImpl;
import com.threeamigos.imageviewer.implementations.datamodel.ExifImageReaderImpl;
import com.threeamigos.imageviewer.implementations.datamodel.ImageSlicesManagerImpl;
import com.threeamigos.imageviewer.implementations.persister.FileBasedCannyEdgesDetectorPreferencesPersister;
import com.threeamigos.imageviewer.implementations.persister.FileBasedEdgesDetectorPreferencesPersister;
import com.threeamigos.imageviewer.implementations.persister.FileBasedExifTagPreferencesPersister;
import com.threeamigos.imageviewer.implementations.persister.FileBasedPathPreferencesPersister;
import com.threeamigos.imageviewer.implementations.persister.FileBasedRomyJonaEdgesDetectorPreferencesPersister;
import com.threeamigos.imageviewer.implementations.persister.FileBasedWindowPreferencesPersister;
import com.threeamigos.imageviewer.implementations.persister.PreferencesPersisterHelperImpl;
import com.threeamigos.imageviewer.implementations.preferences.CannyEdgesDetectorPreferencesImpl;
import com.threeamigos.imageviewer.implementations.preferences.EdgesDetectorPreferencesImpl;
import com.threeamigos.imageviewer.implementations.preferences.ExifTagPreferencesImpl;
import com.threeamigos.imageviewer.implementations.preferences.PathPreferencesImpl;
import com.threeamigos.imageviewer.implementations.preferences.RomyJonaEdgesDetectorPreferencesImpl;
import com.threeamigos.imageviewer.implementations.preferences.WindowPreferencesImpl;
import com.threeamigos.imageviewer.implementations.ui.AboutWindowImpl;
import com.threeamigos.imageviewer.implementations.ui.DragAndDropWindowImpl;
import com.threeamigos.imageviewer.implementations.ui.EdgesDetectorPreferencesSelectorFactoryImpl;
import com.threeamigos.imageviewer.implementations.ui.ExifTagsFilterImpl;
import com.threeamigos.imageviewer.implementations.ui.FileSelectorImpl;
import com.threeamigos.imageviewer.implementations.ui.FontServiceImpl;
import com.threeamigos.imageviewer.implementations.ui.MouseTrackerImpl;
import com.threeamigos.imageviewer.interfaces.datamodel.CommonTagsHelper;
import com.threeamigos.imageviewer.interfaces.datamodel.DataModel;
import com.threeamigos.imageviewer.interfaces.datamodel.EdgesDetectorFactory;
import com.threeamigos.imageviewer.interfaces.datamodel.ExifImageReader;
import com.threeamigos.imageviewer.interfaces.datamodel.ImageSlicesManager;
import com.threeamigos.imageviewer.interfaces.preferences.CannyEdgesDetectorPreferences;
import com.threeamigos.imageviewer.interfaces.preferences.ExifTagPreferences;
import com.threeamigos.imageviewer.interfaces.preferences.PathPreferences;
import com.threeamigos.imageviewer.interfaces.preferences.PreferencesPersisterHelper;
import com.threeamigos.imageviewer.interfaces.preferences.PropertyChangeAwareEdgesDetectorPreferences;
import com.threeamigos.imageviewer.interfaces.preferences.RomyJonaEdgesDetectorPreferences;
import com.threeamigos.imageviewer.interfaces.preferences.WindowPreferences;
import com.threeamigos.imageviewer.interfaces.ui.DragAndDropWindow;
import com.threeamigos.imageviewer.interfaces.ui.EdgesDetectorPreferencesSelectorFactory;
import com.threeamigos.imageviewer.interfaces.ui.ExifTagsFilter;
import com.threeamigos.imageviewer.interfaces.ui.FileSelector;
import com.threeamigos.imageviewer.interfaces.ui.FontService;
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

	// TODO: when toggling autorotation calculation should be stopped and restarted

	// TODO: when the edges preference window is changed to a non-dialog window, the
	// menu should be
	// switched off (or the window itself should be shut down and called once again)

	public Main() {

		// A way to show error/warning messages to the user

		MessageHandler messageHandler = new CompositeMessageHandler(new SwingMessageHandler(),
				new ConsoleMessageHandler());

		// Preferences that can be stored and retrieved in a subsequent run

		PreferencesRootPathProvider preferencesRootPathProvider = new PreferencesRootPathProviderImpl(this,
				messageHandler);
		if (preferencesRootPathProvider.shouldAbort()) {
			System.exit(0);
		}

		WindowPreferences windowPreferences = new WindowPreferencesImpl(
				new FileBasedWindowPreferencesPersister(preferencesRootPathProvider, messageHandler), messageHandler);

		PathPreferences pathPreferences = new PathPreferencesImpl(
				new FileBasedPathPreferencesPersister(preferencesRootPathProvider, messageHandler), messageHandler);

		ExifTagPreferences exifTagPreferences = new ExifTagPreferencesImpl(
				new FileBasedExifTagPreferencesPersister(preferencesRootPathProvider, messageHandler), messageHandler);

		PropertyChangeAwareEdgesDetectorPreferences edgesDetectorPreferences = new EdgesDetectorPreferencesImpl(
				new FileBasedEdgesDetectorPreferencesPersister(preferencesRootPathProvider, messageHandler),
				messageHandler);

		CannyEdgesDetectorPreferences cannyEdgesDetectorPreferences = new CannyEdgesDetectorPreferencesImpl(
				new FileBasedCannyEdgesDetectorPreferencesPersister(preferencesRootPathProvider, messageHandler),
				messageHandler);

		RomyJonaEdgesDetectorPreferences romyJonaEdgesDetectorPreferences = new RomyJonaEdgesDetectorPreferencesImpl(
				new FileBasedRomyJonaEdgesDetectorPreferencesPersister(preferencesRootPathProvider, messageHandler),
				messageHandler);

		PreferencesPersisterHelper preferencesPersisterHelper = new PreferencesPersisterHelperImpl(windowPreferences,
				pathPreferences, exifTagPreferences, edgesDetectorPreferences, cannyEdgesDetectorPreferences,
				romyJonaEdgesDetectorPreferences);

		// Data model

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

		// User Interface

		EdgesDetectorPreferencesSelectorFactory edgesDetectorParametersSelectorFactory = new EdgesDetectorPreferencesSelectorFactoryImpl(
				edgesDetectorPreferences, cannyEdgesDetectorPreferences, romyJonaEdgesDetectorPreferences, dataModel,
				exifImageReader, messageHandler);

		FileSelector fileSelector = new FileSelectorImpl(pathPreferences);

		MouseTracker mouseTracker = new MouseTrackerImpl(dataModel);

		DragAndDropWindow dragAndDropWindow = new DragAndDropWindowImpl(windowPreferences, messageHandler);

		ImageViewerCanvas imageViewerCanvas = new ImageViewerCanvas(windowPreferences, exifTagPreferences, dataModel,
				preferencesPersisterHelper, mouseTracker, fileSelector, edgesDetectorPreferences,
				edgesDetectorParametersSelectorFactory, new AboutWindowImpl(), dragAndDropWindow, messageHandler);

		JMenuBar menuBar = new JMenuBar();
		imageViewerCanvas.addMenus(menuBar);

		JFrame jframe = prepareFrame(menuBar, imageViewerCanvas, windowPreferences, preferencesPersisterHelper);

		jframe.setVisible(true);

	}

	private JFrame prepareFrame(JMenuBar menuBar, ImageViewerCanvas canvas, WindowPreferences windowPreferences,
			PreferencesPersisterHelper preferencesPersisterHelper) {

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
