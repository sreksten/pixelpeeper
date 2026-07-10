package com.threeamigos.pixelpeeper;

import com.threeamigos.common.util.implementations.ui.ChainedInputConsumer;
import com.threeamigos.common.util.interfaces.preferences.flavours.MainWindowPreferences;
import com.threeamigos.common.util.interfaces.ui.AboutWindow;
import com.threeamigos.common.util.interfaces.ui.HintsDisplayer;
import com.threeamigos.common.util.interfaces.ui.InputConsumer;
import com.threeamigos.common.util.ui.draganddrop.DragAndDropSupportHelper;
import com.threeamigos.pixelpeeper.data.ExifTag;
import com.threeamigos.pixelpeeper.implementations.eventbus.EventBus;
import com.threeamigos.pixelpeeper.implementations.eventbus.events.*;
import com.threeamigos.pixelpeeper.implementations.ui.InputAdapter;
import com.threeamigos.pixelpeeper.interfaces.datamodel.DataModel;
import com.threeamigos.pixelpeeper.interfaces.datamodel.FileRenamer;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavors.DragAndDropWindowPreferences;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavors.ShortcutsWindowPreferences;
import com.threeamigos.pixelpeeper.interfaces.ui.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The canvas on which we draw the various images
 *
 * @author Stefano Reksten
 */
public class ImageViewerCanvas extends JPanel implements ImageConsumer, MainWindow {

    private static final long serialVersionUID = 1L;

    private final transient DragAndDropWindowPreferences dragAndDropWindowPreferences;
    private final transient ShortcutsWindowPreferences shortcutsWindowPreferences;
    private final transient DataModel dataModel;
    private final transient CursorManager cursorManager;
    private final transient FileSelector fileSelector;
    private final transient NamePatternSelector namePatternSelector;
    private final transient FileRenamer fileRenamer;
    private final transient Collection<ImageDecorator> decorators;
    private final transient AboutWindow aboutWindow;
    private final transient HintsDisplayer hintsWindow;
    private final transient DragAndDropWindow dragAndDropWindow;
    private final transient ShortcutsWindow shortcutsWindow;
    private final transient Collection<MainWindowPlugin> plugins;

    private final JMenuBar menuBar;
    private final Map<String, JMenu> menus = new HashMap<>();

    public ImageViewerCanvas(ImageViewerCanvasBuilder builder) {
        super();
        this.menuBar = builder.getMenuBar();
        this.dragAndDropWindowPreferences = builder.getDragAndDropWindowPreferences();
        this.shortcutsWindowPreferences = builder.getShortcutsWindowPreferences();
        this.dataModel = builder.getDataModel();
        this.cursorManager = builder.getCursorManager();
        this.fileSelector = builder.getFileSelector();
        this.namePatternSelector = builder.getNamePatternSelector();
        this.fileRenamer = builder.getFileRenamer();
        this.decorators = builder.getDecorators();
        this.aboutWindow = builder.getAboutWindow();
        this.hintsWindow = builder.getHintsDisplayer();
        this.dragAndDropWindow = builder.getDragAndDropWindow();
        this.shortcutsWindow = builder.getShortcutsWindow();
        dragAndDropWindow.setProxyFor(this);
        this.plugins = builder.getPlugins();

        MainWindowPreferences mainWindowPreferences = builder.getMainWindowPreferences();
        setPreferredSize(new Dimension(mainWindowPreferences.getWidth(), mainWindowPreferences.getHeight()));
        setSize(mainWindowPreferences.getWidth(), mainWindowPreferences.getHeight());
        setMinimumSize(new Dimension(800, 600));

        setBackground(Color.LIGHT_GRAY);
        setFocusable(true);
        setDoubleBuffered(true);

        ChainedInputConsumer chainedInputConsumer = builder.getChainedInputConsumer();
        chainedInputConsumer.addConsumer(getInputConsumer(), ChainedInputConsumer.PRIORITY_LOW);
        addMouseListener(chainedInputConsumer);
        addMouseMotionListener(chainedInputConsumer);
        addMouseWheelListener(chainedInputConsumer);
        addKeyListener(chainedInputConsumer);

        updateCursor();

        DragAndDropSupportHelper.addJavaFileListSupport(this, builder.getMessageHandler());

        subscribeToEvents();

        addMenus();
    }

