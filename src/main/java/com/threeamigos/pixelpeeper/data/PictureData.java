package com.threeamigos.pixelpeeper.data;

import com.threeamigos.common.util.interfaces.PropertyChangeAware;
import com.threeamigos.pixelpeeper.implementations.helpers.ExifOrientationHelper;
import com.threeamigos.pixelpeeper.interfaces.filters.Filter;
import com.threeamigos.pixelpeeper.interfaces.filters.FilterFactory;
import com.threeamigos.pixelpeeper.interfaces.filters.FilterFlavor;
import com.threeamigos.pixelpeeper.interfaces.filters.ViewportOverlayPainter;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavors.FilterPreferences;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavors.ImageHandlingPreferences;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Collection;

import static com.threeamigos.pixelpeeper.data.ExifValue.*;

/**
 * The data model used to show and compare an image to another.
 * Each PictureData contains info about:
 * <ul>
 *     <li>Image orientation, orientation correction, dimensions</li>
 *     <li>All {@link ExifTag} associated with this image</li>
 *     <li>A reference to the original file</li>
 *     <li>An optional image containing the filtered image</li>
 *     <li>The focal length, 35mm equivalent, current zoom level</li>
 * </ul>
 *
 * @author Stefano Reksten
 */
public class PictureData implements PropertyChangeAware {

    private Runnable onFilterStarted;
    private Runnable onFilterCompleted;
    /**
     * The orientation of the image as stored on file, and as such, immutable.
     */
    private final int orientation;
    /**
     * Used to remember if we did an autorotation of the image or not.
     */
    private boolean orientationAdjusted = false;
    /**
     * All {@link ExifTag} associated with this image.
     */
    private final ExifMap exifMap;
    /**
     * Original file
     */
    private final File file;
    /**
     * Original file name
     */
    private final String filename;

    /**
     * Original data of the image
     */
    private int sourceWidth;
    private int sourceHeight;
    private BufferedImage sourceImage;

    /**
     * Used to check if we should show image edges or not.
     */
    private final FilterPreferences filterPreferences;
    /**
     * In case we should apply a filter, we need a FilterFactory to get a Filter.
     */
    private final FilterFactory filterFactory;
    /**
     * The Filter class used to apply some effects to the image.
     */
    private Filter filter;
    /**
     * The algorithm used to filter the image.
     */
    private FilterFlavor flavor;
    private boolean filterCalculationInProgress;
    private boolean filterCalculationAborted;
    /**
     * An image that holds the filter results ONLY. If showing results, they are drawn over the original image,
     * eventually using a transparency.
     */
    private BufferedImage filteredImage;
    /**
     * True once a filter calculation has completed (even if its result image is null, as for viewport-overlay
     * filters). Guards against restarting the calculation every paint frame when filteredImage stays null.
     */
    private volatile boolean filterCalculationCompleted;
    /**
     * Non-null when the completed filter implements {@link ViewportOverlayPainter}.
     * Written by the background thread before the completion callback fires, establishing happens-before.
     */
    private volatile ViewportOverlayPainter viewportOverlayPainter;
    /**
     * The flavor that was active when viewportOverlayPainter was set. Used by getViewportOverlayPainter()
     * to return null when the flavor has changed, without needing to null the painter eagerly
     * (which would cause a visible flicker during recalculation).
     */
    private volatile FilterFlavor viewportOverlayPainterFlavor;

    /**
     * Current image (we can rotate or zoom the image so these may differ from the original image data).
     */
    private int width;
    private int height;
    private BufferedImage image;

    /**
     * When comparing two or more images that can be shot with different cameras with different sensors,
     * it can be interesting to compare them using full-frame as a common denominator. Thus we have to
     * remember the focal length used to shoot the image, its 35-mm equivalent and the crop factor of
     * the sensor use.
     */
    private Float focalLength;
    private Float focalLength35mmEquivalent;
    private Float cropFactor;

    /**
     * How much we're zooming out.
     */
    private float zoomLevel;

