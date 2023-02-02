package com.threeamigos.imageviewer;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

import com.threeamigos.common.util.interfaces.MessageHandler;
import com.threeamigos.common.util.ui.draganddrop.DragAndDropSupportHelper;
import com.threeamigos.imageviewer.implementations.ui.ChainedInputConsumer;
import com.threeamigos.imageviewer.implementations.ui.InputAdapter;
import com.threeamigos.imageviewer.interfaces.datamodel.CommunicationMessages;
import com.threeamigos.imageviewer.interfaces.datamodel.DataModel;
import com.threeamigos.imageviewer.interfaces.preferences.flavours.DragAndDropWindowPreferences;
import com.threeamigos.imageviewer.interfaces.preferences.flavours.ImageHandlingPreferences;
import com.threeamigos.imageviewer.interfaces.preferences.flavours.MainWindowPreferences;
import com.threeamigos.imageviewer.interfaces.ui.AboutWindow;
import com.threeamigos.imageviewer.interfaces.ui.CursorManager;
import com.threeamigos.imageviewer.interfaces.ui.DragAndDropWindow;
import com.threeamigos.imageviewer.interfaces.ui.FileSelector;
import com.threeamigos.imageviewer.interfaces.ui.HintsProducer;
import com.threeamigos.imageviewer.interfaces.ui.HintsWindow;
import com.threeamigos.imageviewer.interfaces.ui.ImageDecorator;
import com.threeamigos.imageviewer.interfaces.ui.InputConsumer;
import com.threeamigos.imageviewer.interfaces.ui.KeyRegistry;
import com.threeamigos.imageviewer.interfaces.ui.MainWindow;
import com.threeamigos.imageviewer.interfaces.ui.MainWindowPlugin;

/**
 * The canvas on which we draw the various image slices
 *
 * @author Stefano Reksten
 *
 */
