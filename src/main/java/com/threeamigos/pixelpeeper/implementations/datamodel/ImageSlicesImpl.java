package com.threeamigos.pixelpeeper.implementations.datamodel;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.threeamigos.common.util.interfaces.ui.FontService;
import com.threeamigos.pixelpeeper.data.PictureData;
import com.threeamigos.pixelpeeper.interfaces.datamodel.CommunicationMessages;
import com.threeamigos.pixelpeeper.interfaces.datamodel.ImageSlice;
import com.threeamigos.pixelpeeper.interfaces.datamodel.ImageSlices;
import com.threeamigos.pixelpeeper.interfaces.datamodel.TagsClassifier;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavours.DrawingPreferences;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavours.EdgesDetectorPreferences;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavours.ExifTagPreferences;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavours.ImageHandlingPreferences;

public class ImageSlicesImpl implements ImageSlices, PropertyChangeListener {

	private final TagsClassifier commonTagsHelper;
	private final ExifTagPreferences tagPreferences;
	private final ImageHandlingPreferences imageHandlingPreferences;
	private final DrawingPreferences drawingPreferences;
	private final EdgesDetectorPreferences edgesDetectorPreferences;
	private final FontService fontService;

	private final PropertyChangeSupport propertyChangeSupport;

	private List<ImageSlice> imageSlices = new ArrayList<>();
	private List<ImageSlice> imageSlicesCalculatingEdges = new ArrayList<>();

	private ImageSlice activeSlice;
	private ImageSlice lastActiveSlice;

	public ImageSlicesImpl(TagsClassifier commonTagsHelper, ExifTagPreferences tagPreferences,
			ImageHandlingPreferences imageHandlingPreferences, DrawingPreferences drawingPreferences,
			EdgesDetectorPreferences edgesDetectorPreferences, FontService fontService) {
		this.commonTagsHelper = commonTagsHelper;
		this.tagPreferences = tagPreferences;
		this.imageHandlingPreferences = imageHandlingPreferences;
		this.drawingPreferences = drawingPreferences;
		this.edgesDetectorPreferences = edgesDetectorPreferences;
		this.fontService = fontService;

		propertyChangeSupport = new PropertyChangeSupport(this);
	}

	@Override
	public void clear() {
		imageSlices.clear();
		lastActiveSlice = null;
	}

	@Override
	public void add(PictureData pictureData) {
		ImageSlice imageSlice = new ImageSliceImpl(pictureData, commonTagsHelper, tagPreferences,
				imageHandlingPreferences, drawingPreferences, edgesDetectorPreferences, fontService);
		imageSlice.addPropertyChangeListener(this);
		imageSlices.add(imageSlice);
	}

	@Override
	public void sort() {
		Collections.sort(imageSlices,
				(i1, i2) -> i1.getPictureData().getFilename().compareTo(i2.getPictureData().getFilename()));
	}

	@Override
	public boolean isNotEmpty() {
		return !imageSlices.isEmpty();
	}

	@Override
	public void reframe(int panelWidth, int panelHeight) {
		if (!imageSlices.isEmpty()) {
			Iterator<Rectangle> rectanglesIterator = RectanglesProducer.createRectangles(panelWidth, panelHeight,
					imageHandlingPreferences.getDisposition(), imageSlices.size()).iterator();
			for (ImageSlice imageSlice : imageSlices) {
				imageSlice.setLocation(rectanglesIterator.next());
			}
		}
	}

