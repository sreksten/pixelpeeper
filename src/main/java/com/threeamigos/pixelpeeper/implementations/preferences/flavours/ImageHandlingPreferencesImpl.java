package com.threeamigos.pixelpeeper.implementations.preferences.flavours;

import com.threeamigos.common.util.implementations.BasicPropertyChangeAware;
import com.threeamigos.pixelpeeper.interfaces.datamodel.CommunicationMessages;
import com.threeamigos.pixelpeeper.interfaces.preferences.ExifReaderFlavour;
import com.threeamigos.pixelpeeper.interfaces.preferences.ImageReaderFlavour;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavours.ImageHandlingPreferences;

public class ImageHandlingPreferencesImpl extends BasicPropertyChangeAware implements ImageHandlingPreferences {

	private boolean autorotation;
	private Disposition disposition;
	private int zoomLevel;
	private boolean normalizedForCrop;
	private boolean normalizedForFocalLength;
	private boolean relativeMovement;
	private boolean movementAppliedToAllImages;
	private boolean positionMiniatureVisible;
	private ImageReaderFlavour imageReaderFlavour;
	private ExifReaderFlavour exifReaderFlavour;

	@Override
	public void setAutorotation(boolean autorotation) {
		boolean oldAutorotation = this.autorotation;
		this.autorotation = autorotation;
		firePropertyChange(CommunicationMessages.AUTOROTATION_CHANGED, oldAutorotation, autorotation);
	}

	@Override
	public boolean isAutorotation() {
		return autorotation;
	}

	@Override
	public void setDisposition(Disposition disposition) {
		Disposition oldDisposition = this.disposition;
		this.disposition = disposition;
		firePropertyChange(CommunicationMessages.DISPOSITION_CHANGED, oldDisposition, disposition);
	}

	@Override
	public Disposition getDisposition() {
		return disposition;
	}

	@Override
	public void setZoomLevel(int zoomLevel) {
		int oldZoomLevel = this.zoomLevel;
		if (zoomLevel < MIN_ZOOM_LEVEL) {
			zoomLevel = (int) MIN_ZOOM_LEVEL;
		} else if (zoomLevel > MAX_ZOOM_LEVEL) {
			zoomLevel = (int) MAX_ZOOM_LEVEL;
		}
		this.zoomLevel = zoomLevel;
		firePropertyChange(CommunicationMessages.ZOOM_LEVEL_CHANGED, oldZoomLevel, zoomLevel);
	}

	@Override
	public int getZoomLevel() {
		return zoomLevel;
	}

	@Override
	public void setNormalizedForCrop(boolean normalizedForCrop) {
		boolean oldNormalizedForCrop = this.normalizedForCrop;
		this.normalizedForCrop = normalizedForCrop;
		firePropertyChange(CommunicationMessages.NORMALIZED_FOR_CROP_CHANGED, oldNormalizedForCrop, normalizedForCrop);
	}

	@Override
	public boolean isNormalizedForCrop() {
		return normalizedForCrop;
	}

	@Override
	public void setNormalizedForFocalLength(boolean normalizedForFocalLength) {
		boolean oldNormalizedForFocalLength = this.normalizedForFocalLength;
		this.normalizedForFocalLength = normalizedForFocalLength;
		firePropertyChange(CommunicationMessages.NORMALIZE_FOR_FOCAL_LENGTH_CHANGED, oldNormalizedForFocalLength,
				normalizedForFocalLength);
	}

	@Override
	public boolean isNormalizedForFocalLength() {
		return normalizedForFocalLength;
	}

	@Override
	public void setRelativeMovement(boolean relativeMovement) {
		boolean oldRelativeMovement = this.relativeMovement;
		this.relativeMovement = relativeMovement;
		firePropertyChange(CommunicationMessages.RELATIVE_MOVEMENT_CHANGED, oldRelativeMovement, relativeMovement);
	}

	@Override
	public boolean isRelativeMovement() {
		return relativeMovement;
	}

	@Override
	public void setMovementAppliedToAllImages(boolean movementAppliesToAllImages) {
		boolean oldMovementAppliedToAllImages = this.movementAppliedToAllImages;
		this.movementAppliedToAllImages = movementAppliesToAllImages;
		firePropertyChange(CommunicationMessages.MOVEMENT_APPLIED_TO_ALL_IMAGES_CHANGED, oldMovementAppliedToAllImages,
				movementAppliesToAllImages);
	}

	@Override
	public boolean isMovementAppliedToAllImages() {
		return movementAppliedToAllImages;
	}

	@Override
	public void setPositionMiniatureVisible(boolean positionMiniatureVisible) {
		boolean oldPositionMiniatureVisible = this.positionMiniatureVisible;
		this.positionMiniatureVisible = positionMiniatureVisible;
		firePropertyChange(CommunicationMessages.POSITION_MINIATURE_VISIBILITY_CHANGED, oldPositionMiniatureVisible,
				positionMiniatureVisible);
	}

	public boolean isPositionMiniatureVisible() {
		return positionMiniatureVisible;
	}

	@Override
	public void setImageReaderFlavour(ImageReaderFlavour imageReaderFlavour) {
		ImageReaderFlavour oldImageReaderFlavour = this.imageReaderFlavour;
		this.imageReaderFlavour = imageReaderFlavour;
		firePropertyChange(CommunicationMessages.IMAGE_READER_FLAVOUR_CHANGED, oldImageReaderFlavour,
				imageReaderFlavour);
	}

	@Override
	public ImageReaderFlavour getImageReaderFlavour() {
		return imageReaderFlavour;
	}

	@Override
	public void setExifReaderFlavour(ExifReaderFlavour exifReaderFlavour) {
		ExifReaderFlavour oldExifReaderFlavour = this.exifReaderFlavour;
		this.exifReaderFlavour = exifReaderFlavour;
		firePropertyChange(CommunicationMessages.EXIF_READER_FLAVOUR_CHANGED, oldExifReaderFlavour, exifReaderFlavour);
	}

	@Override
	public ExifReaderFlavour getExifReaderFlavour() {
		return exifReaderFlavour;
	}

	@Override
	public void loadDefaultValues() {
		autorotation = AUTOROTATION_DEFAULT;
		disposition = DISPOSITION_DEFAULT;
		zoomLevel = (int) ZOOM_LEVEL_DEFAULT;
		normalizedForCrop = NORMALIZED_FOR_CROP_DEFAULT;
		normalizedForFocalLength = NORMALIZED_FOR_FOCAL_LENGTH_DEFAULT;
		relativeMovement = MOVEMENT_IN_PERCENTAGE_DEFAULT;
		movementAppliedToAllImages = MOVEMENT_APPLIED_TO_ALL_IMAGES_DEFAULT;
		positionMiniatureVisible = POSITION_MINIATURE_VISIBLE_DEFAULT;
		imageReaderFlavour = IMAGE_READER_FLAVOUR_DEFAULT;
		exifReaderFlavour = METADATA_READER_FLAVOUR_DEFAULT;
	}

	@Override
	public void validate() {
		if (imageReaderFlavour == null) {
			throw new IllegalArgumentException("Invalid image reader flavour");
		}
		if (exifReaderFlavour == null) {
			throw new IllegalArgumentException("Invalid metadata reader flavour");
		}
		if (zoomLevel < 10 || zoomLevel > 100) {
			throw new IllegalArgumentException("Invalid zoom level");
		}
	}

}
