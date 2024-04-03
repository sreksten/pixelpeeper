package com.threeamigos.pixelpeeper.interfaces.datamodel;

import com.threeamigos.pixelpeeper.data.ExifTag;

import java.util.Optional;

/**
 * An interface used to load and store crop factors for camera models that do not provide
 * the 35-mm (full frame) focal length equivalent in their EXIF tags. See also
 * {@link ExifTag}.<br/>
 * Since the 35-mm equivalent is used as a common denominator to properly scale images,
 * if this cannot be found the user may be asked to specify a crop factor for their camera.
 * This information can then be saved in order to be used in later sessions.
 *
 * @author Stefano Reksten
 */
public interface CropFactorRepository {

    /**
     * Tries to retrieve the crop factor for a given camera manufacturer and model.
     */
    Optional<Float> loadCropFactor(String cameraManufacturer, String cameraModel);

    /**
     * Stores the crop factor for a given camera manufacturer and model.
     */
    void storeCropFactor(String cameraManufacturer, String cameraModel, float cropFactor);

    /**
     * If the end user does not know what the crop factor is for a given camera manufacturer
     * and model, the application stores a default value for it assuming that it's a full-frame
     * camera. This value does not survive the current session, and it will be asked again at
     * a later time.
     */
    void storeTemporaryCropFactor(String cameraManufacturer, String cameraModel, float cropFactor);

}
