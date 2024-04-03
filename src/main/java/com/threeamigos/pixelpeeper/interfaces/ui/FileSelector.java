package com.threeamigos.pixelpeeper.interfaces.ui;

import java.awt.*;
import java.io.File;
import java.util.List;

/**
 * An interface used to select images and keeps track of the last browsed directory. The easiest way to implement
 * this is of course with a standard AWT file selector.
 *
 * @author Stefano Reksten
 */
public interface FileSelector {

    /**
     * Returns a collection of selected files.
     *
     * @param component parent component
     */
    List<File> getSelectedFiles(Component component);

    /**
     * Returns a collection of selected files.
     *
     * @param component  parent component
     * @param acceptText a string of text to show on a typical "OK" button
     */
    List<File> getSelectedFiles(Component component, String acceptText);

    /**
     * Returns a collection of selected files.
     *
     * @param component               parent component
     * @param acceptText              a string of text to show on a typical OK button
     * @param acceptTextHighlightChar character shortcut for the OK button
     */
    List<File> getSelectedFiles(Component component, String acceptText, char acceptTextHighlightChar);

    /**
     * Returns a directory that will be searched for images.
     *
     * @param component parent component
     */
    File getSelectedDirectory(Component component);

    /**
     * Returns a directory that will be searched for images.
     *
     * @param component  parent component
     * @param acceptText a string of text to show on a typical OK button
     */
    File getSelectedDirectory(Component component, String acceptText);

    /**
     * Returns a directory that will be searched for images.
     *
     * @param component               parent component
     * @param acceptText              a string of text to show on a typical OK button
     * @param acceptTextHighlightChar character shortcut for the OK button
     */
    File getSelectedDirectory(Component component, String acceptText, char acceptTextHighlightChar);

}
