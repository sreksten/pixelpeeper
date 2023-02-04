package com.threeamigos.imageviewer.interfaces.datamodel;

import java.util.Optional;

public interface CropFactorRepository {

	public Optional<Float> loadCropFactor(String cameraManufacturer, String cameraModel);

	public void storeCropFactor(String cameraManufacturer, String cameraModel, float cropFactor);

	public void storeTemporaryCropFactor(String cameraManufacturer, String cameraModel, float cropFactor);

}
