package com.threeamigos.pixelpeeper.implementations.datamodel;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Optional;

import com.threeamigos.common.util.interfaces.MessageHandler;
import com.threeamigos.pixelpeeper.data.ExifMap;
import com.threeamigos.pixelpeeper.data.PictureData;
import com.threeamigos.pixelpeeper.implementations.helpers.ExifOrientationHelper;
import com.threeamigos.pixelpeeper.interfaces.datamodel.ExifCache;
import com.threeamigos.pixelpeeper.interfaces.datamodel.ExifImageReader;
import com.threeamigos.pixelpeeper.interfaces.datamodel.ImageReaderFactory;
import com.threeamigos.pixelpeeper.interfaces.edgedetect.EdgesDetectorFactory;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavours.EdgesDetectorPreferences;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavours.ImageHandlingPreferences;

public class ExifImageReaderImpl implements ExifImageReader {

	private final ImageHandlingPreferences imageHandlingPreferences;
	private final ImageReaderFactory imageReaderFactory;
	private final EdgesDetectorPreferences edgesDetectorPreferences;
	private final EdgesDetectorFactory edgesDetectorFactory;
	private final MessageHandler messageHandler;

	private final ExifCache exifCache;

	public ExifImageReaderImpl(ImageHandlingPreferences imageHandlingPreferences, ImageReaderFactory imageReaderFactory,
			ExifCache exifCache, EdgesDetectorPreferences edgesDetectorPreferences,
			EdgesDetectorFactory edgesDetectorFactory, MessageHandler messageHandler) {
		this.imageHandlingPreferences = imageHandlingPreferences;
		this.imageReaderFactory = imageReaderFactory;
		this.exifCache = exifCache;
		this.edgesDetectorPreferences = edgesDetectorPreferences;
		this.edgesDetectorFactory = edgesDetectorFactory;
		this.messageHandler = messageHandler;
	}

	public PictureData readImage(File file) {

		if (!file.exists()) {
			messageHandler.handleErrorMessage("File " + file.getName() + " was not found.");
			return null;
		}

		if (!file.canRead()) {
			messageHandler.handleErrorMessage("File " + file.getName() + " cannot be read.");
			return null;
		}

		ExifMap exifMap;

		Optional<ExifMap> exifMapOpt = exifCache.getExifMap(file);

		int pictureOrientation = ExifOrientationHelper.AS_IS;

		if (exifMapOpt.isPresent()) {
			exifMap = exifMapOpt.get();
			pictureOrientation = exifMap.getPictureOrientation();
		} else {
			exifMap = new ExifMap();
		}

		try {

			BufferedImage bufferedImage = imageReaderFactory.getImageReader().readImage(file);

			PictureData pictureData = new PictureData(pictureOrientation, exifMap, bufferedImage, file,
					imageHandlingPreferences, edgesDetectorPreferences, edgesDetectorFactory);

			if (imageHandlingPreferences.isAutorotation()) {
				pictureData.correctOrientation();
			}

			return pictureData;

		} catch (Exception e) {
			messageHandler.handleException(e);
		}

		return null;
	}

}
