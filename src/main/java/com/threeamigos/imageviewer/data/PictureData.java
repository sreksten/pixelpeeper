package com.threeamigos.imageviewer.data;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Collection;

import com.threeamigos.imageviewer.interfaces.datamodel.CannyEdgeDetector;
import com.threeamigos.imageviewer.interfaces.datamodel.CannyEdgeDetectorFactory;

public class PictureData {

	private final int orientation;
	private final ExifMap exifMap;
	private final File file;
	private final String filename;
	private final CannyEdgeDetectorFactory cannyEdgeDetectorFactory;

	private boolean orientationAdjusted = false;

	private int width;
	private int height;
	private BufferedImage image;
	private BufferedImage edgeImage;

	public PictureData(int width, int height, int orientation, ExifMap exifMap, BufferedImage image, File file, CannyEdgeDetectorFactory cannyEdgeDetectorFactory) {
		this.width = width;
		this.height = height;
		this.orientation = orientation;
		this.exifMap = exifMap;
		this.image = image;
		this.file = file;
		this.filename = file.getName();
		this.cannyEdgeDetectorFactory = cannyEdgeDetectorFactory;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public ExifMap getExifMap() {
		return exifMap;
	}

	public Collection<ExifTag> getAllTags() {
		return exifMap.getAllTags();
	}

	public boolean isTagPresent(ExifTag exifTag) {
		return exifMap.getAllTags().contains(exifTag);
	}

	public String getTagDescriptive(ExifTag exifTag) {
		return exifMap.getTagDescriptive(exifTag);
	}

	public Object getTagObject(ExifTag exifTag) {
		return exifMap.getTagObject(exifTag);
	}

	public BufferedImage getImage() {
		return image;
	}

	public File getFile() {
		return file;
	}

	public String getFilename() {
		return filename;
	}

	public void correctOrientation() {
		if (!orientationAdjusted) {
			image = ExifOrientation.correctOrientation(image, orientation);
			swapDimensionsIfNeeded();
			orientationAdjusted = true;
		}
	}

	public void undoOrientationCorrection() {
		if (orientationAdjusted) {
			image = ExifOrientation.undoOrientationCorrection(image, orientation);
			swapDimensionsIfNeeded();
			orientationAdjusted = false;
		}
	}

	private void swapDimensionsIfNeeded() {
		if (orientation > 4 && orientation <= 8) {
			int tmp = width;
			width = height;
			height = tmp;
		}
	}
	
	public boolean isEdgePictureLoaded() {
		return edgeImage != null;
	}

	public BufferedImage getEdgeImage() {
		if (edgeImage == null) {
			CannyEdgeDetector detector = cannyEdgeDetectorFactory.getCannyEdgeDetector();
			detector.setSourceImage(image);
			detector.process();
			edgeImage = detector.getEdgesImage();
		}
		return edgeImage;
	}
	
}
