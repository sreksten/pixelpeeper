package com.threeamigos.imageviewer.interfaces.ui;

import com.threeamigos.imageviewer.interfaces.persister.Persistable;

/**
 * Keeps track of the last directory browsed
 *
 * @author Stefano Reksten
 *
 */
public interface PathPreferences extends Persistable {

	public void setPath(String path);

	public String getPath();

}
