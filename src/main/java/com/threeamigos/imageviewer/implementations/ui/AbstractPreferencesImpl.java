package com.threeamigos.imageviewer.implementations.ui;

import com.threeamigos.common.util.interfaces.ErrorMessageHandler;
import com.threeamigos.imageviewer.interfaces.persister.PersistResult;
import com.threeamigos.imageviewer.interfaces.persister.Persistable;
import com.threeamigos.imageviewer.interfaces.persister.Persister;

public abstract class AbstractPreferencesImpl<T> implements Persistable {

	protected final ErrorMessageHandler errorMessageHandler;
	protected final Persister<T> persister;

	protected AbstractPreferencesImpl(final Persister<T> persister, final ErrorMessageHandler errorMessageHandler) {
		this.persister = persister;
		this.errorMessageHandler = errorMessageHandler;
	}

	protected void loadPostConstruct() {
		if (persister != null) {
			@SuppressWarnings("unchecked")
			PersistResult persistResult = persister.load((T) this);
			if (!persistResult.isSuccessful()) {
				if (!persistResult.isNotFound()) {
					errorMessageHandler.handleErrorMessage("Error while loading " + getEntityDescription()
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
			@SuppressWarnings("unchecked")
			PersistResult persistResult = persister.save((T) this);
			if (!persistResult.isSuccessful()) {
				errorMessageHandler.handleErrorMessage(
						"Error while saving " + getEntityDescription() + " preferences: " + persistResult.getError());
			}
		}
	}

}
