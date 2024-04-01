package com.threeamigos.pixelpeeper.implementations.datamodel;

import com.threeamigos.pixelpeeper.interfaces.datamodel.CropFactorRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class CropFactorRepositoryImpl implements CropFactorRepository {

    private final Map<String, Float> cropFactors = new HashMap<>();

    private final Map<String, Float> tempCropFactors = new HashMap<>();

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

    private String buildKey(String cameraManufacturer, String cameraModel) {
        return cameraManufacturer + "/" + cameraModel;
    }

}
