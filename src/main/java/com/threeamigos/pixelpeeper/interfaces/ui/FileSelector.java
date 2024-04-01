package com.threeamigos.pixelpeeper.interfaces.ui;

import java.awt.*;
import java.io.File;
import java.util.List;

/**
 * Selects images and keeps track of the last directory browsed
 *
 * @author Stefano Reksten
 */
public interface FileSelector {

    List<File> getSelectedFiles(Component component);

    List<File> getSelectedFiles(Component component, String acceptText);

    List<File> getSelectedFiles(Component component, String acceptText, char acceptTextHighlightChar);

    File getSelectedDirectory(Component component);

    File getSelectedDirectory(Component component, String acceptText);

    File getSelectedDirectory(Component component, String acceptText, char acceptTextHighlightChar);

}
