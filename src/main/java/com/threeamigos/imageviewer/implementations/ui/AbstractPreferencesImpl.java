package com.threeamigos.imageviewer.implementations.ui;

import com.threeamigos.common.util.interfaces.MessageHandler;
import com.threeamigos.imageviewer.interfaces.persister.PersistResult;
import com.threeamigos.imageviewer.interfaces.persister.Persistable;
import com.threeamigos.imageviewer.interfaces.persister.Persister;

public abstract class AbstractPreferencesImpl<T> implements Persistable {

	protected final MessageHandler messageConsumer;
	protected final Persister<T> persister;

	protected AbstractPreferencesImpl(final Persister<T> persister, final MessageHandler messageConsumer) {
		this.persister = persister;
		this.messageConsumer = messageConsumer;
	}

	protected void loadPostConstruct() {
		if (persister != null) {
			@SuppressWarnings("unchecked")
			PersistResult persistResult = persister.load((T) this);
			if (!persistResult.isSuccessful()) {
				if (!persistResult.isNotFound()) {
					messageConsumer.handleErrorMessage("Error while loading " + getEntityDescription() + " preferences: " + persistResult.getError());
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
			@SuppressWarnings("unchecked")
			PersistResult persistResult = persister.save((T) this);
			if (!persistResult.isSuccessful()) {
				messageConsumer.handleErrorMessage("Error while saving " + getEntityDescription() + " preferences: " + persistResult.getError());
			}
		}
	}

}
