package com.threeamigos.imageviewer.implementations.preferences.flavours;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import com.threeamigos.imageviewer.interfaces.datamodel.CommunicationMessages;
import com.threeamigos.imageviewer.interfaces.preferences.ExifReaderFlavour;
import com.threeamigos.imageviewer.interfaces.preferences.ImageReaderFlavour;
import com.threeamigos.imageviewer.interfaces.preferences.flavours.PropertyChangeAwareImageHandlingPreferences;

public class ImageHandlingPreferencesImpl implements PropertyChangeAwareImageHandlingPreferences {

	private boolean autorotation;
	private int zoomLevel;
	private boolean normalizedForCrop;
	private boolean normalizedForFocalLength;
	private boolean movementInPercentage;
	private boolean movementAppliedToAllImages;
	private boolean positionMiniatureVisible;
	private ImageReaderFlavour imageReaderFlavour;
	private ExifReaderFlavour metadataReaderFlavour;

	// transient to make Gson serializer ignore this
	private final transient PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

	@Override
	public void setAutorotation(boolean autorotation) {
		this.autorotation = autorotation;
		propertyChangeSupport.firePropertyChange(CommunicationMessages.AUTOROTATION_CHANGED, null, null);
	}

	@Override
	public boolean isAutorotation() {
		return autorotation;
	}

	@Override
	public void setZoomLevel(int zoomLevel) {
		int previousLevel = this.zoomLevel;
		if (zoomLevel < MIN_ZOOM_LEVEL) {
			zoomLevel = MIN_ZOOM_LEVEL;
		} else if (zoomLevel > MAX_ZOOM_LEVEL) {
			zoomLevel = MAX_ZOOM_LEVEL;
		}
		this.zoomLevel = zoomLevel;
		propertyChangeSupport.firePropertyChange(CommunicationMessages.ZOOM_LEVEL_CHANGED, previousLevel, zoomLevel);
	}

	@Override
	public int getZoomLevel() {
		return zoomLevel;
	}

	@Override
	public void setNormalizedForCrop(boolean normalizedForCrop) {
		this.normalizedForCrop = normalizedForCrop;
		propertyChangeSupport.firePropertyChange(CommunicationMessages.ZOOM_LEVEL_CHANGED, null, null);
	}

	@Override
	public boolean isNormalizedForCrop() {
		return normalizedForCrop;
	}

	@Override
	public void setNormalizedForFocalLength(boolean normalizedForFocalLength) {
		this.normalizedForFocalLength = normalizedForFocalLength;
		propertyChangeSupport.firePropertyChange(CommunicationMessages.ZOOM_LEVEL_CHANGED, null, null);
	}

	@Override
	public boolean isNormalizedForFocalLength() {
		return normalizedForFocalLength;
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
		propertyChangeSupport.firePropertyChange(CommunicationMessages.MOVEMENT_APPLIED_TO_ALL_IMAGES_CHANGED, null,
				null);
	}

	@Override
	public boolean isMovementAppliedToAllImages() {
		return movementAppliedToAllImages;
	}

	@Override
	public void setPositionMiniatureVisible(boolean positionMiniatureVisible) {
		this.positionMiniatureVisible = positionMiniatureVisible;
		propertyChangeSupport.firePropertyChange(CommunicationMessages.REQUEST_REPAINT, null, null);
	}

	public boolean isPositionMiniatureVisible() {
		return positionMiniatureVisible;
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
		movementInPercentage = MOVEMENT_IN_PERCENTAGE_DEFAULT;
		movementAppliedToAllImages = MOVEMENT_APPLIED_TO_ALL_IMAGES_DEFAULT;
		positionMiniatureVisible = POSITION_MINIATURE_VISIBLE_DEFAULT;
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

	@Override
	public void addPropertyChangeListener(PropertyChangeListener pcl) {
		propertyChangeSupport.addPropertyChangeListener(pcl);
	}

	@Override
	public void removePropertyChangeListener(PropertyChangeListener pcl) {
		propertyChangeSupport.removePropertyChangeListener(pcl);
	}

}
