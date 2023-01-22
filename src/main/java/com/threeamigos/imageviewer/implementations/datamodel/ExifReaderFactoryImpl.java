package com.threeamigos.imageviewer.implementations.datamodel;

import com.threeamigos.imageviewer.implementations.datamodel.metadatareaders.DrewNoakesExifReader;
import com.threeamigos.imageviewer.interfaces.datamodel.ExifReader;
import com.threeamigos.imageviewer.interfaces.datamodel.ExifReaderFactory;
import com.threeamigos.imageviewer.interfaces.preferences.flavours.ImageHandlingPreferences;

public class ExifReaderFactoryImpl implements ExifReaderFactory {

	private final ImageHandlingPreferences imageHandlingPreferences;

	public ExifReaderFactoryImpl(ImageHandlingPreferences imageHandlingPreferences) {
		this.imageHandlingPreferences = imageHandlingPreferences;
	}

	@Override
	public ExifReader getExifReader() {
		switch (imageHandlingPreferences.getExifReaderFlavour()) {
		case DREW_NOAKES:
			return new DrewNoakesExifReader();
		}
		throw new IllegalArgumentException("No Exif reader was defined for flavour "
				+ imageHandlingPreferences.getImageReaderFlavour().getDescription());
	}

}