    public PictureData(int orientation, ExifMap exifMap, BufferedImage image, File file,
                       ImageHandlingPreferences imageHandlingPreferences,
                       FilterPreferences filterPreferences, FilterFactory filterFactory) {
        this.orientation = orientation;
        this.exifMap = exifMap;
        this.file = file;
        this.filename = file.getName();
        this.filterPreferences = filterPreferences;
        this.filterFactory = filterFactory;

        this.sourceWidth = image.getWidth();
        this.sourceHeight = image.getHeight();
        this.sourceImage = image;

        this.zoomLevel = ImageHandlingPreferences.FULL_ZOOM_LEVEL;

        if (imageHandlingPreferences.isAutorotation()) {
            correctOrientation();
        }

        calculateCropFactor();

        changeZoomLevel(imageHandlingPreferences.getZoomLevel());
    }

    private void calculateCropFactor() {
        focalLength = getTagValueAsFloat(ExifTag.FOCAL_LENGTH);
        focalLength35mmEquivalent = getTagValueAsFloat(ExifTag.FOCAL_LENGTH_35MM_EQUIVALENT);
        if (focalLength != null && focalLength35mmEquivalent != null) {
            cropFactor = focalLength35mmEquivalent / focalLength;
        } else {
            cropFactor = null;
        }
    }

    public int getOriginalWidth() {
        return sourceWidth;
    }

