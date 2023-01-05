package com.threeamigos.imageviewer.implementations.ui;

import javax.swing.JOptionPane;

import com.threeamigos.imageviewer.interfaces.persister.PersistResult;
import com.threeamigos.imageviewer.interfaces.persister.Persistable;
import com.threeamigos.imageviewer.interfaces.persister.Persister;

public abstract class AbstractPreferencesImpl<T> implements Persistable {

	protected Persister<T> persister;

	protected AbstractPreferencesImpl(Persister<T> persister) {
		this.persister = persister;
	}

	protected void loadPostConstruct() {
		if (persister != null) {
			PersistResult persistResult = persister.load((T) this);
			if (!persistResult.isSuccessful()) {
				if (!persistResult.isNotFound()) {
					JOptionPane.showMessageDialog(null, "Error while loading " + getEntityDescription()
							+ " preferences: " + persistResult.getError());
				}
				loadDefaultValues();
			}
		} else {
			loadDefaultValues();
		}
	}

	protected abstract String getEntityDescription();

	protected abstract void loadDefaultValues();

	@Override
	public void persist() {
		if (persister != null) {
			PersistResult persistResult = persister.save((T) this);
			if (!persistResult.isSuccessful()) {
				JOptionPane.showMessageDialog(null,
						"Error while saving " + getEntityDescription() + " preferences: " + persistResult.getError());
			}
		}
	}

}
