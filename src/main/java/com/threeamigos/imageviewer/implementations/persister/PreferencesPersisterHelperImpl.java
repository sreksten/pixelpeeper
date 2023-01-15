package com.threeamigos.imageviewer.implementations.persister;

import java.util.ArrayList;
import java.util.List;

import com.threeamigos.imageviewer.interfaces.persister.Persistable;
import com.threeamigos.imageviewer.interfaces.preferences.PreferencesPersisterHelper;

public class PreferencesPersisterHelperImpl implements PreferencesPersisterHelper {

	private List<Persistable> persistables = new ArrayList<>();

	public PreferencesPersisterHelperImpl(Persistable... persistables) {
		for (Persistable current : persistables) {
			this.persistables.add(current);
		}
	}

	@Override
	public void addPersistable(Persistable persistable) {
		persistables.add(persistable);
	}

	@Override
	public void persist() {
		persistables.forEach(Persistable::persist);
	}

}
