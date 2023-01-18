package com.threeamigos.imageviewer.implementations.persister;

import java.util.ArrayList;
import java.util.List;

import com.threeamigos.imageviewer.interfaces.persister.Persistable;
import com.threeamigos.imageviewer.interfaces.persister.PersistableHelper;

public class PersistableHelperImpl<T extends Persistable> implements PersistableHelper<T> {

	private List<T> entities = new ArrayList<>();

	@Override
	public void add(T persistable) {
		entities.add(persistable);
	}

	@Override
	public void persist() {
		entities.forEach(T::persist);
	}

}
