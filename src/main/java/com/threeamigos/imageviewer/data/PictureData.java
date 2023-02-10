package com.threeamigos.imageviewer.data;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.util.Collection;

import com.threeamigos.imageviewer.implementations.helpers.ExifOrientationHelper;
import com.threeamigos.imageviewer.interfaces.datamodel.CommunicationMessages;
import com.threeamigos.imageviewer.interfaces.edgedetect.EdgesDetector;
import com.threeamigos.imageviewer.interfaces.edgedetect.EdgesDetectorFactory;
import com.threeamigos.imageviewer.interfaces.edgedetect.EdgesDetectorFlavour;
import com.threeamigos.imageviewer.interfaces.preferences.flavours.EdgesDetectorPreferences;
import com.threeamigos.imageviewer.interfaces.preferences.flavours.ImageHandlingPreferences;

public class PictureData {

	private final int orientation;
	private final ExifMap exifMap;
	private final File file;
	private final String filename;
	private final EdgesDetectorPreferences edgesDetectorPreferences;
	private final EdgesDetectorFactory edgesDetectorFactory;

	private final PropertyChangeSupport propertyChangeSupport;

	private boolean orientationAdjusted = false;

	private int sourceWidth;
	private int sourceHeight;
	private BufferedImage sourceImage;

	private boolean edgeCalculationInProgress;
	private boolean edgeCalculationAborted;
	private EdgesDetector detector;
	private EdgesDetectorFlavour flavour;
	private BufferedImage edgesImage;

	private int width;
	private int height;
	private BufferedImage image;

	private Float focalLength;
	private Float focalLength35mmEquivalent;
	private Float cropFactor;

	private float zoomLevel;

	public PictureData(int orientation, ExifMap exifMap, BufferedImage image, File file,
			ImageHandlingPreferences imageHandlingPreferences, EdgesDetectorPreferences edgesDetectorPreferences,
			EdgesDetectorFactory edgesDetectorFactory) {
		this.orientation = orientation;
		this.exifMap = exifMap;
		this.file = file;
		this.filename = file.getName();
		this.edgesDetectorPreferences = edgesDetectorPreferences;
		this.edgesDetectorFactory = edgesDetectorFactory;

		this.sourceWidth = image.getWidth();
		this.sourceHeight = image.getHeight();
		this.sourceImage = image;

		propertyChangeSupport = new PropertyChangeSupport(this);

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

	public boolean isTagPresent(ExifTag exifTag) {
		return exifMap.getKeys().contains(exifTag);
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

	public void correctOrientation() {
		if (orientation != ExifOrientationHelper.AS_IS) {
			if (!orientationAdjusted) {
				sourceImage = ExifOrientationHelper.correctOrientation(sourceImage, orientation);
				swapDimensionsIfNeeded();
				changeZoomLevel(zoomLevel);
				orientationAdjusted = true;
			}
		}
	}

	public void undoOrientationCorrection() {
		if (orientation != ExifOrientationHelper.AS_IS) {
			if (orientationAdjusted) {
				sourceImage = ExifOrientationHelper.undoOrientationCorrection(sourceImage, orientation);
				swapDimensionsIfNeeded();
				changeZoomLevel(zoomLevel);
				orientationAdjusted = false;
			}
		}
	}

	private void swapDimensionsIfNeeded() {
		if (orientation > 4 && orientation <= 8) {
			int tmp = sourceWidth;
			sourceWidth = sourceHeight;
			sourceHeight = tmp;
		}
	}

	public boolean isEdgesImagePresent() {
		return edgesImage != null;
	}

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

	public float getZoomLevel() {
		return zoomLevel;
	}
}
