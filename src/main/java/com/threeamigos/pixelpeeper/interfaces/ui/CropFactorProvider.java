package com.threeamigos.pixelpeeper.interfaces.ui;

import java.awt.*;

public interface CropFactorProvider {

    float CROP_FACTOR_DEFAULT = 1.0f;

    float getCropFactor(String cameraManufacturer, String cameraModel, Component component);

}
