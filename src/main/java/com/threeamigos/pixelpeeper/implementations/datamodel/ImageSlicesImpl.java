package com.threeamigos.pixelpeeper.implementations.datamodel;

import com.threeamigos.common.util.interfaces.ui.FontService;
import com.threeamigos.pixelpeeper.data.PictureData;
import com.threeamigos.pixelpeeper.interfaces.datamodel.CommunicationMessages;
import com.threeamigos.pixelpeeper.interfaces.datamodel.ExifTagsClassifier;
import com.threeamigos.pixelpeeper.interfaces.datamodel.ImageSlice;
import com.threeamigos.pixelpeeper.interfaces.datamodel.ImageSlices;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavours.DrawingPreferences;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavours.EdgesDetectorPreferences;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavours.ImageHandlingPreferences;
import com.threeamigos.pixelpeeper.interfaces.ui.InfoRendererFactory;

import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

public class ImageSlicesImpl implements ImageSlices, PropertyChangeListener {

    private final ExifTagsClassifier exifTagsClassifier;
    private final InfoRendererFactory infoRendererFactory;
    private final ImageHandlingPreferences imageHandlingPreferences;
    private final DrawingPreferences drawingPreferences;
    private final EdgesDetectorPreferences edgesDetectorPreferences;
    private final FontService fontService;

    private final PropertyChangeSupport propertyChangeSupport;

    private final List<ImageSlice> imageSlices = new ArrayList<>();
    private final List<ImageSlice> imageSlicesCalculatingEdges = new ArrayList<>();

    private ImageSlice activeSlice;
    private ImageSlice lastActiveSlice;

    public ImageSlicesImpl(ExifTagsClassifier exifTagsClassifier, InfoRendererFactory infoRendererFactory,
                           ImageHandlingPreferences imageHandlingPreferences, DrawingPreferences drawingPreferences,
                           EdgesDetectorPreferences edgesDetectorPreferences, FontService fontService) {
        this.exifTagsClassifier = exifTagsClassifier;
        this.infoRendererFactory = infoRendererFactory;
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
        ImageSlice imageSlice = new ImageSliceImpl(pictureData, exifTagsClassifier, infoRendererFactory,
                imageHandlingPreferences, drawingPreferences, edgesDetectorPreferences, fontService);
        imageSlice.addPropertyChangeListener(this);
        imageSlices.add(imageSlice);
    }

    @Override
    public void sort() {
        imageSlices.sort(Comparator.comparing(i -> i.getPictureData().getFilename()));
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
        activeSlice.move(deltaX, deltaY);
        if (movementAppliesToAllImages) {
            Collection<ImageSlice> remainingSlices = imageSlices.stream()
                    .filter(s -> !activeSlice.equals(s))
                    .collect(Collectors.toList());
            if (imageHandlingPreferences.isRelativeMovement()) {
                moveOtherSlicesRelative(remainingSlices, deltaX, deltaY);
            } else {
                for (ImageSlice imageSlice : remainingSlices) {
                    imageSlice.move(deltaX, deltaY);
                }
            }
        }
    }

    private void moveOtherSlicesRelative(Collection<ImageSlice> remainingSlices, int deltaX, int deltaY) {
        int notVisibleActiveSliceWidth = activeSlice.getPictureData().getWidth()
                - activeSlice.getLocation().width;
        int notVisibleActiveSliceHeight = activeSlice.getPictureData().getHeight()
                - activeSlice.getLocation().height;

        for (ImageSlice currentSlice : remainingSlices) {
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

    @Override
    public void resetMovement() {
        imageSlices.forEach(ImageSlice::resetMovement);
    }

    @Override
    public void updateZoomLevel() {

        Float minCropFactor = calculateMinCropFactor();
        Float minFocalLength = calculateMinFocalLength();

        for (ImageSlice imageSlice : imageSlices) {
            float zoomLevel = imageHandlingPreferences.getZoomLevel();
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

    private Float calculateMinCropFactor() {
        Float minCropFactor = null;
        if (imageHandlingPreferences.isNormalizedForCrop()) {
            for (ImageSlice imageSlice : imageSlices) {
                Float cropFactor = imageSlice.getPictureData().getCropFactor();
                if (minCropFactor == null || cropFactor != null && minCropFactor > cropFactor) {
                    minCropFactor = cropFactor;
                }
            }
        }
        return minCropFactor;
    }

    private Float calculateMinFocalLength() {
        Float minFocalLength = null;
        if (imageHandlingPreferences.isNormalizedForFocalLength()) {
            for (ImageSlice imageSlice : imageSlices) {
                Float focalLength = imageSlice.getPictureData().getFocalLength35mmEquivalent();
                if (minFocalLength == null || focalLength != null && minFocalLength > focalLength) {
                    minFocalLength = focalLength;
                }
            }
        }
        return minFocalLength;
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
        } else if (CommunicationMessages.TAG_VISIBILITY_CHANGED.equals(evt.getPropertyName()) ||
                CommunicationMessages.TAGS_VISIBILITY_CHANGED.equals(evt.getPropertyName()) ||
                CommunicationMessages.TAGS_VISIBILITY_OVERRIDE_CHANGED.equals(evt.getPropertyName()) ||
                CommunicationMessages.TAGS_RENDERING_CHANGED.equals(evt.getPropertyName())) {
            handleTagsPreferencesChanged(evt);
        }
    }

    private void handleEdgeCalculationCompleted(PropertyChangeEvent evt) {
        ImageSlice imageSlice = (ImageSlice) evt.getNewValue();
        imageSlicesCalculatingEdges.remove(imageSlice);
        propertyChangeSupport.firePropertyChange(CommunicationMessages.EDGES_CALCULATION_COMPLETED, null, null);
    }

    private void handleTagsPreferencesChanged(PropertyChangeEvent evt) {
        for (ImageSlice imageSlice : imageSlices) {
            imageSlice.propertyChange(evt);
        }
    }

    private void requestRepaint() {
        propertyChangeSupport.firePropertyChange(CommunicationMessages.REQUEST_REPAINT, null, null);
    }
}
