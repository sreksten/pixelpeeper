package com.threeamigos.imageviewer;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
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
import com.threeamigos.imageviewer.implementations.ui.ChainedInputAdapter;
import com.threeamigos.imageviewer.implementations.ui.PrioritizedInputAdapter;
import com.threeamigos.imageviewer.interfaces.datamodel.CommunicationMessages;
import com.threeamigos.imageviewer.interfaces.datamodel.DataModel;
import com.threeamigos.imageviewer.interfaces.edgedetect.EdgesDetectorFlavour;
import com.threeamigos.imageviewer.interfaces.edgedetect.ui.EdgesDetectorPreferencesSelectorFactory;
import com.threeamigos.imageviewer.interfaces.persister.PersistableHelper;
import com.threeamigos.imageviewer.interfaces.preferences.Preferences;
import com.threeamigos.imageviewer.interfaces.preferences.PreferencesManager;
import com.threeamigos.imageviewer.interfaces.preferences.flavours.BigPointerPreferences;
import com.threeamigos.imageviewer.interfaces.preferences.flavours.EdgesDetectorPreferences;
import com.threeamigos.imageviewer.interfaces.preferences.flavours.ExifTagPreferences;
import com.threeamigos.imageviewer.interfaces.preferences.flavours.GridPreferences;
import com.threeamigos.imageviewer.interfaces.preferences.flavours.PropertyChangeAwareEdgesDetectorPreferences;
import com.threeamigos.imageviewer.interfaces.preferences.flavours.WindowPreferences;
import com.threeamigos.imageviewer.interfaces.ui.AboutWindow;
import com.threeamigos.imageviewer.interfaces.ui.DragAndDropWindow;
import com.threeamigos.imageviewer.interfaces.ui.FileSelector;
import com.threeamigos.imageviewer.interfaces.ui.ImageDecorator;
import com.threeamigos.imageviewer.interfaces.ui.MouseTracker;

/**
 * The canvas on which we draw the various image slices
 *
 * @author Stefano Reksten
 *
 */
public class ImageViewerCanvas extends JPanel implements Consumer<List<File>>, PropertyChangeListener {

	private static final long serialVersionUID = 1L;

	private final transient WindowPreferences windowPreferences;
	private final transient GridPreferences gridPreferences;
	private final transient BigPointerPreferences bigPointerPreferences;
	private final transient ExifTagPreferences exifTagPreferences;
	private final transient DataModel dataModel;
	private final transient PersistableHelper<PreferencesManager<? extends Preferences>> preferencesPersisterHelper;
	private final transient MouseTracker mouseTracker;
	private final transient FileSelector fileSelector;
	private final transient EdgesDetectorPreferences edgesDetectorPreferences;
	private final transient EdgesDetectorPreferencesSelectorFactory edgesDetectorPreferencesSelectorFactory;
	private final transient ChainedInputAdapter chainedInputAdapter;
	private final transient Collection<ImageDecorator> decorators;
	private final transient AboutWindow aboutWindow;
	private final transient DragAndDropWindow dragAndDropWindow;
	private final transient MessageHandler messageHandler;

	private final Cursor emptyCursor;

	private boolean showHelp = false;
	private JMenuItem showEdgesMenuItem;

	private Map<ExifTag, JMenu> exifTagMenusByTag = new EnumMap<>(ExifTag.class);
	private Map<EdgesDetectorFlavour, JMenuItem> edgesDetectorFlavourMenuItemsByFlavour = new EnumMap<>(
			EdgesDetectorFlavour.class);
	private Map<Integer, JMenuItem> gridSpacingBySize = new HashMap<>();
	private Map<Integer, JMenuItem> bigPointerBySize = new HashMap<>();

