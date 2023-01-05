package com.threeamigos.imageviewer.interfaces.persister;

/**
 * Result of a load or save operation
 *
 * @author Stefano Reksten
 *
 */
public interface PersistResult {

	public boolean isSuccessful();

	public boolean isNotFound();

	public String getError();

}