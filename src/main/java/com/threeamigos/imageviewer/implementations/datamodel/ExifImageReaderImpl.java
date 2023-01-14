package com.threeamigos.imageviewer.implementations.datamodel;

import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import com.threeamigos.imageviewer.data.ExifMap;
import com.threeamigos.imageviewer.data.ExifOrientation;
import com.threeamigos.imageviewer.data.PictureData;
import com.threeamigos.imageviewer.interfaces.datamodel.CannyEdgeDetectorFactory;
import com.threeamigos.imageviewer.interfaces.preferences.WindowPreferences;

public class ExifImageReaderImpl {

	private final WindowPreferences windowPreferences;
	private final CannyEdgeDetectorFactory cannyEdgeDetectorFactory;

	private PictureData pictureData;
	private int pictureOrientation = ExifOrientation.AS_IS;

	public ExifImageReaderImpl(WindowPreferences windowPreferences, CannyEdgeDetectorFactory cannyEdgeDetectorFactory) {
		this.windowPreferences = windowPreferences;
		this.cannyEdgeDetectorFactory = cannyEdgeDetectorFactory;
	}

	public ExifMap readMetadata(File file) {
		try {
			MetadataConsumer metadataConsumer = new MetadataConsumer(file);
			if (metadataConsumer.consume()) {
				pictureOrientation = metadataConsumer.getPictureOrientation();
				return metadataConsumer.getExifMap();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	public boolean readImage(File file) {

		ExifMap exifMap = readMetadata(file);

		if (exifMap != null) {

			try {

				BufferedImage bufferedImage = ImageIO.read(file);

				pictureData = new PictureData(bufferedImage.getWidth(), bufferedImage.getHeight(), pictureOrientation,
						exifMap, bufferedImage, file, cannyEdgeDetectorFactory);

				if (windowPreferences.isAutorotation()) {
					pictureData.correctOrientation();
				}

				return true;

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return false;
	}

	public PictureData getPictureData() {
		return pictureData;
	}

}
