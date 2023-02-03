package com.threeamigos.imageviewer.implementations.datamodel;

import java.awt.Rectangle;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.threeamigos.imageviewer.data.PictureData;
import com.threeamigos.imageviewer.interfaces.datamodel.CommunicationMessages;
import com.threeamigos.imageviewer.interfaces.datamodel.ImageSlice;
import com.threeamigos.imageviewer.interfaces.datamodel.ImageSlicesManager;
import com.threeamigos.imageviewer.interfaces.datamodel.TagsClassifier;
import com.threeamigos.imageviewer.interfaces.preferences.flavours.EdgesDetectorPreferences;
import com.threeamigos.imageviewer.interfaces.preferences.flavours.ExifTagPreferences;
import com.threeamigos.imageviewer.interfaces.preferences.flavours.ImageHandlingPreferences;
import com.threeamigos.imageviewer.interfaces.ui.FontService;

public class ImageSlicesManagerImpl implements ImageSlicesManager, PropertyChangeListener {

	private final TagsClassifier commonTagsHelper;
	private final ExifTagPreferences tagPreferences;
	private final ImageHandlingPreferences imageHandlingPreferences;
	private final EdgesDetectorPreferences edgesDetectorPreferences;
	private final FontService fontService;

	private final PropertyChangeSupport propertyChangeSupport;

	private List<ImageSlice> imageSlices = new ArrayList<>();
	private List<ImageSlice> imageSlicesCalculatingEdges = new ArrayList<>();

	private ImageSlice activeSlice;

	private Float firstFocalLength;
	private Float firstFocalLength35mmEquivalent;
	private Float firstCropFactor;

	public ImageSlicesManagerImpl(TagsClassifier commonTagsHelper, ExifTagPreferences tagPreferences,
			ImageHandlingPreferences imageHandlingPreferences, EdgesDetectorPreferences edgesDetectorPreferences,
			FontService fontService) {
		this.commonTagsHelper = commonTagsHelper;
		this.tagPreferences = tagPreferences;
		this.imageHandlingPreferences = imageHandlingPreferences;
		this.edgesDetectorPreferences = edgesDetectorPreferences;
		this.fontService = fontService;

		propertyChangeSupport = new PropertyChangeSupport(this);
	}

	@Override
	public void clear() {
		imageSlices.clear();
		firstFocalLength35mmEquivalent = null;
		firstCropFactor = null;
	}

	@Override
	public boolean hasLoadedImages() {
		return !imageSlices.isEmpty();
	}

	@Override
	public Collection<ImageSlice> getImageSlices() {
		return Collections.unmodifiableCollection(imageSlices);
	}

	@Override
	public void reframeImageSlices(int panelWidth, int panelHeight) {
		if (!imageSlices.isEmpty()) {
			int sliceWidth = panelWidth / imageSlices.size();
			int sliceHeight = panelHeight;
			int currentScreenOffsetX = 0;
			for (ImageSlice slice : imageSlices) {
				Rectangle sliceRectangle = new Rectangle(currentScreenOffsetX, 0, sliceWidth, sliceHeight);
				slice.setLocation(sliceRectangle);
				currentScreenOffsetX += sliceWidth;
			}
		}
	}

	@Override
	public ImageSlice createImageSlice(PictureData pictureData) {
		ImageSlice imageSlice = new ImageSliceImpl(pictureData, commonTagsHelper, tagPreferences,
				imageHandlingPreferences, edgesDetectorPreferences, fontService);
		imageSlice.addPropertyChangeListener(this);
		imageSlices.add(imageSlice);
		Float focalLength = imageSlice.getPictureData().getFocalLength();
		Float focalLength35mmEquivalent = imageSlice.getPictureData().getFocalLength35mmEquivalent();
		Float cropFactor = imageSlice.getPictureData().getCropFactor();
		if (focalLength != null && focalLength35mmEquivalent != null && cropFactor != null
				&& firstFocalLength35mmEquivalent == null) {
			firstFocalLength = focalLength;
			firstFocalLength35mmEquivalent = focalLength35mmEquivalent;
			firstCropFactor = cropFactor;
		}
		return imageSlice;
	}

