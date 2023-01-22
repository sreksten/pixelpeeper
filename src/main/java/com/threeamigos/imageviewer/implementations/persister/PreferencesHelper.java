package com.threeamigos.imageviewer.implementations.persister;

import java.util.ArrayList;
import java.util.List;

import com.threeamigos.common.util.interfaces.MessageHandler;
import com.threeamigos.common.util.preferences.filebased.interfaces.RootPathProvider;
import com.threeamigos.imageviewer.implementations.preferences.PreferencesManagerImpl;
import com.threeamigos.imageviewer.interfaces.persister.Persistable;
import com.threeamigos.imageviewer.interfaces.persister.PersistableCollector;
import com.threeamigos.imageviewer.interfaces.preferences.Preferences;

public class PreferencesHelper implements PersistableCollector {

	private final RootPathProvider rootPathProvider;
	private final MessageHandler messageHandler;

	private List<Persistable> entities = new ArrayList<>();

	public PreferencesHelper(RootPathProvider rootPathProvider, MessageHandler messageHandler) {
		this.rootPathProvider = rootPathProvider;
		this.messageHandler = messageHandler;
	}

	public void register(Preferences preferences, String filename) {
		add(new PreferencesManagerImpl<>(preferences, filename, preferences.getDescription(), rootPathProvider,
				messageHandler));
	}

	@Override
	public void add(Persistable persistable) {
		entities.add(persistable);
	}

	@Override
	public void persist() {
		entities.forEach(Persistable::persist);
	}

}
