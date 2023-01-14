package com.threeamigos.imageviewer.implementations.datamodel;

import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import com.threeamigos.common.util.interfaces.ExceptionHandler;
import com.threeamigos.imageviewer.data.ExifMap;
import com.threeamigos.imageviewer.data.PictureData;
import com.threeamigos.imageviewer.interfaces.datamodel.CannyEdgeDetectorFactory;
import com.threeamigos.imageviewer.interfaces.datamodel.ExifImageReader;
import com.threeamigos.imageviewer.interfaces.preferences.WindowPreferences;

public class ExifImageReaderImpl implements ExifImageReader {

	private final WindowPreferences windowPreferences;
	private final CannyEdgeDetectorFactory cannyEdgeDetectorFactory;
	private final ExceptionHandler exceptionHandler;

	public ExifImageReaderImpl(WindowPreferences windowPreferences, CannyEdgeDetectorFactory cannyEdgeDetectorFactory,
			ExceptionHandler exceptionHandler) {
		this.windowPreferences = windowPreferences;
		this.cannyEdgeDetectorFactory = cannyEdgeDetectorFactory;
		this.exceptionHandler = exceptionHandler;
	}

	public ExifMap readMetadata(File file) {
		try {
			MetadataConsumer metadataConsumer = new MetadataConsumer(file);
			if (metadataConsumer.consume()) {
				return metadataConsumer.getExifMap();
			}
		} catch (Exception e) {
			exceptionHandler.handleException(e);
		}
		return null;
	}

	public PictureData readImage(File file) {

		ExifMap exifMap = readMetadata(file);

		int pictureOrientation = exifMap.getPictureOrientation();

		if (exifMap != null) {

			try {

				BufferedImage bufferedImage = ImageIO.read(file);

				PictureData pictureData = new PictureData(bufferedImage.getWidth(), bufferedImage.getHeight(),
						pictureOrientation, exifMap, bufferedImage, file, cannyEdgeDetectorFactory);

				if (windowPreferences.isAutorotation()) {
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
