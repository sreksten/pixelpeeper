package com.threeamigos.pixelpeeper.implementations.preferences;

import com.threeamigos.common.util.interfaces.filesystem.RootPathProvider;
import com.threeamigos.common.util.interfaces.messagehandler.MessageHandler;
import com.threeamigos.pixelpeeper.implementations.persister.JsonFilePersister;
import com.threeamigos.pixelpeeper.implementations.preferences.flavours.JsonStatusTracker;
import com.threeamigos.pixelpeeper.interfaces.StatusTracker;
import com.threeamigos.pixelpeeper.interfaces.persister.PersistResult;
import com.threeamigos.pixelpeeper.interfaces.persister.Persister;
import com.threeamigos.pixelpeeper.interfaces.preferences.Preferences;
import com.threeamigos.pixelpeeper.interfaces.preferences.PreferencesManager;

public class PreferencesManagerImpl<T extends Preferences> implements PreferencesManager<T> {

	private final T preferences;
	private final StatusTracker<T> statusTracker;
	private final Persister<T> persister;
	private final MessageHandler messageHandler;
	private boolean invalidAtLoad;

	public PreferencesManagerImpl(T preferences, String filename, String entityDescription,
			RootPathProvider rootPathProvider, MessageHandler messageHandler) {
		this.preferences = preferences;
		this.statusTracker = new JsonStatusTracker<>(preferences);
		this.persister = new JsonFilePersister<>(filename, entityDescription, rootPathProvider, messageHandler);
		this.messageHandler = messageHandler;

		PersistResult persistResult = persister.load(preferences);
		if (!persistResult.isSuccessful()) {
			if (!persistResult.isNotFound()) {
				handleError(persistResult.getError());
				invalidAtLoad = true;
			}
			preferences.loadDefaultValues();
		} else {
			try {
				preferences.validate();
			} catch (IllegalArgumentException e) {
				handleError(e.getMessage());
				preferences.loadDefaultValues();
				invalidAtLoad = true;
			}
		}
		statusTracker.loadInitialValues();
	}

	private void handleError(String error) {
		messageHandler.handleErrorMessage(preferences.getDescription()
				+ " were invalid and have been replaced with default values. Error was: " + error);
	}

	@Override
	public void persist() {
		if (invalidAtLoad || statusTracker.hasChanged()) {
			PersistResult persistResult = persister.save(preferences);
			if (!persistResult.isSuccessful()) {
				messageHandler.handleErrorMessage(persistResult.getError());
			}
		}
	}

}
