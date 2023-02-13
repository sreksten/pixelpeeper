package com.threeamigos.pixelpeeper.interfaces.ui;

import java.awt.Component;

public interface CropFactorProvider {

	public static final float CROP_FACTOR_DEFAULT = 1.0f;

	public float getCropFactor(String cameraManufacturer, String cameraModel, Component component);

}
