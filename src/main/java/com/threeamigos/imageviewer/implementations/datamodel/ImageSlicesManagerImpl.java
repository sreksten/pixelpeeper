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
import com.threeamigos.imageviewer.interfaces.datamodel.CommonTagsHelper;
import com.threeamigos.imageviewer.interfaces.datamodel.CommunicationMessages;
import com.threeamigos.imageviewer.interfaces.datamodel.ImageSlice;
import com.threeamigos.imageviewer.interfaces.datamodel.ImageSlicesManager;
import com.threeamigos.imageviewer.interfaces.preferences.EdgesDetectorPreferences;
import com.threeamigos.imageviewer.interfaces.preferences.ExifTagPreferences;
import com.threeamigos.imageviewer.interfaces.ui.FontService;

public class ImageSlicesManagerImpl implements ImageSlicesManager, PropertyChangeListener {

	private final CommonTagsHelper commonTagsHelper;
	private final ExifTagPreferences tagPreferences;
	private final EdgesDetectorPreferences edgesDetectorPreferences;
	private final FontService fontService;

	private final PropertyChangeSupport propertyChangeSupport;

	private List<ImageSlice> imageSlices = new ArrayList<>();
	private List<ImageSlice> imageSlicesRecalculatingEdges = new ArrayList<>();

	private ImageSlice activeSlice;

	public ImageSlicesManagerImpl(CommonTagsHelper commonTagsHelper, ExifTagPreferences tagPreferences,
			EdgesDetectorPreferences edgesDetectorPreferences, FontService fontService) {
		this.commonTagsHelper = commonTagsHelper;
		this.tagPreferences = tagPreferences;
		this.edgesDetectorPreferences = edgesDetectorPreferences;
		this.fontService = fontService;

		propertyChangeSupport = new PropertyChangeSupport(this);
	}

	@Override
	public void clear() {
		imageSlices.clear();
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
				edgesDetectorPreferences, fontService);
		imageSlice.addPropertyChangeListener(this);
		imageSlices.add(imageSlice);
		return imageSlice;
	}

	@Override
	public void move(int deltaX, int deltaY, boolean movementAppliesToAllImages) {
		if (movementAppliesToAllImages) {
			for (ImageSlice imageSlice : imageSlices) {
				imageSlice.move(deltaX, deltaY);
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
	public void recalculateEdges() {
		synchronized (imageSlicesRecalculatingEdges) {
			if (imageSlicesRecalculatingEdges.isEmpty()) {
				imageSlicesRecalculatingEdges.addAll(imageSlices);
				imageSlices.forEach(ImageSlice::startEdgesCalculation);
			}
		}
		propertyChangeSupport.firePropertyChange(CommunicationMessages.EDGES_CALCULATION_STARTED, null, null);
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
		} else if (CommunicationMessages.EDGES_CALCULATION_COMPLETED.equals(evt.getPropertyName())) {
			handleEdgeCalculationCompleted(evt);
		}
	}

	private void handleEdgeCalculationCompleted(PropertyChangeEvent evt) {
		ImageSlice imageSlice = (ImageSlice) evt.getNewValue();
		imageSlicesRecalculatingEdges.remove(imageSlice);
		propertyChangeSupport.firePropertyChange(CommunicationMessages.EDGES_CALCULATION_COMPLETED, null, null);
	}
}
