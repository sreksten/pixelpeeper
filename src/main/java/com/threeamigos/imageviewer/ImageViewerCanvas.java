package com.threeamigos.imageviewer;

import java.awt.Color;
import java.awt.Component;
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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.EnumMap;
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
import com.threeamigos.imageviewer.interfaces.datamodel.CommunicationMessages;
import com.threeamigos.imageviewer.interfaces.datamodel.DataModel;
import com.threeamigos.imageviewer.interfaces.edgedetect.EdgesDetectorFlavour;
import com.threeamigos.imageviewer.interfaces.edgedetect.ui.EdgesDetectorPreferencesSelectorFactory;
import com.threeamigos.imageviewer.interfaces.persister.PersistableHelper;
import com.threeamigos.imageviewer.interfaces.preferences.Preferences;
import com.threeamigos.imageviewer.interfaces.preferences.PreferencesManager;
import com.threeamigos.imageviewer.interfaces.preferences.flavours.EdgesDetectorPreferences;
import com.threeamigos.imageviewer.interfaces.preferences.flavours.ExifTagPreferences;
import com.threeamigos.imageviewer.interfaces.preferences.flavours.PropertyChangeAwareEdgesDetectorPreferences;
import com.threeamigos.imageviewer.interfaces.preferences.flavours.WindowPreferences;
import com.threeamigos.imageviewer.interfaces.ui.AboutWindow;
import com.threeamigos.imageviewer.interfaces.ui.DragAndDropWindow;
import com.threeamigos.imageviewer.interfaces.ui.FileSelector;
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
	private final transient ExifTagPreferences exifTagPreferences;
	private final transient EdgesDetectorPreferences edgesDetectorPreferences;
	private final transient EdgesDetectorPreferencesSelectorFactory edgesDetectorPreferencesSelectorFactory;
	private final transient DataModel dataModel;
	private final transient PersistableHelper<PreferencesManager<? extends Preferences>> preferencesPersisterHelper;
	private final transient FileSelector fileSelector;
	private final transient AboutWindow aboutWindow;
	private final transient DragAndDropWindow dragAndDropWindow;

	private boolean showHelp = false;
	private JMenuItem showEdgesMenuItem;

	private Map<ExifTag, JMenu> exifTagMenusByTag = new EnumMap<>(ExifTag.class);
	private Map<EdgesDetectorFlavour, JMenuItem> edgesDetectorFlavourMenuItemsByFlavour = new EnumMap<>(
			EdgesDetectorFlavour.class);

	public ImageViewerCanvas(WindowPreferences windowPreferences, ExifTagPreferences exifTagPreferences,
			DataModel dataModel, PersistableHelper<PreferencesManager<? extends Preferences>> preferencesPersisterHelper,
			MouseTracker mouseTracker, FileSelector fileSelector,
			PropertyChangeAwareEdgesDetectorPreferences edgesDetectorPreferences,
			EdgesDetectorPreferencesSelectorFactory edgesDetectorPreferencesSelectorFactory, AboutWindow aboutWindow,
			DragAndDropWindow dragAndDropWindow, MessageHandler messageHandler) {
		super();
		this.windowPreferences = windowPreferences;
		this.exifTagPreferences = exifTagPreferences;
		this.edgesDetectorPreferences = edgesDetectorPreferences;
		edgesDetectorPreferences.addPropertyChangeListener(this);
		this.dataModel = dataModel;
		dataModel.addPropertyChangeListener(this);
		this.preferencesPersisterHelper = preferencesPersisterHelper;
		this.fileSelector = fileSelector;
		this.edgesDetectorPreferencesSelectorFactory = edgesDetectorPreferencesSelectorFactory;
		this.aboutWindow = aboutWindow;
		this.dragAndDropWindow = dragAndDropWindow;
		dragAndDropWindow.setProxyFor(this);

		int width = windowPreferences.getMainWindowWidth();
		int height = windowPreferences.getMainWindowHeight();

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

		DragAndDropSupportHelper.addJavaFileListSupport(this, messageHandler);
	}

	@Override
	public void accept(List<File> selectedFiles) {
		if (!selectedFiles.isEmpty()) {
			dataModel.loadFiles(selectedFiles);
			dataModel.reframe(getWidth(), getHeight());
			repaint();
		}
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
		JMenu edgesDetectorFlavourMenuItem = new JMenu("Flavours");
		edgesDetectorMenu.add(edgesDetectorFlavourMenuItem);
		for (EdgesDetectorFlavour flavour : EdgesDetectorFlavour.values()) {
			JMenuItem flavourMenuItem = addCheckboxMenuItem(edgesDetectorFlavourMenuItem, flavour.getDescription(), -1,
					edgesDetectorPreferences.getEdgesDetectorFlavour() == flavour, event -> {
						updateEdgesDetectorFlavour(flavour);
					});
			edgesDetectorFlavourMenuItemsByFlavour.put(flavour, flavourMenuItem);
		}
		showEdgesMenuItem = addCheckboxMenuItem(edgesDetectorMenu, "Show edges", KeyEvent.VK_M,
				edgesDetectorPreferences.isShowEdges(), event -> {
					edgesDetectorPreferences.setShowEdges(!edgesDetectorPreferences.isShowEdges());
					repaint();
				});
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

	private void updateEdgesDetectorFlavour(EdgesDetectorFlavour flavour) {
		edgesDetectorPreferences.setEdgesDetectorFlavour(flavour);
		for (Entry<EdgesDetectorFlavour, JMenuItem> entry : edgesDetectorFlavourMenuItemsByFlavour.entrySet()) {
			entry.getValue().setSelected(edgesDetectorPreferences.getEdgesDetectorFlavour() == entry.getKey());
		}
		dataModel.calculateEdges();
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
		Graphics2D graphics = (Graphics2D) gfx;
		graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		dataModel.repaint(graphics);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (CommunicationMessages.EDGES_VISIBILITY.equals(evt.getPropertyName())) {
			handleEdgesVisibilityChange();
		} else if (CommunicationMessages.EDGES_CALCULATION_STARTED.equals(evt.getPropertyName())) {
			handleEdgeCalculationStarted();
		} else if (CommunicationMessages.EDGES_CALCULATION_COMPLETED.equals(evt.getPropertyName())) {
			handleEdgeCalculationCompleted();
		} else if (CommunicationMessages.REQUEST_REPAINT.equals(evt.getPropertyName())) {
			handleRequestRepaint();
		}
	}

	private void handleEdgesVisibilityChange() {
		showEdgesMenuItem.setSelected(edgesDetectorPreferences.isShowEdges());
	}

	private void handleEdgeCalculationStarted() {
		repaint();
	}

	private void handleEdgeCalculationCompleted() {
		repaint();
	}

	private void handleRequestRepaint() {
		repaint();
	}

}
