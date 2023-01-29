package com.threeamigos.imageviewer.implementations.datamodel;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Optional;

import com.threeamigos.common.util.interfaces.MessageHandler;
import com.threeamigos.imageviewer.data.ExifMap;
import com.threeamigos.imageviewer.data.PictureData;
import com.threeamigos.imageviewer.implementations.helpers.ExifOrientationHelper;
import com.threeamigos.imageviewer.interfaces.datamodel.ExifImageReader;
import com.threeamigos.imageviewer.interfaces.datamodel.ExifReaderFactory;
import com.threeamigos.imageviewer.interfaces.datamodel.ImageReaderFactory;
import com.threeamigos.imageviewer.interfaces.edgedetect.EdgesDetectorFactory;
import com.threeamigos.imageviewer.interfaces.preferences.flavours.EdgesDetectorPreferences;
import com.threeamigos.imageviewer.interfaces.preferences.flavours.ImageHandlingPreferences;

public class ExifImageReaderImpl implements ExifImageReader {

	private final ImageHandlingPreferences imageHandlingPreferences;
	private final ImageReaderFactory imageReaderFactory;
	private final ExifReaderFactory exifReaderFactory;
	private final EdgesDetectorPreferences edgesDetectorPreferences;
	private final EdgesDetectorFactory edgesDetectorFactory;
	private final MessageHandler messageHandler;

	public ExifImageReaderImpl(ImageHandlingPreferences imageHandlingPreferences, ImageReaderFactory imageReaderFactory,
			ExifReaderFactory exifReaderFactory, EdgesDetectorPreferences edgesDetectorPreferences,
			EdgesDetectorFactory edgesDetectorFactory, MessageHandler messageHandler) {
		this.imageHandlingPreferences = imageHandlingPreferences;
		this.imageReaderFactory = imageReaderFactory;
		this.exifReaderFactory = exifReaderFactory;
		this.edgesDetectorPreferences = edgesDetectorPreferences;
		this.edgesDetectorFactory = edgesDetectorFactory;
		this.messageHandler = messageHandler;
	}

	public Optional<ExifMap> readExifMap(File file) {
		return exifReaderFactory.getExifReader().readMetadata(file);
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

		Optional<ExifMap> exifMapOpt = readExifMap(file);

		int pictureOrientation = ExifOrientationHelper.AS_IS;

		if (exifMapOpt.isPresent()) {
			exifMap = exifMapOpt.get();
			pictureOrientation = exifMap.getPictureOrientation();
		} else {
			exifMap = new ExifMap();
		}

		try {

			BufferedImage bufferedImage = imageReaderFactory.getImageReader().readImage(file);

			PictureData pictureData = new PictureData(bufferedImage.getWidth(), bufferedImage.getHeight(),
					pictureOrientation, exifMap, bufferedImage, file, imageHandlingPreferences,
					edgesDetectorPreferences, edgesDetectorFactory);

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
