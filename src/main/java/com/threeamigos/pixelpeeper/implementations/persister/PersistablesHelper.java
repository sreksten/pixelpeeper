package com.threeamigos.pixelpeeper.implementations.persister;

import java.util.ArrayList;
import java.util.List;

import com.threeamigos.common.util.interfaces.MessageHandler;
import com.threeamigos.common.util.preferences.filebased.interfaces.RootPathProvider;
import com.threeamigos.pixelpeeper.implementations.preferences.PreferencesManagerImpl;
import com.threeamigos.pixelpeeper.interfaces.persister.Persistable;
import com.threeamigos.pixelpeeper.interfaces.persister.PersistableCollector;
import com.threeamigos.pixelpeeper.interfaces.preferences.Preferences;

public class PersistablesHelper implements PersistableCollector {

	private final RootPathProvider rootPathProvider;
	private final MessageHandler messageHandler;

	private List<Persistable> entities = new ArrayList<>();

	public PersistablesHelper(RootPathProvider rootPathProvider, MessageHandler messageHandler) {
		this.rootPathProvider = rootPathProvider;
		this.messageHandler = messageHandler;

		Runtime.getRuntime().addShutdownHook(new Thread(() -> persist()));
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
