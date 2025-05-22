package com.threeamigos.pixelpeeper.implementations.ui;

import com.threeamigos.common.util.implementations.ui.ChainedInputConsumer;
import com.threeamigos.common.util.interfaces.messagehandler.MessageHandler;
import com.threeamigos.common.util.interfaces.preferences.flavours.MainWindowPreferences;
import com.threeamigos.common.util.interfaces.ui.AboutWindow;
import com.threeamigos.common.util.interfaces.ui.HintsDisplayer;
import com.threeamigos.pixelpeeper.ImageViewerCanvas;
import com.threeamigos.pixelpeeper.interfaces.datamodel.DataModel;
import com.threeamigos.pixelpeeper.interfaces.datamodel.FileRenamer;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavors.DragAndDropWindowPreferences;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavors.ShortcutsWindowPreferences;
import com.threeamigos.pixelpeeper.interfaces.ui.*;

import javax.swing.*;
import java.util.Collection;

public class ImageViewerCanvasBuilderImpl implements ImageViewerCanvasBuilder,
        ImageViewerCanvasBuilder.StepMessageHandler,
        ImageViewerCanvasBuilder.StepDataModel,
        ImageViewerCanvasBuilder.StepMainWindowPreferences,
        ImageViewerCanvasBuilder.StepDragAndDropWindowPreferences,
        ImageViewerCanvasBuilder.StepDragAndDropWindow,
        ImageViewerCanvasBuilder.StepShortcutsWindowPreferences,
        ImageViewerCanvasBuilder.StepShortcutsWindow,
        ImageViewerCanvasBuilder.StepCursorManager,
        ImageViewerCanvasBuilder.StepFileSelector,
        ImageViewerCanvasBuilder.StepNamePatternSelector,
        ImageViewerCanvasBuilder.StepFileRenamer,
        ImageViewerCanvasBuilder.StepHintsDisplayer,
        ImageViewerCanvasBuilder.StepMenuBar,
        ImageViewerCanvasBuilder.StepChainedInputConsumer,
        ImageViewerCanvasBuilder.StepDecorators,
        ImageViewerCanvasBuilder.StepPlugins,
        ImageViewerCanvasBuilder.StepAboutWindow,
        ImageViewerCanvasBuilder.StepBuild {

    public static StepMessageHandler builder() {
        return new ImageViewerCanvasBuilderImpl();
    }

    private MessageHandler messageHandler;
    private DataModel dataModel;
    private MainWindowPreferences mainWindowPreferences;
    private DragAndDropWindowPreferences dragAndDropWindowPreferences;
    private DragAndDropWindow dragAndDropWindow;
    private ShortcutsWindowPreferences shortcutsWindowPreferences;
    private ShortcutsWindow shortcutsWindow;
    private CursorManager cursorManager;
    private FileSelector fileSelector;
    private NamePatternSelector namePatternSelector;
    private FileRenamer fileRenamer;
    private HintsDisplayer hintsDisplayer;
    private JMenuBar menuBar;
    private ChainedInputConsumer chainedInputConsumer;
    private Collection<ImageDecorator> decorators;
    private Collection<MainWindowPlugin> plugins;
    private AboutWindow aboutWindow;

    @Override
    public StepDataModel withMessageHandler(MessageHandler messageHandler) {
        this.messageHandler = messageHandler;
        return this;
    }

    @Override
    public MessageHandler getMessageHandler() {
        return messageHandler;
    }

    @Override
    public StepMainWindowPreferences withDataModel(DataModel dataModel) {
        this.dataModel = dataModel;
        return this;
    }

    @Override
    public DataModel getDataModel() {
        return dataModel;
    }

    @Override
    public StepDragAndDropWindowPreferences withMainWindowPreferences(MainWindowPreferences mainWindowPreferences) {
        this.mainWindowPreferences = mainWindowPreferences;
        return this;
    }

    @Override
    public MainWindowPreferences getMainWindowPreferences() {
        return mainWindowPreferences;
    }

    @Override
    public StepDragAndDropWindow withDragAndDropWindowPreferences(DragAndDropWindowPreferences dragAndDropWindowPreferences) {
        this.dragAndDropWindowPreferences = dragAndDropWindowPreferences;
        return this;
    }

    @Override
    public DragAndDropWindowPreferences getDragAndDropWindowPreferences() {
        return dragAndDropWindowPreferences;
    }

    @Override
    public StepShortcutsWindowPreferences withDragAndDropWindow(DragAndDropWindow dragAndDropWindow) {
        this.dragAndDropWindow = dragAndDropWindow;
        return this;
    }

    @Override
    public DragAndDropWindow getDragAndDropWindow() {
        return dragAndDropWindow;
    }

    @Override
    public StepShortcutsWindow withShortcutsWindowPreferences(ShortcutsWindowPreferences shortcutsWindowPreferences) {
        this.shortcutsWindowPreferences = shortcutsWindowPreferences;
        return this;
    }

    @Override
    public ShortcutsWindowPreferences getShortcutsWindowPreferences() {
        return shortcutsWindowPreferences;
    }

    @Override
    public StepCursorManager withShortcutsWindow(ShortcutsWindow shortcutsWindow) {
        this.shortcutsWindow = shortcutsWindow;
        return this;
    }

    @Override
    public ShortcutsWindow getShortcutsWindow() {
        return shortcutsWindow;
    }

    @Override
    public StepFileSelector withCursorManager(CursorManager cursorManager) {
        this.cursorManager = cursorManager;
        return this;
    }

    @Override
    public CursorManager getCursorManager() {
        return cursorManager;
    }

    @Override
    public StepNamePatternSelector withFileSelector(FileSelector fileSelector) {
        this.fileSelector = fileSelector;
        return this;
    }

    @Override
    public FileSelector getFileSelector() {
        return fileSelector;
    }

    @Override
    public StepFileRenamer withNamePatternSelector(NamePatternSelector namePatternSelector) {
        this.namePatternSelector = namePatternSelector;
        return this;
    }

    @Override
    public NamePatternSelector getNamePatternSelector() {
        return namePatternSelector;
    }

    @Override
    public StepHintsDisplayer withFileRenamer(FileRenamer fileRenamer) {
        this.fileRenamer = fileRenamer;
        return this;
    }

    @Override
    public FileRenamer getFileRenamer() {
        return fileRenamer;
    }

    @Override
    public StepMenuBar withHintsDisplayer(HintsDisplayer hintsDisplayer) {
        this.hintsDisplayer = hintsDisplayer;
        return this;
    }

    @Override
    public HintsDisplayer getHintsDisplayer() {
        return hintsDisplayer;
    }

    @Override
    public StepChainedInputConsumer withMenuBar(JMenuBar menuBar) {
        this.menuBar = menuBar;
        return this;
    }

    @Override
    public JMenuBar getMenuBar() {
        return menuBar;
    }

    @Override
    public StepDecorators withChainedInputConsumer(ChainedInputConsumer chainedInputConsumer) {
        this.chainedInputConsumer = chainedInputConsumer;
        return this;
    }

    @Override
    public ChainedInputConsumer getChainedInputConsumer() {
        return chainedInputConsumer;
    }

    @Override
    public StepPlugins withDecorators(Collection<ImageDecorator> decorators) {
        this.decorators = decorators;
        return this;
    }

    @Override
    public Collection<ImageDecorator> getDecorators() {
        return decorators;
    }

    @Override
    public StepAboutWindow withPlugins(Collection<MainWindowPlugin> plugins) {
        this.plugins = plugins;
        return this;
    }

    @Override
    public Collection<MainWindowPlugin> getPlugins() {
        return plugins;
    }

    @Override
    public StepBuild withAboutWindow(AboutWindow aboutWindow) {
        this.aboutWindow = aboutWindow;
        return this;
    }

    @Override
    public AboutWindow getAboutWindow() {
        return aboutWindow;
    }

    @Override
    public ImageViewerCanvas build() {
        return new ImageViewerCanvas(this);
    }

}