    private void subscribeToEvents() {
        EventBus.get().subscribe(BigPointerImageUpdateEvent.class, e -> updateCursor());
        EventBus.get().subscribe(DataModelChangedEvent.class,      e -> reframeDataModel());

        // All events that simply need a repaint
        EventBus.get().subscribe(RepaintRequestEvent.class,                      e -> repaint());
        EventBus.get().subscribe(FilterCalculationStartedEvent.class,            e -> repaint());
        EventBus.get().subscribe(FilterCalculationCompletedEvent.class,          e -> repaint());
        EventBus.get().subscribe(GridVisibilityChangedEvent.class,               e -> repaint());
        EventBus.get().subscribe(GridSpacingChangedEvent.class,                  e -> repaint());
        EventBus.get().subscribe(BigPointerVisibilityChangedEvent.class,         e -> repaint());
        EventBus.get().subscribe(BigPointerSizeChangedEvent.class,               e -> repaint());
        EventBus.get().subscribe(BigPointerRotationChangedEvent.class,           e -> repaint());
        EventBus.get().subscribe(FilterVisibilityChangedEvent.class,             e -> repaint());
        EventBus.get().subscribe(FilterTransparencyChangedEvent.class,           e -> repaint());
        EventBus.get().subscribe(FilterFlavorChangedEvent.class,                 e -> repaint());
        EventBus.get().subscribe(AutorotationChangedEvent.class,                 e -> repaint());
        EventBus.get().subscribe(DispositionChangedEvent.class,                  e -> repaint());
        EventBus.get().subscribe(ZoomLevelChangedEvent.class,                    e -> repaint());
        EventBus.get().subscribe(NormalizedForCropChangedEvent.class,            e -> repaint());
        EventBus.get().subscribe(NormalizedForFocalLengthChangedEvent.class,     e -> repaint());
        EventBus.get().subscribe(RelativeMovementChangedEvent.class,             e -> repaint());
        EventBus.get().subscribe(MovementAppliedToAllImagesChangedEvent.class,   e -> repaint());
        EventBus.get().subscribe(PositionMiniatureVisibilityChangedEvent.class,  e -> repaint());
        EventBus.get().subscribe(ImageReaderFlavorChangedEvent.class,            e -> repaint());
        EventBus.get().subscribe(ExifReaderFlavorChangedEvent.class,             e -> repaint());
        EventBus.get().subscribe(TagsVisibilityChangedEvent.class,               e -> repaint());
        EventBus.get().subscribe(TagsVisibilityOverrideChangedEvent.class,       e -> repaint());
        EventBus.get().subscribe(TagsRenderingChangedEvent.class,                e -> repaint());
        EventBus.get().subscribe(TagVisibilityChangedEvent.class,                e -> repaint());
    }

    private void addMenus() {

        JMenu fileMenu = new JMenu("File");
        menuBar.add(fileMenu);
        addMenuItem(fileMenu, "Open files...", KeyRegistry.OPEN_FILES_KEY, event -> openFiles());
        fileMenu.add(new JSeparator());
        addMenuItem(fileMenu, "Select name pattern", KeyRegistry.NO_KEY, event -> selectNamePattern());
        addMenuItem(fileMenu, "Rename files...", KeyRegistry.NO_KEY, event -> renameFiles());
        fileMenu.add(new JSeparator());
        addMenuItem(fileMenu, "Browse directory", KeyRegistry.BROWSE_DIRECTORY_KEY, event -> browseDirectory());
        addMenuItem(fileMenu, "Open drag and drop panel", KeyRegistry.OPEN_DRAG_AND_DROP_PANEL_KEY,
                event -> openDragAndDropPanel());
        addMenuItem(fileMenu, "Quit", KeyRegistry.QUIT_KEY, event -> quit());

        for (MainWindowPlugin plugin : plugins) {
            plugin.setMainWindow(this);
        }

        JMenu aboutMenu = new JMenu("?");
        menuBar.add(aboutMenu);
        addMenuItem(aboutMenu, "Show hints", KeyRegistry.SHOW_HINTS_KEY, event -> showHints());
        addMenuItem(aboutMenu, "Show shortcuts", KeyRegistry.NO_KEY, event -> showShortcuts());
        addMenuItem(aboutMenu, "About", KeyRegistry.SHOW_ABOUT_KEY, event -> showAboutWindow());

    }

    private void openFiles() {
        accept(fileSelector.getSelectedFiles(this));
    }

    private void selectNamePattern() {
        namePatternSelector.selectNamePattern(this);
    }

    private void renameFiles() {
        fileRenamer.selectAndRename(this);
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

    private void showShortcuts() {
        boolean visible = !shortcutsWindowPreferences.isVisible();
        shortcutsWindowPreferences.setVisible(visible);
        shortcutsWindow.setVisible(visible);
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

    @Override
    public void accept(List<File> selectedFiles, ExifTag tagToGroupBy, int tolerance, ExifTag tagToOrderBy) {
        if (!selectedFiles.isEmpty()) {
            dataModel.loadFiles(selectedFiles, tagToGroupBy, tolerance, tagToOrderBy, 0);
        }
    }

    private void addMenuItem(JMenu menu, String title, KeyRegistry mnemonic, ActionListener actionListener) {
        JMenuItem menuItem = new JMenuItem(title);
        if (actionListener != null) {
            menuItem.addActionListener(actionListener);
        }
        if (mnemonic != KeyRegistry.NO_KEY) {
            menuItem.setMnemonic(mnemonic.getKeyCode());
        }
        menu.add(menuItem);
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

    private InputConsumer getInputConsumer() {

        return new InputAdapter() {

            @Override
            public void keyPressed(KeyEvent e) {
                int key = e.getKeyCode();
                if (key == KeyRegistry.OPEN_FILES_KEY.getKeyCode()) {
                    openFiles();
                } else if (key == KeyRegistry.BROWSE_DIRECTORY_KEY.getKeyCode()) {
                    browseDirectory();
                } else if (key == KeyRegistry.OPEN_DRAG_AND_DROP_PANEL_KEY.getKeyCode()) {
                    openDragAndDropPanel();
                } else if (key == KeyRegistry.SHOW_ABOUT_KEY.getKeyCode()) {
                    showAboutWindow();
                } else if (key == KeyRegistry.QUIT_KEY.getKeyCode()) {
                    quit();
                } else if (key == KeyRegistry.SHOW_HINTS_KEY.getKeyCode()) {
                    showHints();
                }
            }

        };
    }

    @Override
    public JMenu getMenu(String menuTitle) {
        JMenu menu = menus.computeIfAbsent(menuTitle, JMenu::new);
        menuBar.add(menu);
        return menu;
    }

    @Override
    public Component getComponent() {
        return this;
    }

}
