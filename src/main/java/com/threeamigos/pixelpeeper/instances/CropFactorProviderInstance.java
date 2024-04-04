package com.threeamigos.pixelpeeper.instances;

import com.threeamigos.common.util.implementations.json.JsonBuilderImpl;
import com.threeamigos.common.util.implementations.persistence.JsonStatusTrackerFactory;
import com.threeamigos.common.util.interfaces.json.Json;
import com.threeamigos.common.util.interfaces.persistence.StatusTracker;
import com.threeamigos.pixelpeeper.implementations.datamodel.CropFactorRepositoryImpl;
import com.threeamigos.pixelpeeper.implementations.datamodel.CropFactorRepositoryManagerImpl;
import com.threeamigos.pixelpeeper.implementations.ui.CropFactorProviderImpl;
import com.threeamigos.pixelpeeper.interfaces.datamodel.CropFactorRepository;
import com.threeamigos.pixelpeeper.interfaces.datamodel.CropFactorRepositoryManager;
import com.threeamigos.pixelpeeper.interfaces.ui.CropFactorProvider;

/**
 * Since not all cameras provide the 35_mm_equivalent tag, we need a way to retrieve this.
 * A simple file database provides the information to the CropFactoryProvider.
 */
public class CropFactorProviderInstance {

    private static final CropFactorProvider instance;

    private static final CropFactorRepositoryManager cropFactorRepositoryManager;

    static {
        CropFactorRepository cropFactorRepository = new CropFactorRepositoryImpl();
        Json<CropFactorRepository> cropFactorRepositoryJson = new JsonBuilderImpl().build(CropFactorRepository.class);
        StatusTracker<CropFactorRepository> cropFactorRepositoryStatusTracker = new JsonStatusTrackerFactory<>(
                cropFactorRepositoryJson).buildStatusTracker(cropFactorRepository);

        cropFactorRepositoryManager = new CropFactorRepositoryManagerImpl(
                cropFactorRepository, cropFactorRepositoryStatusTracker, "crop_factor.repository",
                "Crop factor repository", RootPathProviderInstance.get(), MessageHandlerInstance.get(),
                cropFactorRepositoryJson);

        Runtime.getRuntime().addShutdownHook(new Thread(cropFactorRepositoryManager::persist));

        instance = new CropFactorProviderImpl(cropFactorRepository, MessageHandlerInstance.get());
    }

    private CropFactorProviderInstance() {
    }

    public static CropFactorProvider get() {
        return instance;
    }
}
