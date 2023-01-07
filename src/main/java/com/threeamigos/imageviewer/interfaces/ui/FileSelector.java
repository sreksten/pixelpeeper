package com.threeamigos.imageviewer.interfaces.ui;

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

	public File getSelectedDirectory(Component component);

}
