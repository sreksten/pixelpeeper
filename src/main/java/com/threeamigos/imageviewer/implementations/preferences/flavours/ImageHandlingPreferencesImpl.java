package com.threeamigos.imageviewer.implementations.preferences.flavours;

import com.threeamigos.imageviewer.interfaces.preferences.ImageReaderFlavour;
import com.threeamigos.imageviewer.interfaces.preferences.flavours.ImageHandlingPreferences;

public class ImageHandlingPreferencesImpl implements ImageHandlingPreferences {

	private boolean autorotation;
	private boolean movementAppliedToAllImages;
	private ImageReaderFlavour imageReaderFlavour;

	@Override
	public void setAutorotation(boolean autorotation) {
		this.autorotation = autorotation;
	}

	@Override
	public boolean isAutorotation() {
		return autorotation;
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
	public void loadDefaultValues() {
		autorotation = AUTOROTATION_DEFAULT;
		movementAppliedToAllImages = MOVEMENT_APPLIED_TO_ALL_IMAGES_DEFAULT;
		imageReaderFlavour = IMAGE_READER_FLAVOUR_DEFAULT;
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
	public void validate() {
	}
}
