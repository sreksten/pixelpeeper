package com.threeamigos.imageviewer.implementations.persister;

import com.threeamigos.common.util.interfaces.MessageHandler;
import com.threeamigos.imageviewer.interfaces.persister.PersistResult;
import com.threeamigos.imageviewer.interfaces.persister.Persistable;
import com.threeamigos.imageviewer.interfaces.persister.Persister;
import com.threeamigos.imageviewer.interfaces.preferences.Preferences;

public class PreferencesPersister<T extends Preferences> implements Persistable {

	private final T preferences;
	private final Persister<T> persister;
	private final MessageHandler messageHandler;

	public PreferencesPersister(T preferences, Persister<T> persister, MessageHandler messageHandler) {
		this.preferences = preferences;
		this.persister = persister;
		this.messageHandler = messageHandler;

		PersistResult persistResult = persister.load(preferences);
		if (!persistResult.isSuccessful()) {
			if (!persistResult.isNotFound()) {
				messageHandler.handleErrorMessage(persistResult.getError());
			}
			preferences.loadDefaultValues();
		}
	}

	@Override
	public void persist() {
		PersistResult persistResult = persister.save(preferences);
		if (!persistResult.isSuccessful()) {
			messageHandler.handleErrorMessage(persistResult.getError());
		}
	}

}
