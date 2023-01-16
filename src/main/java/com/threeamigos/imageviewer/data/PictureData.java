package com.threeamigos.imageviewer.data;

import java.awt.image.BufferedImage;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.util.Collection;

import com.threeamigos.imageviewer.implementations.helpers.ExifOrientationHelper;
import com.threeamigos.imageviewer.interfaces.datamodel.CommunicationMessages;
import com.threeamigos.imageviewer.interfaces.datamodel.EdgesDetector;
import com.threeamigos.imageviewer.interfaces.datamodel.EdgesDetectorFactory;

public class PictureData {

	private final int orientation;
	private final ExifMap exifMap;
	private final File file;
	private final String filename;
	private final EdgesDetectorFactory edgesDetectorFactory;

	private final PropertyChangeSupport propertyChangeSupport;

	private boolean orientationAdjusted = false;

	private int width;
	private int height;
	private BufferedImage image;
	private boolean edgeCalculationInProgress;
	private BufferedImage edgesImage;

	public PictureData(int width, int height, int orientation, ExifMap exifMap, BufferedImage image, File file,
			EdgesDetectorFactory edgesDetectorFactory) {
		this.width = width;
		this.height = height;
		this.orientation = orientation;
		this.exifMap = exifMap;
		this.image = image;
		this.file = file;
		this.filename = file.getName();
		this.edgesDetectorFactory = edgesDetectorFactory;

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
		if (!orientationAdjusted) {
			image = ExifOrientationHelper.correctOrientation(image, orientation);
			swapDimensionsIfNeeded();
			orientationAdjusted = true;
		}
	}

	public void undoOrientationCorrection() {
		if (orientationAdjusted) {
			image = ExifOrientationHelper.undoOrientationCorrection(image, orientation);
			swapDimensionsIfNeeded();
			orientationAdjusted = false;
		}
	}

	private void swapDimensionsIfNeeded() {
		if (orientation > 4 && orientation <= 8) {
			int tmp = width;
			width = height;
			height = tmp;
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
				propertyChangeSupport.firePropertyChange(CommunicationMessages.EDGES_CALCULATION_STARTED, null, this);
				Thread thread = new Thread(new Runnable() {
					public void run() {
						EdgesDetector detector = edgesDetectorFactory.getEdgesDetector();
						detector.setSourceImage(image);
						detector.process();
						edgesImage = detector.getEdgesImage();
						propertyChangeSupport.firePropertyChange(CommunicationMessages.EDGES_CALCULATION_COMPLETED,
								null, this);
						edgeCalculationInProgress = false;
					}
				});
				thread.setDaemon(true);
				thread.start();
			}
		}
	}

}