	@Override
	public void move(final int deltaX, final int deltaY, boolean movementAppliesToAllImages) {
		if (activeSlice == null) {
			return;
		}
		if (movementAppliesToAllImages) {
			if (imageHandlingPreferences.isRelativeMovement()) {

				int notVisibleActiveSliceWidth = activeSlice.getPictureData().getWidth()
						- activeSlice.getLocation().width;
				int notVisibleActiveSliceHeight = activeSlice.getPictureData().getHeight()
						- activeSlice.getLocation().height;

				for (ImageSlice currentSlice : imageSlices) {
					if (currentSlice == activeSlice) {
						currentSlice.move(deltaX, deltaY);
					} else {
						int notVisibleCurrentSliceWidth = currentSlice.getPictureData().getWidth()
								- currentSlice.getLocation().width;
						double offsetX;
						if (notVisibleCurrentSliceWidth > 0) {
							if (notVisibleActiveSliceWidth < 0) {
								offsetX = deltaX;
							} else {
								offsetX = (double) deltaX * notVisibleCurrentSliceWidth / notVisibleActiveSliceWidth;
							}
						} else {
							offsetX = 0.0d;
						}

						int notVisibleCurrentSliceHeight = currentSlice.getPictureData().getHeight()
								- currentSlice.getLocation().height;
						double offsetY;
						if (notVisibleCurrentSliceHeight > 0) {
							if (notVisibleActiveSliceHeight < 0) {
								offsetY = deltaY;
							} else {
								offsetY = (double) deltaY * notVisibleCurrentSliceHeight / notVisibleActiveSliceHeight;
							}
						} else {
							offsetY = 0.0d;
						}

						currentSlice.move(offsetX, offsetY);
					}
				}
			} else {
				for (ImageSlice imageSlice : imageSlices) {
					imageSlice.move(deltaX, deltaY);
				}
			}
		} else {
			activeSlice.move(deltaX, deltaY);
		}
	}

	@Override
	public void resetMovement() {
		imageSlices.forEach(ImageSlice::resetMovement);
	}

	@Override
	public void updateZoomLevel() {
		float baseZoomLevel = imageHandlingPreferences.getZoomLevel();

		Float minCropFactor = null;
		if (imageHandlingPreferences.isNormalizedForCrop()) {
			for (ImageSlice imageSlice : imageSlices) {
				Float cropFactor = imageSlice.getPictureData().getCropFactor();
				if (minCropFactor == null || cropFactor != null && minCropFactor > cropFactor) {
					minCropFactor = cropFactor;
				}
			}
		}

		Float minFocalLength = null;
		if (imageHandlingPreferences.isNormalizedForFocalLength()) {
			for (ImageSlice imageSlice : imageSlices) {
				Float focalLength = imageSlice.getPictureData().getFocalLength35mmEquivalent();
				if (minFocalLength == null || focalLength != null && minFocalLength > focalLength) {
					minFocalLength = focalLength;
				}
			}
		}

		for (ImageSlice imageSlice : imageSlices) {
			float zoomLevel = baseZoomLevel;
			if (imageHandlingPreferences.isNormalizedForCrop()) {
				Float cropFactor = imageSlice.getPictureData().getCropFactor();
				if (minCropFactor != null && cropFactor != null) {
					zoomLevel = zoomLevel * minCropFactor / cropFactor;
				}
			}
			if (imageHandlingPreferences.isNormalizedForFocalLength()) {
				Float focalLength = imageSlice.getPictureData().getFocalLength();
				if (minFocalLength != null && focalLength != null) {
					zoomLevel = zoomLevel * minFocalLength / focalLength;
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
				lastActiveSlice = activeSlice;
				break;
			}
		}
	}

	@Override
	public void setNoActiveSlice() {
		if (activeSlice != null) {
			activeSlice.setSelected(false);
		}
		activeSlice = null;
	}

	@Override
	public void startAnnotating() {
		if (activeSlice != null) {
			activeSlice.startDrawing();
		}
	}

	@Override
	public void addPoint(int x, int y) {
		if (activeSlice != null) {
			activeSlice.addVertex(x, y);
		}
	}

	@Override
	public void stopAnnotating() {
		if (activeSlice != null) {
			activeSlice.stopDrawing();
		}
	}

	@Override
	public void undoLastAnnotation() {
		if (lastActiveSlice != null) {
			lastActiveSlice.undoLastDrawing();
			requestRepaint();
		}
	}

	@Override
	public void clearAnnotations() {
		if (lastActiveSlice != null) {
			lastActiveSlice.clearDrawings();
			requestRepaint();
		}
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
	public void toggleAutorotation() {
		boolean autorotation = imageHandlingPreferences.isAutorotation();
		imageSlices.forEach(slice -> slice.adjustRotation(autorotation));
	}

	@Override
	public void paint(Graphics2D graphics) {
		imageSlices.forEach(slice -> slice.paint(graphics));
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

	private void requestRepaint() {
		propertyChangeSupport.firePropertyChange(CommunicationMessages.REQUEST_REPAINT, null, null);
	}
}
