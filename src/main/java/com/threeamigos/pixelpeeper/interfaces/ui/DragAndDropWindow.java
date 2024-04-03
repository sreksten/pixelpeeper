package com.threeamigos.pixelpeeper.interfaces.ui;

import com.threeamigos.common.util.interfaces.ui.HintsProducer;

import java.io.File;
import java.util.List;
import java.util.function.Consumer;

/**
 * An interface for a window able to receive a list of files and ask the main UI to process them.
 * It extends the following interfaces:
 * <ul>
 *     <li>{@link Consumer} for a list of files, in order to receive them</li>
 *     <li>{@link HintsProducer} in order to provide the end-user for some hints about the
 *     behaviour of this window</li>
 * </ul>
 * This window should act as a proxy for another {@link ImageConsumer}; that is, once it receives the files
 * via a drag-and-drop operation, it passes the same list of files to another component of the UI (very
 * probably the main window).
 *
 * @author Stefano Reksten
 */
public interface DragAndDropWindow extends Consumer<List<File>>, HintsProducer<String> {

    /**
     * Sets the visibility for this window (open or close it)
     */
    void setVisible(boolean visible);

    /**
     * What {@link ImageConsumer} this window is a proxy for.
     */
    void setProxyFor(ImageConsumer consumer);

}
