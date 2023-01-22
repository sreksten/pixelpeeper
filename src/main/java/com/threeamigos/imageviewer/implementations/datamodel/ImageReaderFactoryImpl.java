package com.threeamigos.imageviewer.implementations.datamodel;

import com.threeamigos.imageviewer.implementations.datamodel.imagereaders.ApacheCommonsImagingImageReader;
import com.threeamigos.imageviewer.implementations.datamodel.imagereaders.JavaImageIOImageReader;
import com.threeamigos.imageviewer.interfaces.datamodel.ImageReader;
import com.threeamigos.imageviewer.interfaces.datamodel.ImageReaderFactory;
import com.threeamigos.imageviewer.interfaces.preferences.flavours.ImageHandlingPreferences;

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
