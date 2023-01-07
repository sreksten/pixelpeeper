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

import com.threeamigos.imageviewer.implementations.datamodel.CommonTagsHelperImpl;
import com.threeamigos.imageviewer.implementations.datamodel.DataModelImpl;
import com.threeamigos.imageviewer.implementations.datamodel.ImageSlicesManagerImpl;
import com.threeamigos.imageviewer.implementations.persister.ExifTagPreferencesPersisterImpl;
import com.threeamigos.imageviewer.implementations.persister.PathPreferencesPersisterImpl;
import com.threeamigos.imageviewer.implementations.persister.WindowPreferencesPersisterImpl;
import com.threeamigos.imageviewer.implementations.preferences.ExifTagPreferencesImpl;
import com.threeamigos.imageviewer.implementations.preferences.PathPreferencesImpl;
import com.threeamigos.imageviewer.implementations.preferences.WindowPreferencesImpl;
import com.threeamigos.imageviewer.implementations.ui.AboutWindowImpl;
import com.threeamigos.imageviewer.implementations.ui.ExifTagsFilterImpl;
import com.threeamigos.imageviewer.implementations.ui.FileSelectorImpl;
import com.threeamigos.imageviewer.implementations.ui.FontServiceImpl;
import com.threeamigos.imageviewer.implementations.ui.MouseTrackerImpl;
import com.threeamigos.imageviewer.interfaces.datamodel.CommonTagsHelper;
import com.threeamigos.imageviewer.interfaces.datamodel.DataModel;
import com.threeamigos.imageviewer.interfaces.datamodel.ImageSlicesManager;
import com.threeamigos.imageviewer.interfaces.preferences.ExifTagPreferences;
import com.threeamigos.imageviewer.interfaces.preferences.PathPreferences;
import com.threeamigos.imageviewer.interfaces.preferences.WindowPreferences;
import com.threeamigos.imageviewer.interfaces.ui.ExifTagsFilter;
import com.threeamigos.imageviewer.interfaces.ui.FileSelector;
import com.threeamigos.imageviewer.interfaces.ui.MouseTracker;

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

		// --- End preferences

		CommonTagsHelper commonTagsHelper = new CommonTagsHelperImpl();

		ImageSlicesManager imageSlicesManager = new ImageSlicesManagerImpl(commonTagsHelper, tagPreferences,
				new FontServiceImpl());

		ExifTagsFilter exifTagsFilter = new ExifTagsFilterImpl();

		DataModel dataModel = new DataModelImpl(exifTagsFilter, commonTagsHelper, imageSlicesManager, tagPreferences,
				windowPreferences, pathPreferences);

		FileSelector fileSelector = new FileSelectorImpl(dataModel);

		MouseTracker mouseTracker = new MouseTrackerImpl(dataModel);

		ImageViewerCanvas imageViewerCanvas = new ImageViewerCanvas(dataModel, mouseTracker, fileSelector,
				new AboutWindowImpl());

		JFrame jframe = prepareFrame(imageViewerCanvas, dataModel);

		JMenuBar menuBar = new JMenuBar();

		jframe.setJMenuBar(menuBar);

		imageViewerCanvas.addMenus(menuBar);

		jframe.setVisible(true);

	}

	private JFrame prepareFrame(ImageViewerCanvas canvas, DataModel dataModel) {

		JFrame jframe = new JFrame("3AM Image Viewer");
		jframe.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

		jframe.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				dataModel.savePreferences();
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
				dataModel.setPreferredWidth(canvas.getWidth());
				dataModel.setPreferredHeight(canvas.getHeight());
			}

			@Override
			public void componentMoved(ComponentEvent e) {
				dataModel.setPreferredX(jframe.getX());
				dataModel.setPreferredY(jframe.getY());
			}
		});

		jframe.pack();
		jframe.setResizable(true);
		jframe.setLocation(dataModel.getPreferredX(), dataModel.getPreferredY());

		return jframe;
	}

	public static void main(String[] args) {
		new Main();
	}

}