	@Override
	public void move(int deltaX, int deltaY, boolean movementAppliesToAllImages) {
		if (movementAppliesToAllImages) {
			if (imageHandlingPreferences.isRelativeMovement()) {
				if (activeSlice != null) {
					int activeSliceWidth = activeSlice.getPictureData().getWidth();
					int activeSliceHeight = activeSlice.getPictureData().getHeight();
					double percentageX = (double) deltaX / (double) activeSliceWidth;
					double percentageY = (double) deltaY / (double) activeSliceHeight;
					for (ImageSlice imageSlice : imageSlices) {
						if (imageSlice != activeSlice) {
							double imageSliceWidth = imageSlice.getPictureData().getWidth();
							double imageSliceHeight = imageSlice.getPictureData().getHeight();
							imageSlice.move(percentageX * imageSliceWidth * imageSliceWidth / (double) activeSliceWidth,
									percentageY * imageSliceHeight * imageSliceHeight / (double) activeSliceHeight);
						} else {
							imageSlice.move(deltaX, deltaY);
						}
					}
				}
			} else {
				for (ImageSlice imageSlice : imageSlices) {
					imageSlice.move(deltaX, deltaY);
				}
			}
		} else {
			if (activeSlice != null) {
				activeSlice.move(deltaX, deltaY);
			}
		}
	}

	@Override
	public void resetMovement() {
		imageSlices.forEach(ImageSlice::resetMovement);
	}

	@Override
	public void changeZoomLevel() {
		float baseZoomLevel = imageHandlingPreferences.getZoomLevel();
		for (ImageSlice imageSlice : imageSlices) {
			float zoomLevel = baseZoomLevel;
			if (imageHandlingPreferences.isNormalizedForCrop()) {
				Float cropFactor = imageSlice.getPictureData().getCropFactor();
				if (firstCropFactor != null && cropFactor != null) {
					zoomLevel = zoomLevel * firstCropFactor / cropFactor;
				}
			}
			if (imageHandlingPreferences.isNormalizedForFocalLength()) {
				Float focalLength = imageSlice.getPictureData().getFocalLength();
				if (firstFocalLength != null && focalLength != null) {
					zoomLevel = zoomLevel * firstFocalLength / focalLength;
				}
			}
			imageSlice.changeZoomLevel(zoomLevel);
		}
	}

	@Override
	public void setActiveSlice(int x, int y) {
		for (ImageSlice currentSlice : imageSlices) {
			if (currentSlice.contains(x, y)) {
				activeSlice = currentSlice;
				activeSlice.setSelected(true);
				break;
			}
		}
	}

	@Override
	public void resetActiveSlice() {
		if (activeSlice != null) {
			activeSlice.setSelected(false);
		}
		activeSlice = null;
	}

	@Override
	public void calculateEdges() {
		synchronized (imageSlicesCalculatingEdges) {
			if (!imageSlicesCalculatingEdges.isEmpty()) {
				imageSlices.forEach(ImageSlice::releaseEdges);
				imageSlicesCalculatingEdges.clear();
			}
			imageSlicesCalculatingEdges.addAll(imageSlices);
			imageSlices.forEach(ImageSlice::startEdgesCalculation);
		}
		propertyChangeSupport.firePropertyChange(CommunicationMessages.EDGES_CALCULATION_STARTED, null, null);
	}

	@Override
	public void releaseEdges() {
		imageSlices.forEach(ImageSlice::releaseEdges);
	}

	@Override
	public void addPropertyChangeListener(PropertyChangeListener pcl) {
		propertyChangeSupport.addPropertyChangeListener(pcl);
	}

	@Override
	public void removePropertyChangeListener(PropertyChangeListener pcl) {
		propertyChangeSupport.removePropertyChangeListener(pcl);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (CommunicationMessages.EDGES_CALCULATION_STARTED.equals(evt.getPropertyName())) {
			// We don't care about this
		} else if (CommunicationMessages.EDGES_CALCULATION_COMPLETED.equals(evt.getPropertyName())) {
			handleEdgeCalculationCompleted(evt);
		}
	}

	private void handleEdgeCalculationCompleted(PropertyChangeEvent evt) {
		ImageSlice imageSlice = (ImageSlice) evt.getNewValue();
		imageSlicesCalculatingEdges.remove(imageSlice);
		propertyChangeSupport.firePropertyChange(CommunicationMessages.EDGES_CALCULATION_COMPLETED, null, null);
	}
}