    public int getOriginalHeight() {
        return sourceHeight;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public Float getFocalLength() {
        return focalLength;
    }

    public Float getFocalLength35mmEquivalent() {
        return focalLength35mmEquivalent;
    }

    public Float getCropFactor() {
        return cropFactor;
    }

    public ExifMap getExifMap() {
        return exifMap;
    }

    public Collection<ExifTag> getTags() {
        return exifMap.getKeys();
    }

    public String getTagDescriptive(ExifTag exifTag) {
        return exifMap.getTagDescriptive(exifTag);
    }

    public Float getTagValueAsFloat(ExifTag exifTag) {
        return exifMap.getAsFloat(exifTag);
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

    /**
     * If the picture was shot with the camera that was tilted sideways, it may be possible
     * that we need to rotate the image in order to see the image correctly.
     */
    public void correctOrientation() {
        if (orientation != PICTURE_ORIENTATION_AS_IS && !orientationAdjusted) {
            sourceImage = ExifOrientationHelper.correctOrientation(sourceImage, orientation);
            swapDimensionsIfNeeded();
            changeZoomLevel(zoomLevel);
            orientationAdjusted = true;
        }
    }

    /**
     * If the picture was shot with the camera tilted sideways, we could want to see the image as it
     * was shot (and tilt our head!).
     */
    public void undoOrientationCorrection() {
        if (orientation != PICTURE_ORIENTATION_AS_IS && orientationAdjusted) {
            sourceImage = ExifOrientationHelper.undoOrientationCorrection(sourceImage, orientation);
            swapDimensionsIfNeeded();
            changeZoomLevel(zoomLevel);
            orientationAdjusted = false;

        }
    }

    private void swapDimensionsIfNeeded() {
        if (orientation > PICTURE_ORIENTATION_FLIP_VERTICALLY && orientation <= PICTURE_ORIENTATION_CLOCKWISE_90) {
            int tmp = sourceWidth;
            sourceWidth = sourceHeight;
            sourceHeight = tmp;
        }
    }

    /**
     * Returns an image containing the edges found in the original. The algorithm used
     * to calculate them is defined in the EdgesDetectorPreferences.
     */
    public BufferedImage getFilteredImage() {
        if (flavor != filterPreferences.getFilterFlavor()) {
            filteredImage = null;
            filterCalculationCompleted = false;
        }
        if (!filterCalculationCompleted) {
            startFilterCalculation();
        }
        return filteredImage;
    }

    public void setFilterCallbacks(Runnable onStarted, Runnable onCompleted) {
        this.onFilterStarted = onStarted;
        this.onFilterCompleted = onCompleted;
    }

    public void addPropertyChangeListener(PropertyChangeListener pcl) {
        // No-op: filter events are delivered via callbacks set in setFilterCallbacks()
    }

    public void removePropertyChangeListener(PropertyChangeListener pcl) {
        // No-op: filter events are delivered via callbacks set in setFilterCallbacks()
    }

    /**
     * Starts the filter algorithm in a background thread.
     * The operation may be slow, so if the user zooms in or does some other
     * operation that would make the edges invalid (not applicable anymore
     * for the zoom level, image rotation, and so on), the operation
     * may be interrupted.
     */
    public void startFilterCalculation() {
        synchronized (this) {
            if (!filterCalculationInProgress) {
                filterCalculationInProgress = true;
                filterCalculationAborted = false;
                if (onFilterStarted != null) onFilterStarted.run();
                Thread thread = new Thread(() -> {
                    filter = filterFactory.getFilter();
                    filter.setSourceImage(image);
                    flavor = filterPreferences.getFilterFlavor();
                    filter.process();
                    if (!filterCalculationAborted) {
                        filteredImage = filter.getResultingImage();
                        if (filter instanceof ViewportOverlayPainter) {
                            viewportOverlayPainter = (ViewportOverlayPainter) filter;
                            viewportOverlayPainterFlavor = flavor;
                        } else {
                            viewportOverlayPainter = null;
                            viewportOverlayPainterFlavor = null;
                        }
                        filterCalculationCompleted = true;
                        if (onFilterCompleted != null) onFilterCompleted.run();
                    }
                    filterCalculationInProgress = false;
                    filter = null;
                });
                thread.setDaemon(true);
                thread.start();
            }
        }
    }

    /**
     * If the filter calculation process produces a result that is
     * no more relevant for the current zoom level, image rotation, etc,
     * we can interrupt it.
     */
    /**
     * Returns the viewport overlay painter for the current filter flavor, or null if:
     * <ul>
     *   <li>the current filter does not implement {@link ViewportOverlayPainter}, or</li>
     *   <li>the flavor has changed since the painter was computed (avoids showing a stale
     *       histogram over a different filter while recalculation is in progress).</li>
     * </ul>
     * The painter is intentionally NOT nulled during recalculation so that the old histogram
     * remains visible until the new one is ready, eliminating flicker.
     */
    public ViewportOverlayPainter getViewportOverlayPainter() {
        if (viewportOverlayPainterFlavor != filterPreferences.getFilterFlavor()) {
            return null;
        }
        return viewportOverlayPainter;
    }

    public void releaseFilters() {
        filteredImage = null;
        filterCalculationCompleted = false;
        // viewportOverlayPainter is deliberately kept so the overlay does not flicker
        // while a recalculation is in progress for the same flavor.
        // getViewportOverlayPainter() hides it automatically when the flavor changes.
        flavor = null;
        if (filter != null) {
            filterCalculationAborted = true;
            filter.abort();
            while (filter != null) {
                try {
                    Thread.sleep(5);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    /**
     * Changes the zoom level to the percentage specified, from 10% to 800%.
     * For zoom < 100% the whole image is pre-scaled (it is smaller, safe to allocate).
     * For zoom > 100% the sourceImage is reused as backing store; the visible region
     * is extracted and scaled on the fly in paint(), avoiding huge memory allocations.
     */
    public void changeZoomLevel(float newZoomLevel) {
        zoomLevel = newZoomLevel;
        releaseFilters();
        if (zoomLevel == ImageHandlingPreferences.FULL_ZOOM_LEVEL) {
            width = sourceWidth;
            height = sourceHeight;
            image = sourceImage;
        } else if (zoomLevel < ImageHandlingPreferences.FULL_ZOOM_LEVEL) {
            // Shrink: pre-scale the whole image (smaller than source, fits in memory)
            width = (int) (sourceWidth * zoomLevel / ImageHandlingPreferences.FULL_ZOOM_LEVEL);
            height = (int) (sourceHeight * zoomLevel / ImageHandlingPreferences.FULL_ZOOM_LEVEL);
            image = new BufferedImage(width, height, sourceImage.getType());
            Graphics2D graphics = image.createGraphics();
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            graphics.drawImage(sourceImage, 0, 0, width - 1, height - 1, 0, 0, sourceWidth - 1, sourceHeight - 1, null);
            graphics.dispose();
        } else {
            // Magnify: store virtual dimensions for pan/boundary calculations but keep
            // sourceImage as the backing store — no costly full-resolution scaled copy.
            width = (int) (sourceWidth * zoomLevel / ImageHandlingPreferences.FULL_ZOOM_LEVEL);
            height = (int) (sourceHeight * zoomLevel / ImageHandlingPreferences.FULL_ZOOM_LEVEL);
            image = sourceImage;
        }
        if (filterPreferences.isShowResults()) {
            startFilterCalculation();
        }
    }

    /**
     * Returns the current zoom level for this image. Can't be greater than 100%.
     */
    public float getZoomLevel() {
        return zoomLevel;
    }
}
