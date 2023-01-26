package com.threeamigos.imageviewer;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
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
import com.threeamigos.imageviewer.interfaces.preferences.flavours.PropertyChangeAwareEdgesDetectorPreferences;
import com.threeamigos.imageviewer.interfaces.ui.AboutWindow;
import com.threeamigos.imageviewer.interfaces.ui.CursorManager;
import com.threeamigos.imageviewer.interfaces.ui.DragAndDropWindow;
import com.threeamigos.imageviewer.interfaces.ui.FileSelector;
import com.threeamigos.imageviewer.interfaces.ui.HintsWindow;
import com.threeamigos.imageviewer.interfaces.ui.ImageDecorator;
import com.threeamigos.imageviewer.interfaces.ui.InputConsumer;
import com.threeamigos.imageviewer.interfaces.ui.MouseTracker;

/**
 * The canvas on which we draw the various image slices
 *
 * @author Stefano Reksten
 *
 */
public class ImageViewerCanvas extends JPanel implements Consumer<List<File>>, PropertyChangeListener {

	private static final long serialVersionUID = 1L;

	private final transient MainWindowPreferences mainWindowPreferences;
	private final transient DragAndDropWindowPreferences dragAndDropWindowPreferences;
	private final transient ImageHandlingPreferences imageHandlingPreferences;
	private final transient GridPreferences gridPreferences;
	private final transient BigPointerPreferences bigPointerPreferences;
	private final transient ExifTagPreferences exifTagPreferences;
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
	private Map<Integer, JMenuItem> zoomByLevel = new HashMap<>();

	public ImageViewerCanvas(MainWindowPreferences mainWindowPreferences,
			DragAndDropWindowPreferences dragAndDropWindowPreferences,
			ImageHandlingPreferences imageHandlingPreferences, GridPreferences gridPreferences,
			BigPointerPreferences bigPointerPreferences, ExifTagPreferences exifTagPreferences, DataModel dataModel,
			Persistable preferencesPersisterHelper, MouseTracker mouseTracker, CursorManager cursorManager,
			FileSelector fileSelector, PropertyChangeAwareEdgesDetectorPreferences edgesDetectorPreferences,
			EdgesDetectorPreferencesSelectorFactory edgesDetectorPreferencesSelectorFactory,
			ChainedInputConsumer chainedInputAdapter, Collection<ImageDecorator> decorators, AboutWindow aboutWindow,
			HintsWindow hintsWindow, DragAndDropWindow dragAndDropWindow, MessageHandler messageHandler) {
		super();
		this.mainWindowPreferences = mainWindowPreferences;
		this.dragAndDropWindowPreferences = dragAndDropWindowPreferences;
		this.imageHandlingPreferences = imageHandlingPreferences;
		this.gridPreferences = gridPreferences;
		this.bigPointerPreferences = bigPointerPreferences;
		this.exifTagPreferences = exifTagPreferences;
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
		dragAndDropWindow.setProxyFor(this);

		setSize(mainWindowPreferences.getWidth(), mainWindowPreferences.getHeight());
		setMinimumSize(getSize());

		setBackground(Color.LIGHT_GRAY);
		setFocusable(true);
		setDoubleBuffered(true);

		chainedInputAdapter.addConsumer(getInputConsumer(), 0);
		addMouseListener(chainedInputAdapter);
		addMouseMotionListener(chainedInputAdapter);
		addKeyListener(chainedInputAdapter);

		updateCursor();

		DragAndDropSupportHelper.addJavaFileListSupport(this, messageHandler);
	}

