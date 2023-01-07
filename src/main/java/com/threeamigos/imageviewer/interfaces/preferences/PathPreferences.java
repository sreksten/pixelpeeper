package com.threeamigos.imageviewer.interfaces.preferences;

import java.util.List;

import com.threeamigos.imageviewer.interfaces.persister.Persistable;

/**
 * Keeps track of the last directory browsed
 *
 * @author Stefano Reksten
 *
 */
public interface PathPreferences extends Persistable {

	public void setLastPath(String path);

	public String getLastPath();

	public void setLastFilenames(List<String> lastFilenames);

	public List<String> getLastFilenames();

}
