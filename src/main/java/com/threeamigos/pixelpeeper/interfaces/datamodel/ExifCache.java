package com.threeamigos.pixelpeeper.interfaces.datamodel;

import com.threeamigos.pixelpeeper.data.ExifMap;

import java.io.File;
import java.util.Optional;

public interface ExifCache {

    void clear();

    Optional<ExifMap> getExifMap(File file);

}
