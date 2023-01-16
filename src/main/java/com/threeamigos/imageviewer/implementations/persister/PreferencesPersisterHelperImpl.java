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
		// persistables.stream().filter(Persistable::hasChanged).forEach(Persistable::persist);
		for (Persistable persistable : persistables) {
			if (persistable.hasChanged()) {
				System.out.println("Persistable " + persistable + " has changed, saving it");
				persistable.persist();
			} else {
				System.out.println("Persistable " + persistable + " has NOT changed");
			}
		}
	}

}
