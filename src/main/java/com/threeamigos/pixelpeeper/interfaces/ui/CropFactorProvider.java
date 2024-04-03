package com.threeamigos.pixelpeeper.interfaces.ui;

import java.awt.*;

/**
 * An interface that provides the crop factor for a given camera manufacturer and model.
 * Default crop factor is 1.0f considered the crop factor for a full-frame sensor.
 *
 * @author Stefano Reksten
 */
public interface CropFactorProvider {

    /**
     * Crop factor for a full-frame sensor
     */
    float CROP_FACTOR_DEFAULT = 1.0f;

    /**
     * Returns the crop factor for a given camera manufacturer and model.
     *
     * @param cameraManufacturer manufacturer of the camera (e.g. Canon, Panasonic)
     * @param cameraModel        model of the camera (e.g. RP, G9)
     * @param component          the parent UI component this interface may refer to
     */
    float getCropFactor(String cameraManufacturer, String cameraModel, Component component);

}