	public void addMenus(JMenuBar menuBar) {
		JMenu fileMenu = new JMenu("File");
		menuBar.add(fileMenu);
		addMenuItem(fileMenu, "Open File...", KeyEvent.VK_O, event -> {
			accept(fileSelector.getSelectedFiles(this));
		});
		addMenuItem(fileMenu, "Browse directory", KeyEvent.VK_D, event -> {
			File directory = fileSelector.getSelectedDirectory(this);
			if (directory != null) {
				dataModel.browseDirectory(directory);
				dataModel.reframe(getWidth(), getHeight());
				repaint();
			}
		});
		addMenuItem(fileMenu, "Open Drag and Drop panel", KeyEvent.VK_D, event -> {
			dragAndDropWindowPreferences.setVisible(true);
			dragAndDropWindow.setVisible(true);
		});
		addMenuItem(fileMenu, "Show hints", KeyEvent.VK_H, event -> {
			hintsWindow.showHints(this);
		});

		addMenuItem(fileMenu, "About", KeyEvent.VK_S, event -> aboutWindow.about(this));

		addMenuItem(fileMenu, "Quit", KeyEvent.VK_Q, event -> {
			preferencesPersisterHelper.persist();
			System.exit(0);
		});

		JMenu edgesDetectorMenu = new JMenu("Edges Detector");
		menuBar.add(edgesDetectorMenu);
		showEdgesMenuItem = addCheckboxMenuItem(edgesDetectorMenu, "Show edges", KeyEvent.VK_M,
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
		addMenuItem(edgesDetectorMenu, "Edge Detector parameters", KeyEvent.VK_C, event -> {
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
		addCheckboxMenuItem(imageHandlingMenu, "Auto rotation", KeyEvent.VK_A, dataModel.isAutorotation(), event -> {
			dataModel.toggleAutorotation();
			repaint();
		});
		addCheckboxMenuItem(imageHandlingMenu, "Movement in percentage", KeyEvent.VK_I,
				imageHandlingPreferences.isMovementInPercentage(), event -> {
					imageHandlingPreferences
							.setMovementInPercentage(!imageHandlingPreferences.isMovementInPercentage());
				});
		addCheckboxMenuItem(imageHandlingMenu, "Move all images", KeyEvent.VK_M,
				dataModel.isMovementAppliedToAllImages(), event -> {
					dataModel.toggleMovementAppliedToAllImages();
					repaint();
				});
		miniatureVisibleMenuItem = addCheckboxMenuItem(imageHandlingMenu, "Show position", KeyEvent.VK_P,
				imageHandlingPreferences.isPositionMiniatureVisible(), event -> {
					imageHandlingPreferences
							.setPositionMiniatureVisible(!imageHandlingPreferences.isPositionMiniatureVisible());
					repaint();
				});
		gridVisibleMenuItem = addCheckboxMenuItem(imageHandlingMenu, "Show grid", KeyEvent.VK_M,
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
		addCheckboxMenuItem(imageHandlingMenu, "Show big pointer", KeyEvent.VK_M,
				bigPointerPreferences.isBigPointerVisible(), event -> {
					bigPointerPreferences.setBigPointerVisible(!bigPointerPreferences.isBigPointerVisible());
				});
		JMenu bigPointerSizeMenu = new JMenu("Big pointer size");
		imageHandlingMenu.add(bigPointerSizeMenu);
		int maxDimension = cursorManager.getMaxCursorSize();
		for (int pointerSize = 32; pointerSize <= maxDimension; pointerSize += 16) {
			final int currentSize = pointerSize;
			JMenuItem pointerSizeItem = addCheckboxMenuItem(bigPointerSizeMenu, String.valueOf(pointerSize), -1,
					pointerSize - 1 == bigPointerPreferences.getBigPointerSize(), event -> {
						bigPointerPreferences.setBigPointerSize(currentSize - 1);
						updateBigPointerSizeMenu(currentSize);
					});
			bigPointerBySize.put(pointerSize, pointerSizeItem);
		}
		JMenu zoomMenu = new JMenu("Zoom level");
		imageHandlingMenu.add(zoomMenu);
		for (int zoomLevel = ImageHandlingPreferences.MIN_ZOOM_LEVEL; zoomLevel <= ImageHandlingPreferences.MAX_ZOOM_LEVEL; zoomLevel += 10) {
			final int currentZoom = zoomLevel;
			JMenuItem zoomLevelItem = addCheckboxMenuItem(zoomMenu, String.valueOf(zoomLevel), -1,
					zoomLevel == imageHandlingPreferences.getZoomLevel(), event -> {
						imageHandlingPreferences.setZoomLevel(currentZoom);
						dataModel.changeZoomLevel();
						updateZoomLevelMenu(currentZoom);
						repaint();
					});
			zoomByLevel.put(zoomLevel, zoomLevelItem);
		}

		JMenu tagsMenu = new JMenu("Tags");
		menuBar.add(tagsMenu);
		addCheckboxMenuItem(tagsMenu, "Show tags", KeyEvent.VK_I, exifTagPreferences.isTagsVisible(), event -> {
			exifTagPreferences.setTagsVisible(!exifTagPreferences.isTagsVisible());
			repaint();
		});
		addCheckboxMenuItem(tagsMenu, "overriding visibility", KeyEvent.VK_I,
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

	private void updateZoomLevelMenu(final int zoomLevel) {
		for (Map.Entry<Integer, JMenuItem> entry : zoomByLevel.entrySet()) {
			entry.getValue().setSelected(entry.getKey() == zoomLevel);
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
		Graphics2D g2d = (Graphics2D) gfx;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		dataModel.repaint(g2d);
		for (ImageDecorator decorator : decorators) {
			decorator.paint(g2d);
		}
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (CommunicationMessages.EDGES_VISIBILITY.equals(evt.getPropertyName())) {
			handleEdgesVisibilityChange();
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
		} else if (CommunicationMessages.REQUEST_REPAINT.equals(evt.getPropertyName())) {
			repaint();
		}
	}

	private void handleEdgesVisibilityChange() {
		showEdgesMenuItem.setSelected(edgesDetectorPreferences.isShowEdges());
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

		};

	}

}
