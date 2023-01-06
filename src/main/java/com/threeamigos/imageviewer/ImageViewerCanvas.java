package com.threeamigos.imageviewer;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
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

import com.threeamigos.imageviewer.data.ExifTag;
import com.threeamigos.imageviewer.interfaces.datamodel.DataModel;
import com.threeamigos.imageviewer.interfaces.ui.AboutWindow;
import com.threeamigos.imageviewer.interfaces.ui.FileSelector;
import com.threeamigos.imageviewer.interfaces.ui.MouseTracker;

/**
 * The canvas on which we draw the various image slices
 *
 * @author Stefano Reksten
 *
 */
public class ImageViewerCanvas extends JPanel {

	private static final long serialVersionUID = 1L;

	private final transient DataModel dataModel;
	private final transient FileSelector fileSelector;
	private final transient AboutWindow aboutWindow;

	private boolean showHelp = false;

	public ImageViewerCanvas(DataModel dataModel, MouseTracker mouseTracker, FileSelector fileSelector,
			AboutWindow aboutWindow) {
		super();
		this.dataModel = dataModel;
		this.fileSelector = fileSelector;
		this.aboutWindow = aboutWindow;

		int width = dataModel.getPreferredWidth();
		int height = dataModel.getPreferredHeight();

		setSize(width, height);
		setMinimumSize(getSize());

		setBackground(Color.LIGHT_GRAY);
		setFocusable(true);
		setDoubleBuffered(true);

		addMouseListener(new MouseAdapter() {

			@Override
			public void mousePressed(MouseEvent e) {
				if (dataModel.hasLoadedImages()) {
					setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
					mouseTracker.mousePressed(e);
					repaint();
				}
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				setCursor(Cursor.getDefaultCursor());
				if (dataModel.hasLoadedImages()) {
					mouseTracker.mouseReleased(e);
					repaint();
				}
			}

		});

		addMouseMotionListener(new MouseMotionAdapter() {

			@Override
			public void mouseDragged(MouseEvent e) {
				if (dataModel.hasLoadedImages()) {
					mouseTracker.mouseDragged(e);
					repaint();
				}
			}

		});

		addKeyListener(new KeyAdapter() {

			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_CONTROL) {
					dataModel.setMovementAppliedToAllImagesTemporarilyInverted(true);
				}
			}

			@Override
			public void keyReleased(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_CONTROL) {
					dataModel.setMovementAppliedToAllImagesTemporarilyInverted(false);
				}
			}

		});
	}

	public void addMenus(JMenuBar menuBar) {
		JMenu fileMenu = new JMenu("File");
		menuBar.add(fileMenu);
		addMenuItem(fileMenu, "Open", KeyEvent.VK_O, event -> {
			List<File> selectedFiles = fileSelector.getSelectedFiles(this);
			if (!selectedFiles.isEmpty()) {
				dataModel.loadFiles(selectedFiles);
				dataModel.reframe(getWidth(), getHeight());
				repaint();
			}
		});
		addCheckboxMenuItem(fileMenu, "Auto rotation", KeyEvent.VK_I, dataModel.isAutorotation(), event -> {
			dataModel.toggleAutorotation();
			repaint();
		});
		addCheckboxMenuItem(fileMenu, "Move all images", KeyEvent.VK_M, dataModel.isMovementAppliedToAllImages(),
				event -> {
					dataModel.toggleMovementAppliedToAllImages();
					repaint();
				});
		addCheckboxMenuItem(fileMenu, "Show tags", KeyEvent.VK_I, dataModel.isTagsVisible(), event -> {
			dataModel.toggleTagsVisibility();
			repaint();
		});
		addCheckboxMenuItem(fileMenu, "Show help", KeyEvent.VK_H, showHelp, event -> {
			showHelp = !showHelp;
			repaint();
		});

		fileMenu.addSeparator();
		addMenuItem(fileMenu, "About", KeyEvent.VK_S, event -> aboutWindow.about(this));

		fileMenu.addSeparator();
		addMenuItem(fileMenu, "Quit", KeyEvent.VK_Q, event -> {
			dataModel.savePreferences();
			System.exit(0);
		});

		JMenu tagsMenu = new JMenu("Tags");
		menuBar.add(tagsMenu);
		for (ExifTag exifTag : ExifTag.values()) {
			addCheckboxMenuItem(tagsMenu, exifTag.getDescription(), -1, dataModel.isTagVisible(exifTag), event -> {
				dataModel.toggleTagVisibility(exifTag);
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
		dataModel.reframe(width, height);
		repaint();
	}

	@Override
	public void paintComponent(Graphics gfx) {
		super.paintComponent(gfx);
		Graphics2D graphics = (Graphics2D) gfx;
		graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		dataModel.repaint(graphics);
	}
}
