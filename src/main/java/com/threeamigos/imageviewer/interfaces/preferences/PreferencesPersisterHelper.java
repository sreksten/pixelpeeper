package com.threeamigos.imageviewer.interfaces.preferences;

import com.threeamigos.imageviewer.interfaces.persister.Persistable;

public interface PreferencesPersisterHelper {

	public void addPersistable(Persistable persistable);

	public void persist();

}
