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
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

import com.threeamigos.common.util.interfaces.MessageHandler;
import com.threeamigos.common.util.ui.draganddrop.DragAndDropSupportHelper;
import com.threeamigos.imageviewer.data.ExifTag;
import com.threeamigos.imageviewer.data.ExifTagVisibility;
import com.threeamigos.imageviewer.implementations.ui.ChainedInputConsumer;
import com.threeamigos.imageviewer.implementations.ui.InputAdapter;
import com.threeamigos.imageviewer.implementations.ui.plugins.BigPointerPlugin;
import com.threeamigos.imageviewer.implementations.ui.plugins.EdgesDetectorPlugin;
import com.threeamigos.imageviewer.implementations.ui.plugins.GridPlugin;
import com.threeamigos.imageviewer.implementations.ui.plugins.ImageHandlingPlugin;
import com.threeamigos.imageviewer.interfaces.datamodel.CommunicationMessages;
import com.threeamigos.imageviewer.interfaces.datamodel.DataModel;
import com.threeamigos.imageviewer.interfaces.persister.Persistable;
import com.threeamigos.imageviewer.interfaces.preferences.flavours.BigPointerPreferences;
import com.threeamigos.imageviewer.interfaces.preferences.flavours.DragAndDropWindowPreferences;
import com.threeamigos.imageviewer.interfaces.preferences.flavours.ExifTagPreferences;
import com.threeamigos.imageviewer.interfaces.preferences.flavours.GridPreferences;
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
	private final transient GridPreferences gridPreferences;
	private final transient BigPointerPreferences bigPointerPreferences;
	private final transient ExifTagPreferences exifTagPreferences;
	private final transient DataModel dataModel;
	private final transient CursorManager cursorManager;
	private final transient FileSelector fileSelector;
	private final transient EdgesDetectorPlugin edgesDetectorPlugin;
	private final transient ImageHandlingPlugin imageHandlingPlugin;
	private final transient GridPlugin gridPlugin;
	private final transient BigPointerPlugin bigPointerPlugin;
	private final transient Collection<ImageDecorator> decorators;
	private final transient AboutWindow aboutWindow;
	private final transient HintsWindow hintsWindow;
	private final transient DragAndDropWindow dragAndDropWindow;

	private final JMenuBar menuBar;
	private final Map<String, JMenu> menues = new HashMap<>();

	private Map<ExifTag, JMenu> exifTagMenusByTag = new EnumMap<>(ExifTag.class);

	public ImageViewerCanvas(JMenuBar menuBar, MainWindowPreferences mainWindowPreferences,
			DragAndDropWindowPreferences dragAndDropWindowPreferences,
			ImageHandlingPreferences imageHandlingPreferences, GridPreferences gridPreferences,
			BigPointerPreferences bigPointerPreferences, ExifTagPreferences exifTagPreferences, DataModel dataModel,
			Persistable preferencesPersisterHelper, CursorManager cursorManager, FileSelector fileSelector,
			EdgesDetectorPlugin edgesDetectorPlugin, ImageHandlingPlugin imageHandlingPlugin, GridPlugin gridPlugin,
			BigPointerPlugin bigPointerPlugin, ChainedInputConsumer chainedInputAdapter,
			Collection<ImageDecorator> decorators, AboutWindow aboutWindow, HintsWindow hintsWindow,
			DragAndDropWindow dragAndDropWindow, MessageHandler messageHandler) {
		super();
		this.menuBar = menuBar;
		this.dragAndDropWindowPreferences = dragAndDropWindowPreferences;
		this.imageHandlingPreferences = imageHandlingPreferences;
		this.gridPreferences = gridPreferences;
		this.bigPointerPreferences = bigPointerPreferences;
		this.exifTagPreferences = exifTagPreferences;
		this.dataModel = dataModel;
		this.cursorManager = cursorManager;
		this.fileSelector = fileSelector;
		this.edgesDetectorPlugin = edgesDetectorPlugin;
		this.imageHandlingPlugin = imageHandlingPlugin;
		this.gridPlugin = gridPlugin;
		this.bigPointerPlugin = bigPointerPlugin;
		this.decorators = decorators;
		this.aboutWindow = aboutWindow;
		this.hintsWindow = hintsWindow;
		this.dragAndDropWindow = dragAndDropWindow;
		dragAndDropWindow.setProxyFor(this);

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

		edgesDetectorPlugin.setMainWindow(this);
		imageHandlingPlugin.setMainWindow(this);
		gridPlugin.setMainWindow(this);
		bigPointerPlugin.setMainWindow(this);

		JMenu tagsMenu = new JMenu("Tags");
		menuBar.add(tagsMenu);
		addCheckboxMenuItem(tagsMenu, "Show tags", SHOW_TAGS_KEY, exifTagPreferences.isTagsVisible(), event -> {
			exifTagPreferences.setTagsVisible(!exifTagPreferences.isTagsVisible());
			repaint();
		});
		addCheckboxMenuItem(tagsMenu, "overriding visibility", SHOW_TAGS_OVERRIDING_PREFERENCES_KEY,
				exifTagPreferences.isOverridingTagsVisibility(), event -> {
					exifTagPreferences.setOverridingTagsVisibility(!exifTagPreferences.isOverridingTagsVisibility());
					repaint();
				});
		tagsMenu.addSeparator();
		for (ExifTag exifTag : ExifTag.values()) {
			JMenu exifTagMenu = new JMenu(exifTag.getDescription());
			exifTagMenusByTag.put(exifTag, exifTagMenu);
			tagsMenu.add(exifTagMenu);
			addCheckboxMenuItem(exifTagMenu, ExifTagVisibility.YES.getDescription(), -1,
					exifTagPreferences.getTagVisibility(exifTag) == ExifTagVisibility.YES, event -> {
						exifTagPreferences.setTagVisibility(exifTag, ExifTagVisibility.YES);
						updateExifTagMenu(exifTag);
						repaint();
					});
			addCheckboxMenuItem(exifTagMenu, ExifTagVisibility.ONLY_IF_DIFFERENT.getDescription(), -1,
					exifTagPreferences.getTagVisibility(exifTag) == ExifTagVisibility.ONLY_IF_DIFFERENT, event -> {
						exifTagPreferences.setTagVisibility(exifTag, ExifTagVisibility.ONLY_IF_DIFFERENT);
						updateExifTagMenu(exifTag);
						repaint();
					});
			addCheckboxMenuItem(exifTagMenu, ExifTagVisibility.NO.getDescription(), -1,
					exifTagPreferences.getTagVisibility(exifTag) == ExifTagVisibility.NO, event -> {
						exifTagPreferences.setTagVisibility(exifTag, ExifTagVisibility.NO);
						updateExifTagMenu(exifTag);
						repaint();
					});
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

	private void updateExifTagMenu(ExifTag exifTag) {
		JMenu exifTagMenu = exifTagMenusByTag.get(exifTag);
		Component[] items = exifTagMenu.getMenuComponents();
		ExifTagVisibility exifTagVisibility = exifTagPreferences.getTagVisibility(exifTag);
		for (int i = 0; i < items.length; i++) {
			JCheckBoxMenuItem item = (JCheckBoxMenuItem) items[i];
			item.setSelected(exifTagVisibility.getDescription().equals(item.getText()));
		}
	}

	private JMenuItem addCheckboxMenuItem(JMenu menu, String title, int mnemonic, boolean initialValue,
			ActionListener actionListener) {
		JMenuItem menuItem = new JCheckBoxMenuItem(title);
		if (actionListener != null) {
			menuItem.addActionListener(actionListener);
		}
		if (mnemonic != NO_KEY) {
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

		if (CommunicationMessages.CHANGE_EDGES_VISIBILITY.equals(evt.getPropertyName())) {
			repaint();

		} else if (CommunicationMessages.EDGES_CALCULATION_STARTED.equals(evt.getPropertyName())) {
			repaint();

		} else if (CommunicationMessages.EDGES_CALCULATION_COMPLETED.equals(evt.getPropertyName())) {
			repaint();

		} else if (CommunicationMessages.BIG_POINTER_IMAGE_CHANGED.equals(evt.getPropertyName())) {
			updateCursor();

		} else if (CommunicationMessages.MINIATURE_VISIBILITY_CHANGE.equals(evt.getPropertyName())) {
			updateCursor();

		} else if (CommunicationMessages.GRID_VISIBILITY_CHANGE.equals(evt.getPropertyName())) {
			repaint();

		} else if (CommunicationMessages.GRID_SIZE_CHANGED.equals(evt.getPropertyName())) {
			repaint();

		} else if (CommunicationMessages.ZOOM_LEVEL_CHANGED.equals(evt.getPropertyName())) {
			repaint();

		} else if (CommunicationMessages.DATA_MODEL_CHANGED.equals(evt.getPropertyName())) {
			reframeDataModel();

		} else if (CommunicationMessages.REQUEST_REPAINT.equals(evt.getPropertyName())) {
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