	public ImageViewerCanvas(WindowPreferences windowPreferences, GridPreferences gridPreferences,
			BigPointerPreferences bigPointerPreferences, ExifTagPreferences exifTagPreferences, DataModel dataModel,
			PersistableHelper<PreferencesManager<? extends Preferences>> preferencesPersisterHelper,
			MouseTracker mouseTracker, FileSelector fileSelector,
			PropertyChangeAwareEdgesDetectorPreferences edgesDetectorPreferences,
			EdgesDetectorPreferencesSelectorFactory edgesDetectorPreferencesSelectorFactory,
			ChainedInputAdapter chainedInputAdapter, Collection<ImageDecorator> decorators, AboutWindow aboutWindow,
			DragAndDropWindow dragAndDropWindow, MessageHandler messageHandler) {
		super();
		this.windowPreferences = windowPreferences;
		this.gridPreferences = gridPreferences;
		this.bigPointerPreferences = bigPointerPreferences;
		this.exifTagPreferences = exifTagPreferences;
		this.dataModel = dataModel;
		dataModel.addPropertyChangeListener(this);
		this.preferencesPersisterHelper = preferencesPersisterHelper;
		this.mouseTracker = mouseTracker;
		this.fileSelector = fileSelector;
		this.edgesDetectorPreferences = edgesDetectorPreferences;
		edgesDetectorPreferences.addPropertyChangeListener(this);
		this.edgesDetectorPreferencesSelectorFactory = edgesDetectorPreferencesSelectorFactory;
		this.chainedInputAdapter = chainedInputAdapter;
		this.decorators = decorators;
		this.aboutWindow = aboutWindow;
		this.dragAndDropWindow = dragAndDropWindow;
		dragAndDropWindow.setProxyFor(this);
		this.messageHandler = messageHandler;

		setSize(windowPreferences.getMainWindowWidth(), windowPreferences.getMainWindowHeight());
		setMinimumSize(getSize());

		setBackground(Color.LIGHT_GRAY);
		setFocusable(true);
		setDoubleBuffered(true);

		chainedInputAdapter.addAdapter(getInputAdapter());
		addMouseListener(chainedInputAdapter);
		addMouseMotionListener(chainedInputAdapter);
		addKeyListener(chainedInputAdapter);

		emptyCursor = createInvisibleCursor();
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
			windowPreferences.setDragAndDropWindowVisible(true);
			dragAndDropWindow.setVisible(true);
		});
		addCheckboxMenuItem(fileMenu, "Show help", KeyEvent.VK_H, showHelp, event -> {
			showHelp = !showHelp;
			repaint();
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
		addCheckboxMenuItem(imageHandlingMenu, "Auto rotation", KeyEvent.VK_I, dataModel.isAutorotation(), event -> {
			dataModel.toggleAutorotation();
			repaint();
		});
		addCheckboxMenuItem(imageHandlingMenu, "Move all images", KeyEvent.VK_M,
				dataModel.isMovementAppliedToAllImages(), event -> {
					dataModel.toggleMovementAppliedToAllImages();
					repaint();
				});
		addCheckboxMenuItem(imageHandlingMenu, "Show grid", KeyEvent.VK_M, gridPreferences.isGridVisible(), event -> {
			gridPreferences.setGridVisible(!gridPreferences.isGridVisible());
			repaint();
		});
		JMenu gridSpacingMenu = new JMenu("Grid spacing");
		imageHandlingMenu.add(gridSpacingMenu);
		for (int gridSpacing = 25; gridSpacing <= 200; gridSpacing += 25) {
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
					updateCursor();
					repaint();
				});
		JMenu bigPointerSizeMenu = new JMenu("Big pointer size");
		imageHandlingMenu.add(bigPointerSizeMenu);
		for (int pointerSize = 25; pointerSize <= 200; pointerSize += 25) {
			final int currentSize = pointerSize;
			JMenuItem pointerSizeItem = addCheckboxMenuItem(bigPointerSizeMenu, String.valueOf(pointerSize), -1,
					pointerSize == bigPointerPreferences.getBigPointerSize(), event -> {
						bigPointerPreferences.setBigPointerSize(currentSize);
						updateBigPointerSizeMenu(currentSize);
						repaint();
					});
			bigPointerBySize.put(pointerSize, pointerSizeItem);
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
		if (bigPointerPreferences.isBigPointerVisible()) {
			setCursor(emptyCursor);
		} else {
			setCursor(Cursor.getDefaultCursor());
		}
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
		} else if (CommunicationMessages.REQUEST_REPAINT.equals(evt.getPropertyName())) {
			repaint();
		}
	}

	private void handleEdgesVisibilityChange() {
		showEdgesMenuItem.setSelected(edgesDetectorPreferences.isShowEdges());
	}

	private Cursor createInvisibleCursor() {
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		Image image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
		return toolkit.createCustomCursor(image, new Point(0, 0), "invisibleCursor");
	}

	private PrioritizedInputAdapter getInputAdapter() {

		return new PrioritizedInputAdapter() {

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
				updateCursor();
				if (dataModel.hasLoadedImages()) {
					mouseTracker.mouseReleased(e);
					repaint();
				}
			}

			@Override
			public void mouseMoved(MouseEvent e) {
				mouseTracker.mouseMoved(e);
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
				if (e.getKeyCode() == KeyEvent.VK_CONTROL) {
					dataModel.setMovementAppliedToAllImagesTemporarilyInverted(true);
				} else if (e.getKeyCode() == KeyEvent.VK_NUMPAD1) {
					bigPointerPreferences.setBigPointerRotation((float) (3 * Math.PI / 4));
				} else if (e.getKeyCode() == KeyEvent.VK_NUMPAD2) {
					bigPointerPreferences.setBigPointerRotation((float) (Math.PI / 2));
				} else if (e.getKeyCode() == KeyEvent.VK_NUMPAD3) {
					bigPointerPreferences.setBigPointerRotation((float) (Math.PI / 4));
				} else if (e.getKeyCode() == KeyEvent.VK_NUMPAD4) {
					bigPointerPreferences.setBigPointerRotation((float) (Math.PI));
				} else if (e.getKeyCode() == KeyEvent.VK_NUMPAD6) {
					bigPointerPreferences.setBigPointerRotation(0);
				} else if (e.getKeyCode() == KeyEvent.VK_NUMPAD7) {
					bigPointerPreferences.setBigPointerRotation((float) (5 * Math.PI / 4));
				} else if (e.getKeyCode() == KeyEvent.VK_NUMPAD8) {
					bigPointerPreferences.setBigPointerRotation((float) (6 * Math.PI / 4));
				} else if (e.getKeyCode() == KeyEvent.VK_NUMPAD9) {
					bigPointerPreferences.setBigPointerRotation((float) (7 * Math.PI / 4));
				}
				repaint();
			}

			@Override
			public void keyReleased(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_CONTROL) {
					dataModel.setMovementAppliedToAllImagesTemporarilyInverted(false);
				}
			}

		};

	}

}
