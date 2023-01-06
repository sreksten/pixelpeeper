package com.threeamigos.imageviewer;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.io.File;
import java.util.List;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

import com.threeamigos.imageviewer.data.ExifAndImageReader;
import com.threeamigos.imageviewer.data.ExifTag;
import com.threeamigos.imageviewer.data.PictureData;
import com.threeamigos.imageviewer.interfaces.ui.AboutWindow;
import com.threeamigos.imageviewer.interfaces.ui.CleanupHelper;
import com.threeamigos.imageviewer.interfaces.ui.ExifTagPreferences;
import com.threeamigos.imageviewer.interfaces.ui.FileSelector;
import com.threeamigos.imageviewer.interfaces.ui.ImageSlice;
import com.threeamigos.imageviewer.interfaces.ui.ImageSlicesManager;
import com.threeamigos.imageviewer.interfaces.ui.MouseTracker;
import com.threeamigos.imageviewer.interfaces.ui.ScreenOffsetTracker;
import com.threeamigos.imageviewer.interfaces.ui.WindowPreferences;

/**
 * The canvas on which we draw the various image slices
 *
 * @author Stefano Reksten
 *
 */
public class ImageViewerCanvas extends JPanel {

	private static final long serialVersionUID = 1L;

	private final transient ScreenOffsetTracker screenOffsetTracker;
	private final transient ImageSlicesManager slicesManager;
	private final transient ExifTagPreferences tagPreferences;
	private final transient WindowPreferences windowPreferences;
	private final transient CleanupHelper cleanupHelper;
	private final transient FileSelector fileSelector;
	private final transient AboutWindow aboutWindow;

	private boolean showHelp = false;

	public ImageViewerCanvas(MouseTracker mouseTracker, ScreenOffsetTracker screenOffsetTracker,
			ImageSlicesManager slicesManager, ExifTagPreferences tagPreferences, WindowPreferences windowPreferences,
			CleanupHelper cleanupHelper, FileSelector fileSelector, AboutWindow aboutWindow) {
		super();
		this.screenOffsetTracker = screenOffsetTracker;
		this.slicesManager = slicesManager;
		this.tagPreferences = tagPreferences;
		this.windowPreferences = windowPreferences;
		this.cleanupHelper = cleanupHelper;
		this.fileSelector = fileSelector;
		this.aboutWindow = aboutWindow;

		int width = windowPreferences.getWidth();
		int height = windowPreferences.getHeight();

		setSize(width, height);
		setMinimumSize(getSize());

		setBackground(Color.LIGHT_GRAY);
		setFocusable(true);
		setDoubleBuffered(true);

		addMouseListener(new MouseAdapter() {

			@Override
			public void mousePressed(MouseEvent e) {
				if (!slicesManager.isEmpty()) {
					setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
					mouseTracker.mousePressed(e, slicesManager.findSlice(e.getX(), e.getY()));
					repaint();
				}
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				setCursor(Cursor.getDefaultCursor());
				if (!slicesManager.isEmpty()) {
					mouseTracker.mouseReleased(e);
					repaint();
				}
			}

		});

		addMouseMotionListener(new MouseMotionAdapter() {

			@Override
			public void mouseDragged(MouseEvent e) {
				if (!slicesManager.isEmpty()) {
					mouseTracker.mouseDragged(e);
					repaint();
				}
			}

		});
	}

	public void addMenus(JMenuBar menuBar) {
		JMenu fileMenu = new JMenu("File");
		menuBar.add(fileMenu);
		addMenuItem(fileMenu, "Open", KeyEvent.VK_O, event -> load());
		addCheckboxMenuItem(fileMenu, "Auto rotation", KeyEvent.VK_I, windowPreferences.isAutorotation(), event -> {
			boolean autorotation = !windowPreferences.isAutorotation();
			windowPreferences.setAutorotation(autorotation);
			for (ImageSlice slice : slicesManager.getImageSlices()) {
				slice.adjustRotation(autorotation);
			}
			repaint();
		});
		addCheckboxMenuItem(fileMenu, "Show tags", KeyEvent.VK_I, tagPreferences.isTagsVisible(), event -> {
			tagPreferences.setTagsVisible(!tagPreferences.isTagsVisible());
			repaint();
		});
		addCheckboxMenuItem(fileMenu, "Show help", KeyEvent.VK_H, showHelp, event -> {
			showHelp = !showHelp;
			repaint();
		});

		fileMenu.addSeparator();
		addMenuItem(fileMenu, "About", KeyEvent.VK_S, event -> aboutWindow.about(this));

		fileMenu.addSeparator();
		addMenuItem(fileMenu, "Quit", KeyEvent.VK_Q, event -> cleanupHelper.cleanUpAndExit());

		JMenu tagsMenu = new JMenu("Tags");
		menuBar.add(tagsMenu);
		for (ExifTag exifTag : ExifTag.values()) {
			addCheckboxMenuItem(tagsMenu, exifTag.getDescription(), -1, tagPreferences.isTagVisible(exifTag), event -> {
				tagPreferences.toggle(exifTag);
				repaint();
			});
		}
	}

	private JMenuItem addCheckboxMenuItem(JMenu menu, String title, int mnemonic, boolean initialValue,
			ActionListener actionListener) {
		JMenuItem menuItem = new JCheckBoxMenuItem(title);
		if (actionListener != null) {
			menuItem.addActionListener(actionListener);
		}
		if (mnemonic != -1) {
			menuItem.setMnemonic(mnemonic);
		}
		menuItem.setSelected(initialValue);
		menu.add(menuItem);
		return menuItem;
	}

	private JMenuItem addMenuItem(JMenu menu, String title, int mnemonic, ActionListener actionListener) {
		JMenuItem menuItem = new JMenuItem(title);
		if (actionListener != null) {
			menuItem.addActionListener(actionListener);
		}
		if (mnemonic != -1) {
			menuItem.setMnemonic(mnemonic);
		}
		menu.add(menuItem);
		return menuItem;
	}

	public void reframe() {
		int width = getParent().getWidth();
		int height = getParent().getHeight();
		setSize(width, height);
		slicesManager.reframeImageSlices(width, height);
		repaint();
	}

	@Override
	public void paintComponent(Graphics gfx) {
		super.paintComponent(gfx);
		Graphics2D graphics = (Graphics2D) gfx;
		graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		for (ImageSlice slice : slicesManager.getImageSlices()) {
			slice.paint(graphics);
		}
	}

	private void load() {

		List<File> selectedFiles = fileSelector.getSelectedFiles(this);

		if (!selectedFiles.isEmpty()) {
			slicesManager.clear();
			for (File file : selectedFiles) {
				ExifAndImageReader reader = new ExifAndImageReader(windowPreferences);
				if (reader.readImage(file)) {
					PictureData imageData = reader.getPictureData();
					ImageSlice slice = slicesManager.createImageSlice(imageData);
					slicesManager.add(slice);
				}
			}
			slicesManager.reframeImageSlices(getWidth(), getHeight());
			screenOffsetTracker.reset();
			repaint();
		}
	}
}
