package com.threeamigos.imageviewer;

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
import com.threeamigos.imageviewer.implementations.datamodel.CannyEdgeDetectorFactoryImpl;
import com.threeamigos.imageviewer.implementations.datamodel.CommonTagsHelperImpl;
import com.threeamigos.imageviewer.implementations.datamodel.DataModelImpl;
import com.threeamigos.imageviewer.implementations.datamodel.ExifImageReaderImpl;
import com.threeamigos.imageviewer.implementations.datamodel.ImageSlicesManagerImpl;
import com.threeamigos.imageviewer.implementations.persister.FileBasedCannyEdgeDetectorPreferencesPersister;
import com.threeamigos.imageviewer.implementations.persister.FileBasedExifTagPreferencesPersister;
import com.threeamigos.imageviewer.implementations.persister.FileBasedPathPreferencesPersister;
import com.threeamigos.imageviewer.implementations.persister.FileBasedWindowPreferencesPersister;
import com.threeamigos.imageviewer.implementations.persister.PreferencesPersisterHelperImpl;
import com.threeamigos.imageviewer.implementations.preferences.CannyEdgeDetectorPreferencesImpl;
import com.threeamigos.imageviewer.implementations.preferences.ExifTagPreferencesImpl;
import com.threeamigos.imageviewer.implementations.preferences.PathPreferencesImpl;
import com.threeamigos.imageviewer.implementations.preferences.WindowPreferencesImpl;
import com.threeamigos.imageviewer.implementations.ui.AboutWindowImpl;
import com.threeamigos.imageviewer.implementations.ui.CannyEdgeDetectorPreferencesSelectorFactoryImpl;
import com.threeamigos.imageviewer.implementations.ui.DragAndDropWindowImpl;
import com.threeamigos.imageviewer.implementations.ui.ExifTagsFilterImpl;
import com.threeamigos.imageviewer.implementations.ui.FileSelectorImpl;
import com.threeamigos.imageviewer.implementations.ui.FontServiceImpl;
import com.threeamigos.imageviewer.implementations.ui.MouseTrackerImpl;
import com.threeamigos.imageviewer.interfaces.datamodel.CannyEdgeDetectorFactory;
import com.threeamigos.imageviewer.interfaces.datamodel.CommonTagsHelper;
import com.threeamigos.imageviewer.interfaces.datamodel.DataModel;
import com.threeamigos.imageviewer.interfaces.datamodel.ExifImageReader;
import com.threeamigos.imageviewer.interfaces.datamodel.ImageSlicesManager;
import com.threeamigos.imageviewer.interfaces.preferences.CannyEdgeDetectorPreferences;
import com.threeamigos.imageviewer.interfaces.preferences.ExifTagPreferences;
import com.threeamigos.imageviewer.interfaces.preferences.PathPreferences;
import com.threeamigos.imageviewer.interfaces.preferences.PreferencesPersisterHelper;
import com.threeamigos.imageviewer.interfaces.preferences.WindowPreferences;
import com.threeamigos.imageviewer.interfaces.ui.CannyEdgeDetectorPreferencesSelectorFactory;
import com.threeamigos.imageviewer.interfaces.ui.DragAndDropWindow;
import com.threeamigos.imageviewer.interfaces.ui.ExifTagsFilter;
import com.threeamigos.imageviewer.interfaces.ui.FileSelector;
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

		CannyEdgeDetectorPreferences cannyEdgeDetectorPreferences = new CannyEdgeDetectorPreferencesImpl(
				new FileBasedCannyEdgeDetectorPreferencesPersister(preferencesRootPathProvider, messageHandler),
				messageHandler);

		PreferencesPersisterHelper preferencesPersisterHelper = new PreferencesPersisterHelperImpl(windowPreferences,
				pathPreferences, exifTagPreferences, cannyEdgeDetectorPreferences);

		// Data model

		CannyEdgeDetectorFactory cannyEdgeDetectorFactory = new CannyEdgeDetectorFactoryImpl(
				cannyEdgeDetectorPreferences);

		ExifImageReader imageReader = new ExifImageReaderImpl(windowPreferences, cannyEdgeDetectorFactory,
				messageHandler);

		CommonTagsHelper commonTagsHelper = new CommonTagsHelperImpl();

		ImageSlicesManager imageSlicesManager = new ImageSlicesManagerImpl(commonTagsHelper, exifTagPreferences,
				cannyEdgeDetectorPreferences, new FontServiceImpl());

		ExifTagsFilter exifTagsFilter = new ExifTagsFilterImpl();

		DataModel dataModel = new DataModelImpl(exifTagsFilter, commonTagsHelper, imageSlicesManager, windowPreferences,
				pathPreferences, cannyEdgeDetectorPreferences, imageReader);

		// User Interface

		CannyEdgeDetectorPreferencesSelectorFactory cannyEdgeDetectorParametersSelectorFactory = new CannyEdgeDetectorPreferencesSelectorFactoryImpl(
				cannyEdgeDetectorPreferences, imageReader, messageHandler);

		FileSelector fileSelector = new FileSelectorImpl(pathPreferences);

		MouseTracker mouseTracker = new MouseTrackerImpl(dataModel);

		DragAndDropWindow dragAndDropWindow = new DragAndDropWindowImpl(windowPreferences, messageHandler);

		ImageViewerCanvas imageViewerCanvas = new ImageViewerCanvas(windowPreferences, exifTagPreferences, dataModel,
				preferencesPersisterHelper, mouseTracker, fileSelector, cannyEdgeDetectorPreferences,
				cannyEdgeDetectorParametersSelectorFactory, new AboutWindowImpl(), dragAndDropWindow, messageHandler);

		JFrame jframe = prepareFrame(imageViewerCanvas, windowPreferences, preferencesPersisterHelper);

		JMenuBar menuBar = new JMenuBar();

		jframe.setJMenuBar(menuBar);

		imageViewerCanvas.addMenus(menuBar);

		jframe.setVisible(true);

	}

	private JFrame prepareFrame(ImageViewerCanvas canvas, WindowPreferences windowPreferences,
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

		jframe.setLayout(null);
		Container container = jframe.getContentPane();
		container.setPreferredSize(new Dimension(canvas.getWidth(), canvas.getHeight()));
		jframe.add(canvas);
		canvas.setLocation(0, 0);

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

		jframe.pack();
		jframe.setResizable(true);
		jframe.setLocation(windowPreferences.getMainWindowX(), windowPreferences.getMainWindowY());

		return jframe;
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> new Main());
	}

}
