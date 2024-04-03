package com.threeamigos.pixelpeeper;

import com.threeamigos.common.util.implementations.ui.ChainedInputConsumer;
import com.threeamigos.pixelpeeper.instances.*;
import com.threeamigos.pixelpeeper.instances.decorators.GridDecoratorInstance;
import com.threeamigos.pixelpeeper.instances.plugins.*;
import com.threeamigos.pixelpeeper.interfaces.datamodel.DataModel;
import com.threeamigos.pixelpeeper.interfaces.ui.ImageDecorator;
import com.threeamigos.pixelpeeper.interfaces.ui.MainWindowPlugin;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Main class of the PixelPeeper application.<br/>
 * Makes use of Drew Noakes'
 * <a href="https://drewnoakes.com/code/exif/">Metadata Extractor</a>.
 *
 * @author Stefano Reksten
 */

public class Main {

    // BUGFIX: if the drag and drop window is open, focus is switched to that. Well
    // maybe not quite a bug but...

    // BUGFIX: the big pointer may flicker when changed

    // BUGFIX: clicking mouse button when keeping CAPS key will still change to hand
    // cursor even when drawing

    // BUGFIX: lens manufacturer still missing

    //TODO activate the RomyJona edges detector only in a test environment :)
    //TODO a help window that shows all keyboard shortcuts

    public static final String APPLICATION_NAME = "3AM Pixel Peeper";

    public Main() {

        DataModel dataModel = DataModelInstance.get();
        new Thread(dataModel::loadLastFiles).start();

        // User Interface

        Collection<ImageDecorator> decorators = new ArrayList<>();
        decorators.add(GridDecoratorInstance.get());

        JMenuBar menuBar = new JMenuBar();

        ImageViewerCanvas imageViewerCanvas = new ImageViewerCanvas(menuBar, Preferences.MAIN_WINDOW,
                Preferences.DRAG_AND_DROP_WINDOW, dataModel, CursorManagerInstance.get(), FileSelectorInstance.get(),
                NamePatternSelectorInstance.get(), FileRenamerInstance.get(),
                ChainedInputConsumerInstance.get(), decorators, AboutWindowInstance.get(), HintsDisplayerInstance.get(),
                DragAndDropWindowInstance.get(), MessageHandlerInstance.get(), getPlugins());

        dataModel.addPropertyChangeListener(imageViewerCanvas);
        dataModel.addPropertyChangeListener(ControlsPanelInstance.get());
        ImageSlicesInstance.get().addPropertyChangeListener(imageViewerCanvas);
        UserInputTrackerInstance.get().addPropertyChangeListener(dataModel);
        EdgesDetectorPluginInstance.get().addPropertyChangeListener(dataModel);
        ImageHandlingPluginInstance.get().addPropertyChangeListener(dataModel);
        Preferences.IMAGE_HANDLING.addPropertyChangeListener(ControlsPanelInstance.get());
        Preferences.IMAGE_HANDLING.addPropertyChangeListener(imageViewerCanvas);
        Preferences.IMAGE_HANDLING.addPropertyChangeListener(dataModel);
        Preferences.IMAGE_HANDLING.addPropertyChangeListener(ImageHandlingPluginInstance.get());
        Preferences.CURSOR.addPropertyChangeListener(imageViewerCanvas);
        Preferences.CURSOR.addPropertyChangeListener(CursorManagerInstance.get());
        Preferences.CURSOR.addPropertyChangeListener(CursorPluginInstance.get());
        Preferences.GRID.addPropertyChangeListener(imageViewerCanvas);
        Preferences.GRID.addPropertyChangeListener(GridPluginInstance.get());
        Preferences.EXIF_TAG.addPropertyChangeListener(imageViewerCanvas);
        Preferences.EXIF_TAG.addPropertyChangeListener(ImageSlicesInstance.get());
        Preferences.EDGES_DETECTOR.addPropertyChangeListener(dataModel);
        Preferences.EDGES_DETECTOR.addPropertyChangeListener(EdgesDetectorPluginInstance.get());
        CursorManagerInstance.get().addPropertyChangeListener(imageViewerCanvas);

        setupChainedInputConsumer();

        JFrame jframe = prepareFrame(menuBar, imageViewerCanvas, ControlsPanelInstance.get());
        jframe.setVisible(true);

        if (Preferences.HINTS.isHintsVisibleAtStartup()) {
            HintsDisplayerInstance.get().showHints(jframe);
        }
    }

    private List<MainWindowPlugin> getPlugins() {
        List<MainWindowPlugin> plugins = new ArrayList<>();
        plugins.add(EdgesDetectorPluginInstance.get());
        plugins.add(ImageHandlingPluginInstance.get());
        plugins.add(GridPluginInstance.get());
        plugins.add(CursorPluginInstance.get());
        plugins.add(ExifTagsPluginInstance.get());
        return plugins;
    }

    private void setupChainedInputConsumer() {
        ChainedInputConsumer chainedInputConsumer = ChainedInputConsumerInstance.get();
        chainedInputConsumer.addConsumer(UserInputTrackerInstance.get().getInputConsumer(), ChainedInputConsumer.PRIORITY_HIGH);
        chainedInputConsumer.addConsumer(CursorManagerInstance.get().getInputConsumer(), ChainedInputConsumer.PRIORITY_HIGH);
        chainedInputConsumer.addConsumer(GridPluginInstance.get().getInputConsumer(), ChainedInputConsumer.PRIORITY_MEDIUM);
        chainedInputConsumer.addConsumer(EdgesDetectorPluginInstance.get().getInputConsumer(), ChainedInputConsumer.PRIORITY_LOW);
        chainedInputConsumer.addConsumer(ImageHandlingPluginInstance.get().getInputConsumer(), ChainedInputConsumer.PRIORITY_LOW);
        chainedInputConsumer.addConsumer(ExifTagsPluginInstance.get().getInputConsumer(), ChainedInputConsumer.PRIORITY_LOW);
        chainedInputConsumer.addConsumer(DataModelInstance.get().getInputConsumer(), ChainedInputConsumer.PRIORITY_LOW);
    }

    private JFrame prepareFrame(JMenuBar menuBar, ImageViewerCanvas imageViewerCanvas, ControlsPanel controlsPanel) {

        JFrame jframe = new JFrame(APPLICATION_NAME);
        jframe.setMinimumSize(new Dimension(800, 600));

        jframe.setJMenuBar(menuBar);
        jframe.add(imageViewerCanvas, BorderLayout.CENTER);
        jframe.add(controlsPanel, BorderLayout.SOUTH);

        jframe.pack();
        jframe.setResizable(true);
        jframe.setLocation(Preferences.MAIN_WINDOW.getX(), Preferences.MAIN_WINDOW.getY());

        jframe.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                imageViewerCanvas.reframeDataModel();
                Preferences.MAIN_WINDOW.setWidth(imageViewerCanvas.getWidth());
                Preferences.MAIN_WINDOW.setHeight(imageViewerCanvas.getHeight());
            }

            @Override
            public void componentMoved(ComponentEvent e) {
                Preferences.MAIN_WINDOW.setX(jframe.getX());
                Preferences.MAIN_WINDOW.setY(jframe.getY());
            }
        });

        jframe.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        return jframe;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::new);
    }

}
