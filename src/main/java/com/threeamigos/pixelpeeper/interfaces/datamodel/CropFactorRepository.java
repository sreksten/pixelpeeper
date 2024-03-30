package com.threeamigos.pixelpeeper.interfaces.datamodel;

import java.util.Optional;

public interface CropFactorRepository {

    Optional<Float> loadCropFactor(String cameraManufacturer, String cameraModel);

    void storeCropFactor(String cameraManufacturer, String cameraModel, float cropFactor);

    void storeTemporaryCropFactor(String cameraManufacturer, String cameraModel, float cropFactor);

}
