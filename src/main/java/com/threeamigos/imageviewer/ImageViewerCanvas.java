package com.threeamigos.imageviewer;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
import com.threeamigos.imageviewer.interfaces.datamodel.CommunicationMessages;
import com.threeamigos.imageviewer.interfaces.datamodel.DataModel;
import com.threeamigos.imageviewer.interfaces.edgedetect.EdgesDetectorFlavour;
import com.threeamigos.imageviewer.interfaces.edgedetect.ui.EdgesDetectorPreferencesSelectorFactory;
import com.threeamigos.imageviewer.interfaces.persister.Persistable;
import com.threeamigos.imageviewer.interfaces.preferences.ExifReaderFlavour;
import com.threeamigos.imageviewer.interfaces.preferences.ImageReaderFlavour;
import com.threeamigos.imageviewer.interfaces.preferences.flavours.BigPointerPreferences;
import com.threeamigos.imageviewer.interfaces.preferences.flavours.DragAndDropWindowPreferences;
import com.threeamigos.imageviewer.interfaces.preferences.flavours.EdgesDetectorPreferences;
import com.threeamigos.imageviewer.interfaces.preferences.flavours.ExifTagPreferences;
import com.threeamigos.imageviewer.interfaces.preferences.flavours.GridPreferences;
import com.threeamigos.imageviewer.interfaces.preferences.flavours.ImageHandlingPreferences;
import com.threeamigos.imageviewer.interfaces.preferences.flavours.MainWindowPreferences;
import com.threeamigos.imageviewer.interfaces.preferences.flavours.PathPreferences;
import com.threeamigos.imageviewer.interfaces.preferences.flavours.PropertyChangeAwareEdgesDetectorPreferences;
import com.threeamigos.imageviewer.interfaces.ui.AboutWindow;
import com.threeamigos.imageviewer.interfaces.ui.CursorManager;
import com.threeamigos.imageviewer.interfaces.ui.DragAndDropWindow;
import com.threeamigos.imageviewer.interfaces.ui.ExifTagsFilter;
import com.threeamigos.imageviewer.interfaces.ui.FileSelector;
import com.threeamigos.imageviewer.interfaces.ui.HintsProducer;
import com.threeamigos.imageviewer.interfaces.ui.HintsWindow;
import com.threeamigos.imageviewer.interfaces.ui.ImageDecorator;
import com.threeamigos.imageviewer.interfaces.ui.InputConsumer;
import com.threeamigos.imageviewer.interfaces.ui.KeyRegistry;
import com.threeamigos.imageviewer.interfaces.ui.MouseTracker;

/**
 * The canvas on which we draw the various image slices
 *
 * @author Stefano Reksten
 *
 */
