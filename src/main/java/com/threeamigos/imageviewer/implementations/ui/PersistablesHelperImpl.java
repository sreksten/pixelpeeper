package com.threeamigos.imageviewer.implementations.ui;

import java.util.ArrayList;
import java.util.List;

import com.threeamigos.imageviewer.interfaces.persister.Persistable;
import com.threeamigos.imageviewer.interfaces.ui.PersistablesHelper;

public class PersistablesHelperImpl implements PersistablesHelper {

	private List<Persistable> persistables = new ArrayList<>();

	@Override
	public void addPersistable(Persistable persistable) {
		if (!persistables.contains(persistable)) {
			persistables.add(persistable);
		}
	}

	@Override
	public void persist() {
		for (Persistable persistable : persistables) {
			persistable.persist();
		}
	}

}
