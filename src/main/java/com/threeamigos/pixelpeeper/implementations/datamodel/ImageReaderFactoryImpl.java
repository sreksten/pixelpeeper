package com.threeamigos.pixelpeeper.implementations.datamodel;

import com.threeamigos.pixelpeeper.implementations.datamodel.imagereaders.ApacheCommonsImagingImageReader;
import com.threeamigos.pixelpeeper.implementations.datamodel.imagereaders.JavaImageIOImageReader;
import com.threeamigos.pixelpeeper.interfaces.datamodel.ImageReader;
import com.threeamigos.pixelpeeper.interfaces.datamodel.ImageReaderFactory;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavours.ImageHandlingPreferences;

public class ImageReaderFactoryImpl implements ImageReaderFactory {

	private final ImageHandlingPreferences imageHandlingPreferences;

	public ImageReaderFactoryImpl(ImageHandlingPreferences imageHandlingPreferences) {
		this.imageHandlingPreferences = imageHandlingPreferences;
	}

	@Override
	public ImageReader getImageReader() {
		switch (imageHandlingPreferences.getImageReaderFlavour()) {
		case JAVA:
			return new JavaImageIOImageReader();
		case APACHE_COMMONS_IMAGING:
			return new ApacheCommonsImagingImageReader();
		}
		throw new IllegalArgumentException("No image reader was defined for flavour "
				+ imageHandlingPreferences.getImageReaderFlavour().getDescription());
	}

}
