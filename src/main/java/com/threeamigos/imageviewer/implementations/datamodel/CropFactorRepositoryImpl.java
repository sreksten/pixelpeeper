package com.threeamigos.imageviewer.implementations.datamodel;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.threeamigos.imageviewer.interfaces.datamodel.CropFactorRepository;

public class CropFactorRepositoryImpl implements CropFactorRepository {

	private Map<String, Float> cropFactors = new HashMap<>();

	private transient Map<String, Float> tempCropFactors = new HashMap<>();

	@Override
	public Optional<Float> loadCropFactor(String cameraManufacturer, String cameraModel) {
		Float cropFactor = cropFactors.get(buildKey(cameraManufacturer, cameraModel));
		if (cropFactor == null) {
			cropFactor = tempCropFactors.get(buildKey(cameraManufacturer, cameraModel));
		}
		return Optional.ofNullable(cropFactor);
	}

	@Override
	public void storeCropFactor(String cameraManufacturer, String cameraModel, float cropFactor) {
		synchronized (cropFactors) {
			cropFactors.put(buildKey(cameraManufacturer, cameraModel), cropFactor);
		}
	}

	@Override
	public void storeTemporaryCropFactor(String cameraManufacturer, String cameraModel, float cropFactor) {
		synchronized (tempCropFactors) {
			tempCropFactors.put(buildKey(cameraManufacturer, cameraModel), cropFactor);
		}
	}

	private final String buildKey(String cameraManufacturer, String cameraModel) {
		return new StringBuilder(cameraManufacturer).append("/").append(cameraModel).toString();
	}

}
