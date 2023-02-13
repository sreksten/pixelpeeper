package com.threeamigos.pixelpeeper.implementations.datamodel;

import com.threeamigos.common.util.interfaces.MessageHandler;
import com.threeamigos.common.util.preferences.filebased.interfaces.RootPathProvider;
import com.threeamigos.pixelpeeper.implementations.persister.JsonFilePersister;
import com.threeamigos.pixelpeeper.implementations.preferences.flavours.JsonStatusTracker;
import com.threeamigos.pixelpeeper.interfaces.StatusTracker;
import com.threeamigos.pixelpeeper.interfaces.datamodel.CropFactorRepository;
import com.threeamigos.pixelpeeper.interfaces.datamodel.CropFactorRepositoryManager;
import com.threeamigos.pixelpeeper.interfaces.persister.PersistResult;
import com.threeamigos.pixelpeeper.interfaces.persister.Persister;

public class CropFactorRepositoryManagerImpl implements CropFactorRepositoryManager {

	private final CropFactorRepository cropFactorRepository;
	private final StatusTracker<CropFactorRepository> statusTracker;
	private final Persister<CropFactorRepository> persister;
	private final MessageHandler messageHandler;
	private boolean invalidAtLoad;

	public CropFactorRepositoryManagerImpl(CropFactorRepository cropFactorRepository, String filename,
			String entityDescription, RootPathProvider rootPathProvider, MessageHandler messageHandler) {
		this.cropFactorRepository = cropFactorRepository;
		this.statusTracker = new JsonStatusTracker<>(cropFactorRepository);
		this.persister = new JsonFilePersister<>(filename, entityDescription, rootPathProvider, messageHandler);
		this.messageHandler = messageHandler;

		PersistResult persistResult = persister.load(cropFactorRepository);
		if (!persistResult.isSuccessful()) {
			if (!persistResult.isNotFound()) {
				handleError(persistResult.getError());
				invalidAtLoad = true;
			}
		}
		statusTracker.loadInitialValues();
	}

	private void handleError(String error) {
		messageHandler
				.handleErrorMessage("Crop factor repository was invalid and has been cleared. Error was: " + error);
	}

	@Override
	public void persist() {
		if (invalidAtLoad || statusTracker.hasChanged()) {
			PersistResult persistResult = persister.save(cropFactorRepository);
			if (!persistResult.isSuccessful()) {
				messageHandler.handleErrorMessage(persistResult.getError());
			}
		}
	}

}
