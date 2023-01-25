package com.threeamigos.imageviewer.implementations.preferences.flavours;

import com.threeamigos.imageviewer.interfaces.preferences.ExifReaderFlavour;
import com.threeamigos.imageviewer.interfaces.preferences.ImageReaderFlavour;
import com.threeamigos.imageviewer.interfaces.preferences.flavours.ImageHandlingPreferences;

public class ImageHandlingPreferencesImpl implements ImageHandlingPreferences {

	private boolean autorotation;
	private boolean movementInPercentage;
	private boolean movementAppliedToAllImages;
	private int zoomLevel;
	private ImageReaderFlavour imageReaderFlavour;
	private ExifReaderFlavour metadataReaderFlavour;

	@Override
	public void setAutorotation(boolean autorotation) {
		this.autorotation = autorotation;
	}

	@Override
	public boolean isAutorotation() {
		return autorotation;
	}

	@Override
	public void setMovementInPercentage(boolean movementInPercentage) {
		this.movementInPercentage = movementInPercentage;
	}

	@Override
	public boolean isMovementInPercentage() {
		return movementInPercentage;
	}

	@Override
	public void setMovementAppliedToAllImages(boolean movementAppliesToAllImages) {
		this.movementAppliedToAllImages = movementAppliesToAllImages;
	}

	@Override
	public boolean isMovementAppliedToAllImages() {
		return movementAppliedToAllImages;
	}

	@Override
	public void setZoomLevel(int zoomLevel) {
		this.zoomLevel = zoomLevel;
	}

	@Override
	public int getZoomLevel() {
		return zoomLevel;
	}

	@Override
	public void setImageReaderFlavour(ImageReaderFlavour imageReaderFlavour) {
		this.imageReaderFlavour = imageReaderFlavour;
	}

	@Override
	public ImageReaderFlavour getImageReaderFlavour() {
		return imageReaderFlavour;
	}

	@Override
	public void setExifReaderFlavour(ExifReaderFlavour metadataReaderFlavour) {
		this.metadataReaderFlavour = metadataReaderFlavour;
	}

	@Override
	public ExifReaderFlavour getExifReaderFlavour() {
		return metadataReaderFlavour;
	}

	@Override
	public void loadDefaultValues() {
		autorotation = AUTOROTATION_DEFAULT;
		movementAppliedToAllImages = MOVEMENT_APPLIED_TO_ALL_IMAGES_DEFAULT;
		zoomLevel = ZOOM_LEVEL_DEFAULT;
		imageReaderFlavour = IMAGE_READER_FLAVOUR_DEFAULT;
		metadataReaderFlavour = METADATA_READER_FLAVOUR_DEFAULT;
	}

	@Override
	public void validate() {
		if (imageReaderFlavour == null) {
			throw new IllegalArgumentException("Invalid image reader flavour");
		}
		if (metadataReaderFlavour == null) {
			throw new IllegalArgumentException("Invalid metadata reader flavour");
		}
		if (zoomLevel < 10 || zoomLevel > 100) {
			throw new IllegalArgumentException("Invalid zoom level");
		}
	}
}
