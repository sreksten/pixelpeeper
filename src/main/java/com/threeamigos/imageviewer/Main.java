package com.threeamigos.imageviewer;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.WindowConstants;

import com.threeamigos.imageviewer.implementations.persister.ExifTagPreferencesPersisterImpl;
import com.threeamigos.imageviewer.implementations.persister.PathPreferencesPersisterImpl;
import com.threeamigos.imageviewer.implementations.persister.WindowPreferencesPersisterImpl;
import com.threeamigos.imageviewer.implementations.ui.AboutWindowImpl;
import com.threeamigos.imageviewer.implementations.ui.CleanupHelperImpl;
import com.threeamigos.imageviewer.implementations.ui.ExifTagPreferencesImpl;
import com.threeamigos.imageviewer.implementations.ui.FileSelectorImpl;
import com.threeamigos.imageviewer.implementations.ui.FontServiceImpl;
import com.threeamigos.imageviewer.implementations.ui.ImageSliceFactoryImpl;
import com.threeamigos.imageviewer.implementations.ui.ImageSlicesManagerImpl;
import com.threeamigos.imageviewer.implementations.ui.MouseTrackerImpl;
import com.threeamigos.imageviewer.implementations.ui.PathPreferencesImpl;
import com.threeamigos.imageviewer.implementations.ui.ScreenOffsetTrackerImpl;
import com.threeamigos.imageviewer.implementations.ui.WindowPreferencesImpl;
import com.threeamigos.imageviewer.interfaces.ui.CleanupHelper;
import com.threeamigos.imageviewer.interfaces.ui.ExifTagPreferences;
import com.threeamigos.imageviewer.interfaces.ui.FileSelector;
import com.threeamigos.imageviewer.interfaces.ui.ImageSlicesManager;
import com.threeamigos.imageviewer.interfaces.ui.MouseTracker;
import com.threeamigos.imageviewer.interfaces.ui.PathPreferences;
import com.threeamigos.imageviewer.interfaces.ui.ScreenOffsetTracker;
import com.threeamigos.imageviewer.interfaces.ui.WindowPreferences;

/**
 * Uses the MEtadata Extractor from https://drewnoakes.com/code/exif/
 *
 * @author Stefano Reksten
 *
 */

public class Main {

	// 193-197-200

	// TODO: lens manufacturer

	public Main() {

		// Preferences that can be stored and retrieved in a subsequent run

		WindowPreferences windowPreferences = new WindowPreferencesImpl(new WindowPreferencesPersisterImpl());

		PathPreferences pathPreferences = new PathPreferencesImpl(new PathPreferencesPersisterImpl());

		ExifTagPreferences tagPreferences = new ExifTagPreferencesImpl(new ExifTagPreferencesPersisterImpl());

		CleanupHelper cleanupHelper = new CleanupHelperImpl();
		cleanupHelper.addPersistable(windowPreferences);
		cleanupHelper.addPersistable(pathPreferences);
		cleanupHelper.addPersistable(tagPreferences);

		// --- End preferences

		ScreenOffsetTracker screenOffsetTracker = new ScreenOffsetTrackerImpl();

		MouseTracker mouseTracker = new MouseTrackerImpl(screenOffsetTracker);

		ImageSlicesManager imageSlicesManager = new ImageSlicesManagerImpl(
				new ImageSliceFactoryImpl(screenOffsetTracker, tagPreferences, new FontServiceImpl()));

		FileSelector fileSelector = new FileSelectorImpl(pathPreferences);

		ImageViewerCanvas imageViewerCanvas = new ImageViewerCanvas(mouseTracker, screenOffsetTracker,
				imageSlicesManager, tagPreferences, windowPreferences, cleanupHelper, fileSelector,
				new AboutWindowImpl());

		JFrame jframe = prepareFrame(imageViewerCanvas, windowPreferences, cleanupHelper);

		imageViewerCanvas.addMenus(prepareMenu(jframe));

		jframe.setVisible(true);

	}

	private JFrame prepareFrame(ImageViewerCanvas canvas, WindowPreferences framePreferences,
			CleanupHelper cleanupHelper) {

		JFrame jframe = new JFrame("3AM Image Viewer");
		jframe.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

		jframe.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				cleanupHelper.cleanUpAndExit();
			}
		});

		jframe.setLayout(null);
		Container c = jframe.getContentPane();
		c.setPreferredSize(new Dimension(canvas.getWidth(), canvas.getHeight()));
		jframe.add(canvas);
		canvas.setLocation(0, 0);

		jframe.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				canvas.reframe();
				framePreferences.setWidth(canvas.getWidth());
				framePreferences.setHeight(canvas.getHeight());
			}

			@Override
			public void componentMoved(ComponentEvent e) {
				framePreferences.setX(jframe.getX());
				framePreferences.setY(jframe.getY());
			}
		});

		jframe.pack();
		jframe.setResizable(true);
		jframe.setLocation(framePreferences.getX(), framePreferences.getY());

		return jframe;
	}

	private JMenuBar prepareMenu(JFrame jframe) {
		JMenuBar menuBar = new JMenuBar();
		jframe.setJMenuBar(menuBar);
		return menuBar;
	}

	public static void main(String[] args) throws Exception {
		new Main();
	}

}