public class ImageViewerCanvas extends JPanel
		implements Consumer<List<File>>, PropertyChangeListener, HintsProducer, KeyRegistry, MainWindow {

	private static final long serialVersionUID = 1L;

	private final transient DragAndDropWindowPreferences dragAndDropWindowPreferences;
	private final transient ImageHandlingPreferences imageHandlingPreferences;
	private final transient DataModel dataModel;
	private final transient CursorManager cursorManager;
	private final transient FileSelector fileSelector;
	private final transient Collection<ImageDecorator> decorators;
	private final transient AboutWindow aboutWindow;
	private final transient HintsWindow hintsWindow;
	private final transient DragAndDropWindow dragAndDropWindow;
	private final transient MainWindowPlugin[] plugins;

	private final JMenuBar menuBar;
	private final Map<String, JMenu> menues = new HashMap<>();

	public ImageViewerCanvas(JMenuBar menuBar, MainWindowPreferences mainWindowPreferences,
			DragAndDropWindowPreferences dragAndDropWindowPreferences,
			ImageHandlingPreferences imageHandlingPreferences, DataModel dataModel, CursorManager cursorManager,
			FileSelector fileSelector, ChainedInputConsumer chainedInputAdapter, Collection<ImageDecorator> decorators,
			AboutWindow aboutWindow, HintsWindow hintsWindow, DragAndDropWindow dragAndDropWindow,
			MessageHandler messageHandler, MainWindowPlugin... plugins) {
		super();
		this.menuBar = menuBar;
		this.dragAndDropWindowPreferences = dragAndDropWindowPreferences;
		this.imageHandlingPreferences = imageHandlingPreferences;
		this.dataModel = dataModel;
		this.cursorManager = cursorManager;
		this.fileSelector = fileSelector;
		this.decorators = decorators;
		this.aboutWindow = aboutWindow;
		this.hintsWindow = hintsWindow;
		this.dragAndDropWindow = dragAndDropWindow;
		dragAndDropWindow.setProxyFor(this);
		this.plugins = plugins;

		setPreferredSize(new Dimension(mainWindowPreferences.getWidth(), mainWindowPreferences.getHeight()));
		setSize(mainWindowPreferences.getWidth(), mainWindowPreferences.getHeight());
		setMinimumSize(new Dimension(800, 600));

		setBackground(Color.LIGHT_GRAY);
		setFocusable(true);
		setDoubleBuffered(true);

		chainedInputAdapter.addConsumer(getInputConsumer(), ChainedInputConsumer.PRIORITY_LOW);
		addMouseListener(chainedInputAdapter);
		addMouseMotionListener(chainedInputAdapter);
		addKeyListener(chainedInputAdapter);

		updateCursor();

		DragAndDropSupportHelper.addJavaFileListSupport(this, messageHandler);

		addMenus();
	}

	private void addMenus() {

		JMenu fileMenu = new JMenu("File");
		menuBar.add(fileMenu);
		addMenuItem(fileMenu, "Open Files...", OPEN_FILES_KEY, event -> openFiles());
		addMenuItem(fileMenu, "Browse directory", BROWSE_DIRECTORY_KEY, event -> browseDirectory());
		addMenuItem(fileMenu, "Open Drag and Drop panel", OPEN_DRAG_AND_DROP_PANEL_KEY,
				event -> openDragAndDropPanel());
		addMenuItem(fileMenu, "Show hints", SHOW_HINTS_KEY, event -> showHints());
		addMenuItem(fileMenu, "About", SHOW_ABOUT_KEY, event -> showAboutWindow());
		addMenuItem(fileMenu, "Quit", QUIT_KEY, event -> quit());

		for (MainWindowPlugin plugin : plugins) {
			plugin.setMainWindow(this);
		}
	}

	private void openFiles() {
		accept(fileSelector.getSelectedFiles(this));
	}

	private void browseDirectory() {
		dataModel.browseDirectory(fileSelector.getSelectedDirectory(this), this);
	}

	private void openDragAndDropPanel() {
		boolean visible = !dragAndDropWindowPreferences.isVisible();
		dragAndDropWindowPreferences.setVisible(visible);
		dragAndDropWindow.setVisible(visible);
	}

	private void showHints() {
		hintsWindow.showHints(this);
	}

	private void showAboutWindow() {
		aboutWindow.about(this);
	}

	private void quit() {
		System.exit(0);
	}

	private void updateCursor() {
		setCursor(cursorManager.getCursor());
	}

	@Override
	public void accept(List<File> selectedFiles) {
		if (!selectedFiles.isEmpty()) {
			dataModel.loadFiles(selectedFiles);
		}
	}

	private JMenuItem addMenuItem(JMenu menu, String title, int mnemonic, ActionListener actionListener) {
		JMenuItem menuItem = new JMenuItem(title);
		if (actionListener != null) {
			menuItem.addActionListener(actionListener);
		}
		if (mnemonic != NO_KEY) {
			menuItem.setMnemonic(mnemonic);
		}
		menu.add(menuItem);
		return menuItem;
	}

	public void reframeDataModel() {
		dataModel.reframe(getWidth(), getHeight());
		repaint();
	}

	@Override
	public void paintComponent(Graphics gfx) {
		super.paintComponent(gfx);
		Graphics2D g2d = (Graphics2D) gfx;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		dataModel.repaint(g2d);
		for (ImageDecorator decorator : decorators) {
			decorator.paint(g2d);
		}
		requestFocus();
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (CommunicationMessages.BIG_POINTER_IMAGE_UPDATE_REQUEST.equals(evt.getPropertyName())) {
			updateCursor();
		} else if (CommunicationMessages.DATA_MODEL_CHANGED.equals(evt.getPropertyName())) {
			reframeDataModel();
		} else {
			repaint();
		}
	}

	private InputConsumer getInputConsumer() {

		return new InputAdapter() {

			@Override
			public void keyPressed(KeyEvent e) {
				int key = e.getKeyCode();
				if (key == OPEN_FILES_KEY) {
					openFiles();
				} else if (key == BROWSE_DIRECTORY_KEY) {
					browseDirectory();
				} else if (key == OPEN_DRAG_AND_DROP_PANEL_KEY) {
					openDragAndDropPanel();
				} else if (key == SHOW_ABOUT_KEY) {
					showAboutWindow();
				} else if (key == QUIT_KEY) {
					quit();
				} else if (key == ENLARGE_KEY) {
					imageHandlingPreferences.setZoomLevel(
							imageHandlingPreferences.getZoomLevel() + ImageHandlingPreferences.ZOOM_LEVEL_STEP);
				} else if (key == REDUCE_KEY) {
					imageHandlingPreferences.setZoomLevel(
							imageHandlingPreferences.getZoomLevel() - ImageHandlingPreferences.ZOOM_LEVEL_STEP);
				}
			}
		};

	}

	@Override
	public Collection<String> getHints() {
		Collection<String> hints = new ArrayList<>();
		hints.add(
				"If no grid is visible you can change the images' zoom level using the plus or minus key on the numeric keypad.");
		return hints;
	}

	@Override
	public JMenu getMenu(String menuTitle) {
		JMenu menu = menues.computeIfAbsent(menuTitle, title -> new JMenu(title));
		menuBar.add(menu);
		return menu;
	}

	@Override
	public Component getComponent() {
		return this;
	}

}
