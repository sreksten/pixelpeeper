package com.threeamigos.pixelpeeper.interfaces.ui;

import com.threeamigos.common.util.implementations.ui.ChainedInputConsumer;
import com.threeamigos.common.util.interfaces.messagehandler.MessageHandler;
import com.threeamigos.common.util.interfaces.preferences.flavours.MainWindowPreferences;
import com.threeamigos.common.util.interfaces.ui.AboutWindow;
import com.threeamigos.common.util.interfaces.ui.HintsDisplayer;
import com.threeamigos.pixelpeeper.ImageViewerCanvas;
import com.threeamigos.pixelpeeper.interfaces.datamodel.DataModel;
import com.threeamigos.pixelpeeper.interfaces.datamodel.FileRenamer;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavours.DragAndDropWindowPreferences;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavours.ShortcutsWindowPreferences;

import javax.swing.*;
import java.util.Collection;

public interface ImageViewerCanvasBuilder {

    interface StepMessageHandler {
        StepDataModel withMessageHandler(MessageHandler messageHandler);
    }

    MessageHandler getMessageHandler();

    interface StepDataModel {
        StepMainWindowPreferences withDataModel(DataModel dataModel);
    }

    DataModel getDataModel();

    interface StepMainWindowPreferences {
        StepDragAndDropWindowPreferences withMainWindowPreferences(MainWindowPreferences mainWindowPreferences);
    }

    MainWindowPreferences getMainWindowPreferences();

    interface StepDragAndDropWindowPreferences {
        StepDragAndDropWindow withDragAndDropWindowPreferences(DragAndDropWindowPreferences dragAndDropWindowPreferences);
    }

    DragAndDropWindowPreferences getDragAndDropWindowPreferences();

    interface StepDragAndDropWindow {
        StepShortcutsWindowPreferences withDragAndDropWindow(DragAndDropWindow dragAndDropWindow);
    }

    DragAndDropWindow getDragAndDropWindow();

    interface StepShortcutsWindowPreferences {
        StepShortcutsWindow withShortcutsWindowPreferences(ShortcutsWindowPreferences shortcutsWindowPreferences);
    }

    ShortcutsWindowPreferences getShortcutsWindowPreferences();

    interface StepShortcutsWindow {
        StepCursorManager withShortcutsWindow(ShortcutsWindow shortcutsWindow);
    }

    ShortcutsWindow getShortcutsWindow();

    interface StepCursorManager {
        StepFileSelector withCursorManager(CursorManager cursorManager);
    }

    CursorManager getCursorManager();

    interface StepFileSelector {
        StepNamePatternSelector withFileSelector(FileSelector fileSelector);
    }

    FileSelector getFileSelector();

    interface StepNamePatternSelector {
        StepFileRenamer withNamePatternSelector(NamePatternSelector namePatternSelector);
    }

    NamePatternSelector getNamePatternSelector();

    interface StepFileRenamer {
        StepHintsDisplayer withFileRenamer(FileRenamer fileRenamer);
    }

    FileRenamer getFileRenamer();

    interface StepHintsDisplayer {
        StepMenuBar withHintsDisplayer(HintsDisplayer hintsDisplayer);
    }

    HintsDisplayer getHintsDisplayer();


    interface StepMenuBar {
        StepChainedInputConsumer withMenuBar(JMenuBar menuBar);
    }

    JMenuBar getMenuBar();

    interface StepChainedInputConsumer {
        StepDecorators withChainedInputConsumer(ChainedInputConsumer chainedInputConsumer);
    }

    ChainedInputConsumer getChainedInputConsumer();

    interface StepDecorators {
        StepPlugins withDecorators(Collection<ImageDecorator> decorators);
    }

    Collection<ImageDecorator> getDecorators();

    interface StepPlugins {
        StepAboutWindow withPlugins(Collection<MainWindowPlugin> plugins);
    }

    Collection<MainWindowPlugin> getPlugins();

    interface StepAboutWindow {
        StepBuild withAboutWindow(AboutWindow aboutWindow);
    }

    AboutWindow getAboutWindow();

    interface StepBuild {
        ImageViewerCanvas build();
    }

}
