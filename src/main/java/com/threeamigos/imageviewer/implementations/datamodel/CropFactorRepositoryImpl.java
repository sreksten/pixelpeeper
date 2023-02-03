package com.threeamigos.imageviewer.implementations.datamodel;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.threeamigos.imageviewer.interfaces.datamodel.CropFactorRepository;

public class CropFactorRepositoryImpl implements CropFactorRepository {

	private Map<String, Float> cropFactors = new HashMap<>();

	@Override
	public Optional<Float> loadCropFactor(String cameraManufacturer, String cameraModel) {
		return Optional.ofNullable(cropFactors.get(buildKey(cameraManufacturer, cameraModel)));
	}

	@Override
	public void storeCropFactor(String cameraManufacturer, String cameraModel, float cropFactor) {
		cropFactors.put(buildKey(cameraManufacturer, cameraModel), cropFactor);
	}

	private final String buildKey(String cameraManufacturer, String cameraModel) {
		return new StringBuilder(cameraManufacturer).append("/").append(cameraModel).toString();
	}

}