public class ImageViewerCanvas extends JPanel
		implements Consumer<List<File>>, PropertyChangeListener, HintsProducer, KeyRegistry {

	private static final long serialVersionUID = 1L;

	private final transient DragAndDropWindowPreferences dragAndDropWindowPreferences;
	private final transient ImageHandlingPreferences imageHandlingPreferences;
	private final transient GridPreferences gridPreferences;
	private final transient BigPointerPreferences bigPointerPreferences;
	private final transient ExifTagPreferences exifTagPreferences;
	private final transient PathPreferences pathPreferences;
	private final transient ExifTagsFilter exifTagsFilter;
	private final transient DataModel dataModel;
	private final transient Persistable preferencesPersisterHelper;
	private final transient MouseTracker mouseTracker;
	private final transient CursorManager cursorManager;
	private final transient FileSelector fileSelector;
	private final transient EdgesDetectorPreferences edgesDetectorPreferences;
	private final transient EdgesDetectorPreferencesSelectorFactory edgesDetectorPreferencesSelectorFactory;
	private final transient Collection<ImageDecorator> decorators;
	private final transient AboutWindow aboutWindow;
	private final transient HintsWindow hintsWindow;
	private final transient DragAndDropWindow dragAndDropWindow;
	private final transient MessageHandler messageHandler;

	private JMenuItem showEdgesMenuItem;

	private Map<ExifTag, JMenu> exifTagMenusByTag = new EnumMap<>(ExifTag.class);
	private Map<ImageReaderFlavour, JMenuItem> imageReadersByFlavour = new EnumMap<>(ImageReaderFlavour.class);
	private Map<ExifReaderFlavour, JMenuItem> exifReadersByFlavour = new EnumMap<>(ExifReaderFlavour.class);
	private Map<EdgesDetectorFlavour, JMenuItem> edgesDetectorFlavourMenuItemsByFlavour = new EnumMap<>(
			EdgesDetectorFlavour.class);
	private Map<Integer, JMenuItem> gridSpacingBySize = new HashMap<>();
	private JMenuItem gridVisibleMenuItem;
	private JMenuItem miniatureVisibleMenuItem;
	private Map<Integer, JMenuItem> bigPointerBySize = new HashMap<>();

	public ImageViewerCanvas(MainWindowPreferences mainWindowPreferences,
			DragAndDropWindowPreferences dragAndDropWindowPreferences,
			ImageHandlingPreferences imageHandlingPreferences, GridPreferences gridPreferences,
			BigPointerPreferences bigPointerPreferences, ExifTagPreferences exifTagPreferences,
			PathPreferences pathPreferences, ExifTagsFilter exifTagsFilter, DataModel dataModel,
			Persistable preferencesPersisterHelper, MouseTracker mouseTracker, CursorManager cursorManager,
			FileSelector fileSelector, PropertyChangeAwareEdgesDetectorPreferences edgesDetectorPreferences,
			EdgesDetectorPreferencesSelectorFactory edgesDetectorPreferencesSelectorFactory,
			ChainedInputConsumer chainedInputAdapter, Collection<ImageDecorator> decorators, AboutWindow aboutWindow,
			HintsWindow hintsWindow, DragAndDropWindow dragAndDropWindow, MessageHandler messageHandler) {
		super();
		this.dragAndDropWindowPreferences = dragAndDropWindowPreferences;
		this.imageHandlingPreferences = imageHandlingPreferences;
		this.gridPreferences = gridPreferences;
		this.bigPointerPreferences = bigPointerPreferences;
		this.exifTagPreferences = exifTagPreferences;
		this.pathPreferences = pathPreferences;
		this.exifTagsFilter = exifTagsFilter;
		this.dataModel = dataModel;
		dataModel.addPropertyChangeListener(this);
		this.preferencesPersisterHelper = preferencesPersisterHelper;
		this.mouseTracker = mouseTracker;
		this.cursorManager = cursorManager;
		this.fileSelector = fileSelector;
		this.edgesDetectorPreferences = edgesDetectorPreferences;
		edgesDetectorPreferences.addPropertyChangeListener(this);
		this.edgesDetectorPreferencesSelectorFactory = edgesDetectorPreferencesSelectorFactory;
		this.decorators = decorators;
		this.aboutWindow = aboutWindow;
		this.hintsWindow = hintsWindow;
		this.dragAndDropWindow = dragAndDropWindow;
		this.messageHandler = messageHandler;
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
	}

	public void addMenus(JMenuBar menuBar) {
		JMenu fileMenu = new JMenu("File");
		menuBar.add(fileMenu);
		addMenuItem(fileMenu, "Open Files...", OPEN_FILES_KEY, event -> openFiles());
		addMenuItem(fileMenu, "Browse directory", BROWSE_DIRECTORY_KEY, event -> browseDirectory());
		addMenuItem(fileMenu, "Open Drag and Drop panel", OPEN_DRAG_AND_DROP_PANEL_KEY,
				event -> openDragAndDropPanel());
		addMenuItem(fileMenu, "Show hints", SHOW_HINTS_KEY, event -> showHints());
		addMenuItem(fileMenu, "About", SHOW_ABOUT_KEY, event -> showAboutWindow());
		addMenuItem(fileMenu, "Quit", QUIT_KEY, event -> quit());

		JMenu edgesDetectorMenu = new JMenu("Edges Detector");
		menuBar.add(edgesDetectorMenu);
		showEdgesMenuItem = addCheckboxMenuItem(edgesDetectorMenu, "Show edges", SHOW_EDGES_KEY,
				edgesDetectorPreferences.isShowEdges(), event -> {
					dataModel.toggleShowingEdges();
					repaint();
				});
		JMenu edgesDetectorFlavourMenuItem = new JMenu("Flavours");
		edgesDetectorMenu.add(edgesDetectorFlavourMenuItem);
		for (EdgesDetectorFlavour flavour : EdgesDetectorFlavour.values()) {
			JMenuItem flavourMenuItem = addCheckboxMenuItem(edgesDetectorFlavourMenuItem, flavour.getDescription(), -1,
					edgesDetectorPreferences.getEdgesDetectorFlavour() == flavour, event -> {
						updateEdgesDetectorFlavour(flavour);
					});
			edgesDetectorFlavourMenuItemsByFlavour.put(flavour, flavourMenuItem);
		}
		addMenuItem(edgesDetectorMenu, "Edge Detector parameters", SHOW_EDGES_DETETECTOR_PARAMETERS_KEY, event -> {
			edgesDetectorPreferencesSelectorFactory.createSelector(this).selectParameters(this);
		});

		JMenu imageHandlingMenu = new JMenu("Image handling");
		menuBar.add(imageHandlingMenu);
		JMenu imageReaderMenu = new JMenu("Image reader library");
		imageHandlingMenu.add(imageReaderMenu);
		for (ImageReaderFlavour flavour : ImageReaderFlavour.values()) {
			JMenuItem imageReaderItem = addCheckboxMenuItem(imageReaderMenu, flavour.getDescription(), -1,
					flavour == imageHandlingPreferences.getImageReaderFlavour(), event -> {
						imageHandlingPreferences.setImageReaderFlavour(flavour);
						updateImageReaderMenu(flavour);
						repaint();
					});
			imageReadersByFlavour.put(flavour, imageReaderItem);
		}
		JMenu exifReaderMenu = new JMenu("Exif reader library");
		imageHandlingMenu.add(exifReaderMenu);
		for (ExifReaderFlavour flavour : ExifReaderFlavour.values()) {
			JMenuItem exifReaderItem = addCheckboxMenuItem(exifReaderMenu, flavour.getDescription(), -1,
					flavour == imageHandlingPreferences.getExifReaderFlavour(), event -> {
						imageHandlingPreferences.setExifReaderFlavour(flavour);
						updateExifReaderMenu(flavour);
						repaint();
					});
			exifReadersByFlavour.put(flavour, exifReaderItem);
		}
		addCheckboxMenuItem(imageHandlingMenu, "Auto rotation", AUTOROTATION_KEY, dataModel.isAutorotation(), event -> {
			dataModel.toggleAutorotation();
			repaint();
		});
		addCheckboxMenuItem(imageHandlingMenu, "Movement in percentage", MOVEMENT_IN_PERCENTAGE_KEY,
				imageHandlingPreferences.isMovementInPercentage(), event -> {
					imageHandlingPreferences
							.setMovementInPercentage(!imageHandlingPreferences.isMovementInPercentage());
				});
		addCheckboxMenuItem(imageHandlingMenu, "Move all images", MOVE_ALL_IMAGES_KEY,
				dataModel.isMovementAppliedToAllImages(), event -> {
					dataModel.toggleMovementAppliedToAllImages();
					repaint();
				});
		miniatureVisibleMenuItem = addCheckboxMenuItem(imageHandlingMenu, "Show position", SHOW_POSITION_MINIATURE_KEY,
				imageHandlingPreferences.isPositionMiniatureVisible(), event -> {
					imageHandlingPreferences
							.setPositionMiniatureVisible(!imageHandlingPreferences.isPositionMiniatureVisible());
					repaint();
				});
		addCheckboxMenuItem(imageHandlingMenu, "Normalize for crop factor", NORMALIZE_FOR_CROP_FACTOR_KEY,
				imageHandlingPreferences.isNormalizedForCrop(), event -> {
					imageHandlingPreferences.setNormalizedForCrop(!imageHandlingPreferences.isNormalizedForCrop());
					repaint();
				});
		addCheckboxMenuItem(imageHandlingMenu, "Normalize for focal length", NORMALIZE_FOR_FOCAL_LENGTH_KEY,
				imageHandlingPreferences.isNormalizedForFocalLength(), event -> {
					imageHandlingPreferences
							.setNormalizedForFocalLength(!imageHandlingPreferences.isNormalizedForFocalLength());
					repaint();
				});
		gridVisibleMenuItem = addCheckboxMenuItem(imageHandlingMenu, "Show grid", SHOW_GRID_KEY,
				gridPreferences.isGridVisible(), event -> {
					gridPreferences.setGridVisible(!gridPreferences.isGridVisible());
					repaint();
				});
		JMenu gridSpacingMenu = new JMenu("Grid spacing");
		imageHandlingMenu.add(gridSpacingMenu);
		for (int gridSpacing = GridPreferences.GRID_SPACING_MIN; gridSpacing <= GridPreferences.GRID_SPACING_MAX; gridSpacing += GridPreferences.GRID_SPACING_STEP) {
			final int currentSpacing = gridSpacing;
			JMenuItem gridSpacingItem = addCheckboxMenuItem(gridSpacingMenu, String.valueOf(gridSpacing), -1,
					gridSpacing == gridPreferences.getGridSpacing(), event -> {
						gridPreferences.setGridSpacing(currentSpacing);
						updateGridSpacingMenu(currentSpacing);
						repaint();
					});
			gridSpacingBySize.put(gridSpacing, gridSpacingItem);
		}
		addCheckboxMenuItem(imageHandlingMenu, "Show big pointer", SHOW_BIG_POINTER_KEY,
				bigPointerPreferences.isBigPointerVisible(), event -> {
					bigPointerPreferences.setBigPointerVisible(!bigPointerPreferences.isBigPointerVisible());
				});
		JMenu bigPointerSizeMenu = new JMenu("Big pointer size");
		imageHandlingMenu.add(bigPointerSizeMenu);
		int maxDimension = cursorManager.getMaxCursorSize();
		for (int pointerSize = BigPointerPreferences.BIG_POINTER_MIN_SIZE; pointerSize <= maxDimension; pointerSize += BigPointerPreferences.BIG_POINTER_SIZE_STEP) {
			final int currentSize = pointerSize;
			JMenuItem pointerSizeItem = addCheckboxMenuItem(bigPointerSizeMenu, String.valueOf(pointerSize), -1,
					pointerSize - 1 == bigPointerPreferences.getBigPointerSize(), event -> {
						bigPointerPreferences.setBigPointerSize(currentSize - 1);
						updateBigPointerSizeMenu(currentSize);
					});
			bigPointerBySize.put(pointerSize, pointerSizeItem);
		}

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
		File directory = fileSelector.getSelectedDirectory(this);
		if (directory != null) {
			if (directory.isDirectory()) {

				Collection<File> files = findImageFiles(directory);

				Collection<File> filesToLoad = exifTagsFilter.filterByTags(this, files);

				if (!filesToLoad.isEmpty()) {
					pathPreferences.setLastPath(directory.getPath());
					pathPreferences.setTagToGroupBy(exifTagsFilter.getTagToGroupBy());
					dataModel.loadFiles(filesToLoad, exifTagsFilter.getTagToGroupBy(), 0);
					dataModel.reframe(getWidth(), getHeight());
					repaint();
				}
			} else {
				messageHandler.handleErrorMessage("Selected file is not a directory.");
			}
		}
	}

	private void openDragAndDropPanel() {
		dragAndDropWindowPreferences.setVisible(true);
		dragAndDropWindow.setVisible(true);
	}

	private void showHints() {
		hintsWindow.showHints(this);
	}

	private void showAboutWindow() {
		aboutWindow.about(this);
	}

	private void quit() {
		preferencesPersisterHelper.persist();
		System.exit(0);
	}

	private Collection<File> findImageFiles(File directory) {
		Collection<File> files = new ArrayList<>();
		for (File file : directory.listFiles()) {
			if (file.isFile()) {
				files.add(file);
			}
		}
		return files;
	}

	private void updateCursor() {
		setCursor(cursorManager.getCursor());
	}

	@Override
	public void accept(List<File> selectedFiles) {
		if (!selectedFiles.isEmpty()) {
			dataModel.loadFiles(selectedFiles);
			dataModel.reframe(getWidth(), getHeight());
			repaint();
		}
	}

	private void updateEdgesDetectorFlavour(EdgesDetectorFlavour flavour) {
		edgesDetectorPreferences.setEdgesDetectorFlavour(flavour);
		for (Entry<EdgesDetectorFlavour, JMenuItem> entry : edgesDetectorFlavourMenuItemsByFlavour.entrySet()) {
			entry.getValue().setSelected(edgesDetectorPreferences.getEdgesDetectorFlavour() == entry.getKey());
		}
		if (edgesDetectorPreferences.isShowEdges()) {
			dataModel.calculateEdges();
		}
	}

	private void updateImageReaderMenu(final ImageReaderFlavour flavour) {
		for (Map.Entry<ImageReaderFlavour, JMenuItem> entry : imageReadersByFlavour.entrySet()) {
			entry.getValue().setSelected(entry.getKey() == flavour);
		}
	}

	private void updateExifReaderMenu(final ExifReaderFlavour flavour) {
		for (Map.Entry<ExifReaderFlavour, JMenuItem> entry : exifReadersByFlavour.entrySet()) {
			entry.getValue().setSelected(entry.getKey() == flavour);
		}
	}

	private void updateGridSpacingMenu(final int gridSpacing) {
		for (Map.Entry<Integer, JMenuItem> entry : gridSpacingBySize.entrySet()) {
			entry.getValue().setSelected(entry.getKey() == gridSpacing);
		}
	}

	private void updateBigPointerSizeMenu(final int pointerSize) {
		for (Map.Entry<Integer, JMenuItem> entry : bigPointerBySize.entrySet()) {
			entry.getValue().setSelected(entry.getKey() == pointerSize);
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
		if (CommunicationMessages.EDGES_VISIBILITY.equals(evt.getPropertyName())) {
			showEdgesMenuItem.setSelected(edgesDetectorPreferences.isShowEdges());

		} else if (CommunicationMessages.EDGES_CALCULATION_STARTED.equals(evt.getPropertyName())) {
			repaint();

		} else if (CommunicationMessages.EDGES_CALCULATION_COMPLETED.equals(evt.getPropertyName())) {
			repaint();

		} else if (CommunicationMessages.BIG_POINTER_IMAGE_CHANGED.equals(evt.getPropertyName())) {
			updateCursor();

		} else if (CommunicationMessages.MINIATURE_VISIBILITY_CHANGE.equals(evt.getPropertyName())) {
			miniatureVisibleMenuItem.setSelected(imageHandlingPreferences.isPositionMiniatureVisible());
			repaint();

		} else if (CommunicationMessages.GRID_VISIBILITY_CHANGE.equals(evt.getPropertyName())) {
			gridVisibleMenuItem.setSelected(gridPreferences.isGridVisible());
			repaint();

		} else if (CommunicationMessages.GRID_SIZE_CHANGED.equals(evt.getPropertyName())) {
			updateGridSpacingMenu(gridPreferences.getGridSpacing());
			repaint();

		} else if (CommunicationMessages.ZOOM_LEVEL_CHANGED.equals(evt.getPropertyName())) {
			repaint();

		} else if (CommunicationMessages.DATA_MODEL_CHANGED.equals(evt.getPropertyName())) {
			dataModel.reframe(getWidth(), getHeight());
			repaint();

		} else if (CommunicationMessages.REQUEST_REPAINT.equals(evt.getPropertyName())) {
			repaint();
		}
	}

	private InputConsumer getInputConsumer() {

		return new InputAdapter() {

			@Override
			public void mousePressed(MouseEvent e) {
				if (dataModel.hasLoadedImages()) {
					if (!bigPointerPreferences.isBigPointerVisible()) {
						setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
					}
					mouseTracker.mousePressed(e);
					repaint();
				}
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				updateCursor();
				if (dataModel.hasLoadedImages()) {
					mouseTracker.mouseReleased(e);
					repaint();
				}
			}

			@Override
			public void mouseMoved(MouseEvent e) {
				repaint();
			}

			@Override
			public void mouseDragged(MouseEvent e) {
				if (dataModel.hasLoadedImages()) {
					mouseTracker.mouseDragged(e);
					repaint();
				}
			}

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
				}

				if (key == KeyEvent.VK_ADD) {
					int zoomLevel = imageHandlingPreferences.getZoomLevel();
					if (zoomLevel < ImageHandlingPreferences.MAX_ZOOM_LEVEL) {
						imageHandlingPreferences.setZoomLevel(zoomLevel + ImageHandlingPreferences.ZOOM_LEVEL_STEP);
					}
					e.consume();
				} else if (key == KeyEvent.VK_SUBTRACT) {
					int zoomLevel = imageHandlingPreferences.getZoomLevel();
					if (zoomLevel > ImageHandlingPreferences.MIN_ZOOM_LEVEL) {
						imageHandlingPreferences.setZoomLevel(zoomLevel - ImageHandlingPreferences.ZOOM_LEVEL_STEP);
					}
					e.consume();
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

}
