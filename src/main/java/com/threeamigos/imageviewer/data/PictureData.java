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
import com.threeamigos.imageviewer.interfaces.preferences.flavours.ImageHandlingPreferences;

public class PictureData {

	private final int orientation;
	private final ExifMap exifMap;
	private final File file;
	private final String filename;
	private final ImageHandlingPreferences imageHandlingPreferences;
	private final EdgesDetectorFactory edgesDetectorFactory;

	private final PropertyChangeSupport propertyChangeSupport;

	private boolean orientationAdjusted = false;

	private int sourceWidth;
	private int sourceHeight;
	private BufferedImage sourceImage;

	private boolean edgeCalculationInProgress;
	private boolean edgeCalculationAborted;
	private EdgesDetector detector;
	private BufferedImage edgesImage;

	private int width;
	private int height;
	private BufferedImage image;

	public PictureData(int width, int height, int orientation, ExifMap exifMap, BufferedImage image, File file,
			ImageHandlingPreferences imageHandlingPreferences, EdgesDetectorFactory edgesDetectorFactory) {
		this.orientation = orientation;
		this.exifMap = exifMap;
		this.file = file;
		this.filename = file.getName();
		this.imageHandlingPreferences = imageHandlingPreferences;
		this.edgesDetectorFactory = edgesDetectorFactory;

		this.sourceWidth = width;
		this.sourceHeight = height;
		this.sourceImage = image;

		correctForZoom();

		propertyChangeSupport = new PropertyChangeSupport(this);
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public ExifMap getExifMap() {
		return exifMap;
	}

	public Collection<ExifTag> getAllTags() {
		return exifMap.getAllTags();
	}

	public boolean isTagPresent(ExifTag exifTag) {
		return exifMap.getAllTags().contains(exifTag);
	}

	public String getTagDescriptive(ExifTag exifTag) {
		return exifMap.getTagDescriptive(exifTag);
	}

	public Object getTagObject(ExifTag exifTag) {
		return exifMap.getTagObject(exifTag);
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
			releaseEdges();
			if (!orientationAdjusted) {
				sourceImage = ExifOrientationHelper.correctOrientation(sourceImage, orientation);
				swapDimensionsIfNeeded();
				correctForZoom();
				orientationAdjusted = true;
			}
		}
	}

	public void undoOrientationCorrection() {
		if (orientation != ExifOrientationHelper.AS_IS) {
			releaseEdges();
			if (orientationAdjusted) {
				sourceImage = ExifOrientationHelper.undoOrientationCorrection(sourceImage, orientation);
				swapDimensionsIfNeeded();
				correctForZoom();
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

	public void correctForZoom() {
		boolean hadEdges = edgesImage != null;
		releaseEdges();
		int zoomLevel = imageHandlingPreferences.getZoomLevel();
		if (zoomLevel == 100) {
			width = sourceWidth;
			height = sourceHeight;
			image = sourceImage;
		} else {
			width = sourceWidth * zoomLevel / 100;
			height = sourceHeight * zoomLevel / 100;
			image = new BufferedImage(width, height, sourceImage.getType());
			Graphics2D graphics = image.createGraphics();
			graphics.drawImage(sourceImage, 0, 0, width - 1, height - 1, 0, 0, sourceWidth - 1, sourceHeight - 1, null);
			graphics.dispose();
		}
		if (hadEdges) {
			startEdgesCalculation();
		}
	}
}
