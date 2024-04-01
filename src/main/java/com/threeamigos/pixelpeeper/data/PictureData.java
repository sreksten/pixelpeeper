package com.threeamigos.pixelpeeper.data;

import com.threeamigos.common.util.interfaces.PropertyChangeAware;
import com.threeamigos.pixelpeeper.implementations.helpers.ExifOrientationHelper;
import com.threeamigos.pixelpeeper.interfaces.datamodel.CommunicationMessages;
import com.threeamigos.pixelpeeper.interfaces.edgedetect.EdgesDetector;
import com.threeamigos.pixelpeeper.interfaces.edgedetect.EdgesDetectorFactory;
import com.threeamigos.pixelpeeper.interfaces.edgedetect.EdgesDetectorFlavour;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavours.EdgesDetectorPreferences;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavours.ImageHandlingPreferences;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.util.Collection;

import static com.threeamigos.pixelpeeper.data.ExifValue.PICTURE_ORIENTATION_AS_IS;

/**
 * The data model used to show and compare images.
 *
 * @author Stefano Reksten
 */
public class PictureData implements PropertyChangeAware {

    /**
     * PropertyChangeSupport is used to alert of edge calculation start and completion,
     * in order to refresh UI components.
     */
    private final PropertyChangeSupport propertyChangeSupport;
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
    private final EdgesDetectorPreferences edgesDetectorPreferences;
    /**
     * In case we should show edges, we need an EdgesDetectorFactory in order to get an EdgesDetector.
     */
    private final EdgesDetectorFactory edgesDetectorFactory;
    /**
     * The EdgesDetector class used to find edges within the image.
     */
    private EdgesDetector detector;
    /**
     * The algorithm used to find edges.
     */
    private EdgesDetectorFlavour flavour;
    private boolean edgeCalculationInProgress;
    private boolean edgeCalculationAborted;
    /**
     * An image that holds the edges ONLY. If showing edges they are drawn over the original image, eventually
     * using a transparency.
     */
    private BufferedImage edgesImage;

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
                       EdgesDetectorPreferences edgesDetectorPreferences, EdgesDetectorFactory edgesDetectorFactory) {
        propertyChangeSupport = new PropertyChangeSupport(this);

        this.orientation = orientation;
        this.exifMap = exifMap;
        this.file = file;
        this.filename = file.getName();
        this.edgesDetectorPreferences = edgesDetectorPreferences;
        this.edgesDetectorFactory = edgesDetectorFactory;

        this.sourceWidth = image.getWidth();
        this.sourceHeight = image.getHeight();
        this.sourceImage = image;

        this.zoomLevel = ImageHandlingPreferences.MAX_ZOOM_LEVEL;

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
        if (orientation > 4 && orientation <= 8) {
            int tmp = sourceWidth;
            sourceWidth = sourceHeight;
            sourceHeight = tmp;
        }
    }

    /**
     * Returns an image containing the edges found in the original. The algorithm used
     * to calculate them is defined in the EdgesDetectorPreferences.
     */
    public BufferedImage getEdgesImage() {
        if (flavour != edgesDetectorPreferences.getEdgesDetectorFlavour()) {
            edgesImage = null;
        }
        if (edgesImage == null) {
            startEdgesCalculation();
        }
        return edgesImage;
    }

    public void addPropertyChangeListener(PropertyChangeListener pcl) {
        propertyChangeSupport.addPropertyChangeListener(pcl);
    }

    public void removePropertyChangeListener(PropertyChangeListener pcl) {
        propertyChangeSupport.removePropertyChangeListener(pcl);
    }

    /**
     * Starts the edges detection algorithm in a background thread.
     * The operation may be slow, so if the user zooms in or does some other
     * operation that would make the edges invalid (not applicable anymore
     * for the zoom level, image rotation, and so on) the operation
     * may be interrupted.
     */
    public void startEdgesCalculation() {
        synchronized (this) {
            if (!edgeCalculationInProgress) {
                edgeCalculationInProgress = true;
                edgeCalculationAborted = false;
                propertyChangeSupport.firePropertyChange(CommunicationMessages.EDGES_CALCULATION_STARTED, null, this);
                Thread thread = new Thread(new Runnable() {
                    public void run() {
                        detector = edgesDetectorFactory.getEdgesDetector();
                        detector.setSourceImage(image);
                        flavour = edgesDetectorPreferences.getEdgesDetectorFlavour();
                        detector.process();
                        if (!edgeCalculationAborted) {
                            edgesImage = detector.getEdgesImage();
                            propertyChangeSupport.firePropertyChange(CommunicationMessages.EDGES_CALCULATION_COMPLETED,
                                    null, this);
                        }
                        edgeCalculationInProgress = false;
                        detector = null;
                    }
                });
                thread.setDaemon(true);
                thread.start();
            }
        }
    }

    /**
     * If the edges calculation process would produce a result that is
     * no more relevant for the current zoom level, image rotation etc,
     * we can interrupt it.
     */
    public void releaseEdges() {
        edgesImage = null;
        flavour = null;
        if (detector != null) {
            edgeCalculationAborted = true;
            detector.abort();
            while (detector != null) {
                try {
                    Thread.sleep(5);
                } catch (InterruptedException e) {
                }
            }
        }
    }

    /**
     * Changes the zoom level to the percentage specified, up to 100%.
     */
    public void changeZoomLevel(float newZoomLevel) {
        zoomLevel = newZoomLevel;
        releaseEdges();
        if (zoomLevel == ImageHandlingPreferences.MAX_ZOOM_LEVEL) {
            width = sourceWidth;
            height = sourceHeight;
            image = sourceImage;
        } else {
            width = (int) (sourceWidth * zoomLevel / ImageHandlingPreferences.MAX_ZOOM_LEVEL);
            height = (int) (sourceHeight * zoomLevel / ImageHandlingPreferences.MAX_ZOOM_LEVEL);
            image = new BufferedImage(width, height, sourceImage.getType());
            Graphics2D graphics = image.createGraphics();
            graphics.drawImage(sourceImage, 0, 0, width - 1, height - 1, 0, 0, sourceWidth - 1, sourceHeight - 1, null);
            graphics.dispose();
        }
        if (edgesDetectorPreferences.isShowEdges()) {
            startEdgesCalculation();
        }
    }

    /**
     * Returns the current zoom level for this image. Can't be greater than 100%.
     */
    public float getZoomLevel() {
        return zoomLevel;
    }
}
