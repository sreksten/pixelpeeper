package com.threeamigos.pixelpeeper.interfaces.ui;

import java.awt.Component;
import java.io.File;
import java.util.List;

/**
 * Selects images and keeps track of the last directory browsed
 *
 * @author Stefano Reksten
 *
 */
public interface FileSelector {

	public List<File> getSelectedFiles(Component component);

	public List<File> getSelectedFiles(Component component, String acceptText);

	public List<File> getSelectedFiles(Component component, String acceptText, char acceptTextHighlightChar);

	public File getSelectedDirectory(Component component);

	public File getSelectedDirectory(Component component, String acceptText);

	public File getSelectedDirectory(Component component, String acceptText, char acceptTextHighlightChar);

}
