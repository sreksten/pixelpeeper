package com.threeamigos.imageviewer.interfaces.preferences;

/**
 * A set of preferences for an application.
 * 
 * @author stefano
 *
 */
public interface Preferences {

	public String getDescription();

	public void validate();

	public void loadDefaultValues();

}
