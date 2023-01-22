package com.threeamigos.imageviewer.implementations.datamodel;

import java.awt.image.BufferedImage;
import java.io.File;

import com.threeamigos.common.util.interfaces.ExceptionHandler;
import com.threeamigos.imageviewer.data.ExifMap;
import com.threeamigos.imageviewer.data.PictureData;
import com.threeamigos.imageviewer.interfaces.datamodel.ExifImageReader;
import com.threeamigos.imageviewer.interfaces.datamodel.ImageReaderFactory;
import com.threeamigos.imageviewer.interfaces.edgedetect.EdgesDetectorFactory;
import com.threeamigos.imageviewer.interfaces.preferences.flavours.ImageHandlingPreferences;

public class ExifImageReaderImpl implements ExifImageReader {

	private final ImageHandlingPreferences imageHandlingPreferences;
	private final ImageReaderFactory imageReaderFactory;
	private final EdgesDetectorFactory edgesDetectorFactory;
	private final ExceptionHandler exceptionHandler;

	public ExifImageReaderImpl(ImageHandlingPreferences imageHandlingPreferences, ImageReaderFactory imageReaderFactory,
			EdgesDetectorFactory edgesDetectorFactory, ExceptionHandler exceptionHandler) {
		this.imageHandlingPreferences = imageHandlingPreferences;
		this.imageReaderFactory = imageReaderFactory;
		this.edgesDetectorFactory = edgesDetectorFactory;
		this.exceptionHandler = exceptionHandler;
	}

	public ExifMap readMetadata(File file) {
		MetadataConsumer metadataConsumer = new MetadataConsumer(file);
		if (metadataConsumer.consume()) {
			return metadataConsumer.getExifMap();
		}
		return null;
	}

	public PictureData readImage(File file) {

		ExifMap exifMap = readMetadata(file);

		int pictureOrientation = exifMap.getPictureOrientation();

		if (exifMap != null) {

			try {

				BufferedImage bufferedImage = imageReaderFactory.getImageReader().readImage(file);

				PictureData pictureData = new PictureData(bufferedImage.getWidth(), bufferedImage.getHeight(),
						pictureOrientation, exifMap, bufferedImage, file, edgesDetectorFactory);

				if (imageHandlingPreferences.isAutorotation()) {
					pictureData.correctOrientation();
				}

				return pictureData;

			} catch (Exception e) {
				exceptionHandler.handleException(e);
			}
		}

		return null;
	}

}
