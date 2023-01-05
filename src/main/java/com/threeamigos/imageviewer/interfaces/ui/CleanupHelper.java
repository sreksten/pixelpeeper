package com.threeamigos.imageviewer.interfaces.ui;

import com.threeamigos.imageviewer.interfaces.persister.Persistable;

/**
 * At the end of the day this helps storing all preferences and exits the
 * program
 *
 * @author Stefano Reksten
 *
 */
public interface CleanupHelper {

	public void addPersistable(Persistable persistable);

	public void cleanUpAndExit();

}
