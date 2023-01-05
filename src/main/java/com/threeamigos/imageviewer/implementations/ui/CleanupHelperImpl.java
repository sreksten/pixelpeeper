package com.threeamigos.imageviewer.implementations.ui;

import java.util.ArrayList;
import java.util.List;

import com.threeamigos.imageviewer.interfaces.persister.Persistable;
import com.threeamigos.imageviewer.interfaces.ui.CleanupHelper;

public class CleanupHelperImpl implements CleanupHelper {

	private List<Persistable> persistables = new ArrayList<>();

	public void addPersistable(Persistable persistable) {
		if (!persistables.contains(persistable)) {
			persistables.add(persistable);
		}
	}

	@Override
	public void cleanUpAndExit() {
		for (Persistable persistable : persistables) {
			persistable.persist();
		}
		System.exit(0);
	}

}
