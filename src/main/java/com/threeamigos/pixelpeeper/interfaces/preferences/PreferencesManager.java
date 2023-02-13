package com.threeamigos.pixelpeeper.interfaces.preferences;

import com.threeamigos.pixelpeeper.interfaces.persister.Persistable;

/**
 * A class that takes care of the whole lifecycle of a set of preferences. The
 * persist() method, when called, should result in preferences being saved.
 * 
 * @author stefano
 *
 * @param <T>
 */
public interface PreferencesManager<T extends Preferences> extends Persistable {

}
