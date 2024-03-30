package com.threeamigos.pixelpeeper.interfaces.datamodel;

import com.threeamigos.pixelpeeper.data.PictureData;

import java.io.File;

public interface ExifImageReader {

    PictureData readImage(File file);

}
