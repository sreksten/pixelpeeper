package com.threeamigos.imageviewer.interfaces.preferences.flavours;

import com.threeamigos.imageviewer.interfaces.preferences.ExifReaderFlavour;
import com.threeamigos.imageviewer.interfaces.preferences.ImageReaderFlavour;
import com.threeamigos.imageviewer.interfaces.preferences.Preferences;

public interface ImageHandlingPreferences extends Preferences {

	public static final boolean AUTOROTATION_DEFAULT = true;
	public static final boolean MOVEMENT_IN_PERCENTAGE = true;
	public static final boolean MOVEMENT_APPLIED_TO_ALL_IMAGES_DEFAULT = true;
	public static final int MIN_ZOOM_LEVEL = 10;
	public static final int ZOOM_LEVEL_DEFAULT = 100;
	public static final int MAX_ZOOM_LEVEL = 100;
	public static final ImageReaderFlavour IMAGE_READER_FLAVOUR_DEFAULT = ImageReaderFlavour.JAVA;
	public static final ExifReaderFlavour METADATA_READER_FLAVOUR_DEFAULT = ExifReaderFlavour.DREW_NOAKES;

	default String getDescription() {
		return "Image handling preferences";
	}

	public void setMovementAppliedToAllImages(boolean movementAppliesToAllImages);

	public boolean isMovementAppliedToAllImages();

	public void setAutorotation(boolean autorotation);

	public boolean isAutorotation();

	public void setMovementInPercentage(boolean movementInPercentage);

	public boolean isMovementInPercentage();

	public void setImageReaderFlavour(ImageReaderFlavour imageReaderFlavour);

	public ImageReaderFlavour getImageReaderFlavour();

	public void setExifReaderFlavour(ExifReaderFlavour exifReaderFlavour);

	public ExifReaderFlavour getExifReaderFlavour();

	public void setZoomLevel(int zoomLevel);

	public int getZoomLevel();

}
