package com.threeamigos.pixelpeeper.interfaces.preferences;

import com.threeamigos.common.util.interfaces.PropertyChangeAware;

/**
 * A set of preferences for an application.
 * 
 * @author stefano
 *
 */
public interface Preferences extends PropertyChangeAware {

	public String getDescription();

	public void validate();

	public void loadDefaultValues();

}
