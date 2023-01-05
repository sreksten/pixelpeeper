package com.threeamigos.imageviewer.data;

import java.awt.image.BufferedImage;
import java.io.File;

public class PictureData {

	private final int orientation;
	private final ExifMap exifMap;
	private final File file;
	private final String filename;

	private boolean orientationAdjusted = false;

	private int width;
	private int height;
	private BufferedImage image;

	public PictureData(int width, int height, int orientation, ExifMap exifMap, BufferedImage image, File file) {
		this.width = width;
		this.height = height;
		this.orientation = orientation;
		this.exifMap = exifMap;
		this.image = image;
		this.file = file;
		this.filename = file.getName();
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
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
			image = ExifOrientationCorrector.correctOrientation(image, orientation);
			swapDimensionsIfNeeded();
			orientationAdjusted = true;
		}
	}

	public void undoOrientationCorrection() {
		if (orientationAdjusted) {
			image = ExifOrientationCorrector.undoOrientationCorrection(image, orientation);
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

}
