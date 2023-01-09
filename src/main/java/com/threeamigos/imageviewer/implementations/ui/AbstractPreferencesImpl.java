package com.threeamigos.imageviewer.implementations.ui;

import javax.swing.JOptionPane;

import com.threeamigos.common.util.interfaces.MessageConsumer;
import com.threeamigos.imageviewer.interfaces.persister.PersistResult;
import com.threeamigos.imageviewer.interfaces.persister.Persistable;
import com.threeamigos.imageviewer.interfaces.persister.Persister;

public abstract class AbstractPreferencesImpl<T> implements Persistable {

	protected final MessageConsumer messageConsumer;
	protected final Persister<T> persister;

	protected AbstractPreferencesImpl(final Persister<T> persister, final MessageConsumer messageConsumer) {
		this.persister = persister;
		this.messageConsumer = messageConsumer;
	}

	protected void loadPostConstruct() {
		if (persister != null) {
			PersistResult persistResult = persister.load((T) this);
			if (!persistResult.isSuccessful()) {
				if (!persistResult.isNotFound()) {
					messageConsumer.error("Error while loading " + getEntityDescription() + " preferences: " + persistResult.getError());
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
				messageConsumer.error("Error while saving " + getEntityDescription() + " preferences: " + persistResult.getError());
			}
		}
	}

}
