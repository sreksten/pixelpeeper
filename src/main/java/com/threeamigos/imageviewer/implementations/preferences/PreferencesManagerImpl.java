package com.threeamigos.imageviewer.implementations.preferences;

import com.threeamigos.common.util.interfaces.MessageHandler;
import com.threeamigos.imageviewer.interfaces.StatusTracker;
import com.threeamigos.imageviewer.interfaces.persister.PersistResult;
import com.threeamigos.imageviewer.interfaces.persister.Persister;
import com.threeamigos.imageviewer.interfaces.preferences.Preferences;
import com.threeamigos.imageviewer.interfaces.preferences.PreferencesManager;

public class PreferencesManagerImpl<T extends Preferences> implements PreferencesManager<T> {

	private final T preferences;
	private final StatusTracker<T> statusTracker;
	private final Persister<T> persister;
	private final MessageHandler messageHandler;

	public PreferencesManagerImpl(T preferences, StatusTracker<T> statusTracker, Persister<T> persister,
			MessageHandler messageHandler) {
		this.preferences = preferences;
		this.statusTracker = statusTracker;
		this.persister = persister;
		this.messageHandler = messageHandler;

		PersistResult persistResult = persister.load(preferences);
		if (!persistResult.isSuccessful()) {
			if (!persistResult.isNotFound()) {
				messageHandler.handleErrorMessage(persistResult.getError());
			}
			preferences.loadDefaultValues();
		}
		statusTracker.hasChanged();
	}

	@Override
	public void persist() {
		if (statusTracker.hasChanged()) {
			PersistResult persistResult = persister.save(preferences);
			if (!persistResult.isSuccessful()) {
				messageHandler.handleErrorMessage(persistResult.getError());
			}
		}
	}

}
