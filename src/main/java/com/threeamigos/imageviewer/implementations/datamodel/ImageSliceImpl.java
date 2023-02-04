package com.threeamigos.imageviewer.implementations.datamodel;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import com.threeamigos.common.util.ui.draganddrop.BorderedStringRenderer;
import com.threeamigos.imageviewer.data.PictureData;
import com.threeamigos.imageviewer.implementations.helpers.ImageDrawHelper;
import com.threeamigos.imageviewer.interfaces.datamodel.CommunicationMessages;
import com.threeamigos.imageviewer.interfaces.datamodel.ImageSlice;
import com.threeamigos.imageviewer.interfaces.datamodel.TagsClassifier;
import com.threeamigos.imageviewer.interfaces.preferences.flavours.EdgesDetectorPreferences;
import com.threeamigos.imageviewer.interfaces.preferences.flavours.ExifTagPreferences;
import com.threeamigos.imageviewer.interfaces.preferences.flavours.ImageHandlingPreferences;
import com.threeamigos.imageviewer.interfaces.ui.FontService;

public class ImageSliceImpl implements ImageSlice, PropertyChangeListener {

	private final PictureData pictureData;
	private final TagsClassifier commonTagsHelper;
	private final ExifTagPreferences tagPreferences;
	private final ImageHandlingPreferences imageHandlingPreferences;
	private final EdgesDetectorPreferences edgesDetectorPreferences;
	private final FontService fontService;

	private final PropertyChangeSupport propertyChangeSupport;

	private Rectangle location;
	private double screenXOffset;
	private double screenYOffset;

	private boolean selected;

	private boolean edgeCalculationInProgress;

	public ImageSliceImpl(PictureData pictureData, TagsClassifier commonTagsHelper, ExifTagPreferences tagPreferences,
			ImageHandlingPreferences imageHandlingPreferences, EdgesDetectorPreferences edgesDetectorPreferences,
			FontService fontService) {
		this.pictureData = pictureData;
		pictureData.addPropertyChangeListener(this);
		this.commonTagsHelper = commonTagsHelper;
		this.tagPreferences = tagPreferences;
		this.imageHandlingPreferences = imageHandlingPreferences;
		this.edgesDetectorPreferences = edgesDetectorPreferences;
		this.fontService = fontService;

		propertyChangeSupport = new PropertyChangeSupport(this);
	}

	@Override
	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	@Override
	public void setLocation(Rectangle location) {
		this.location = location;
		checkBoundaries();
	}

	@Override
	public void move(double deltaX, double deltaY) {
		float zoomLevel = pictureData.getZoomLevel();
		screenXOffset += deltaX * zoomLevel / 100.0d;
		screenYOffset += deltaY * zoomLevel / 100.0d;
		checkBoundaries();
	}

	private void checkBoundaries() {
		int pictureWidth = pictureData.getWidth();
		if (screenXOffset > pictureWidth - location.width) {
			screenXOffset = pictureWidth - location.width;
		} else if (screenXOffset < 0.0d) {
			screenXOffset = 0.0d;
		}

		int pictureHeight = pictureData.getHeight();
		if (screenYOffset > pictureHeight - location.height) {
			screenYOffset = pictureHeight - location.height;
		} else if (screenYOffset < 0.0d) {
			screenYOffset = 0.0d;
		}
	}

	@Override
	public void resetMovement() {
		if (location != null) {
			screenXOffset = (pictureData.getWidth() - location.width) / 2;
			if (screenXOffset < 0.0d) {
				screenXOffset = 0.0d;
			}
			screenYOffset = (pictureData.getHeight() - location.height) / 2;
			if (screenYOffset < 0.0d) {
				screenYOffset = 0.0d;
			}
		} else {
			screenXOffset = 0.0d;
			screenYOffset = 0.0d;
		}
	}

	@Override
	public void changeZoomLevel(float zoomLevel) {
		pictureData.changeZoomLevel(zoomLevel);
	}

	@Override
	public Rectangle getLocation() {
		return location;
	}

	@Override
	public boolean contains(int x, int y) {
		return location != null && location.x <= x && x < location.x + location.width && location.y <= y
				&& y < location.y + location.height;
	}

	@Override
	public PictureData getPictureData() {
		return pictureData;
	}

	@Override
	public void paint(Graphics2D g2d) {

		if (location == null) {
			return;
		}

		int locationX = location.x;
		int locationY = location.y;
		int locationWidth = location.width;
		int locationHeight = location.height;
		int pictureWidth = pictureData.getWidth();
		int pictureHeight = pictureData.getHeight();

		if (pictureWidth < locationWidth || pictureHeight < locationHeight) {
			g2d.setColor(Color.GRAY);
			g2d.drawRect(locationX, locationY, locationWidth - 1, locationHeight - 1);
		}

		int imageSliceWidth = locationWidth <= pictureWidth ? locationWidth : pictureWidth;
		int imageSliceHeight = locationHeight <= pictureHeight ? locationHeight : pictureHeight;
		int imageSliceStartX = (int) screenXOffset;
		if (imageSliceStartX < 0) {
			imageSliceStartX = 0;
		}
		if (imageSliceStartX + imageSliceWidth > pictureWidth) {
			imageSliceStartX = pictureWidth - imageSliceWidth;
		}
		int imageSliceStartY = (int) screenYOffset;
		if (imageSliceStartY < 0) {
			imageSliceStartY = 0;
		}
		if (imageSliceStartY + imageSliceHeight > pictureHeight) {
			imageSliceStartY = pictureHeight - imageSliceHeight;
		}

		BufferedImage subImage = pictureData.getImage().getSubimage(imageSliceStartX, imageSliceStartY, imageSliceWidth,
				imageSliceHeight);
		BufferedImage edgesImage = null;

		if (edgesDetectorPreferences.isShowEdges()) {
			edgesImage = pictureData.getEdgesImage();
			if (edgesImage != null) {
				edgesImage = edgesImage.getSubimage(imageSliceStartX, imageSliceStartY, imageSliceWidth,
						imageSliceHeight);
			}
		}

		Shape previousClip = g2d.getClip();

		g2d.setClip(locationX, locationY, locationWidth, locationHeight);

		int pictureX = locationX;
		if (locationWidth > pictureWidth) {
			pictureX += (locationWidth - pictureWidth) / 2;
		}
		int pictureY = locationY;
		if (locationHeight > pictureHeight) {
			pictureY += (locationHeight - pictureHeight) / 2;
		}

		if (!edgesDetectorPreferences.isShowEdges()
				|| edgesDetectorPreferences.getEdgesTransparency() == EdgesDetectorPreferences.TOTAL_EDGES_TRANSPARENCY
				|| edgesImage == null) {
			g2d.drawImage(subImage, pictureX, pictureY, null);
		} else if (edgesDetectorPreferences.getEdgesTransparency() == EdgesDetectorPreferences.NO_EDGES_TRANSPARENCY) {
			g2d.drawImage(edgesImage, pictureX, pictureY, null);
		} else {
			ImageDrawHelper.drawTransparentImageAtop(g2d, subImage, edgesImage, pictureX, pictureY,
					edgesDetectorPreferences.getEdgesTransparency());
		}

		if (selected) {
			g2d.setColor(Color.RED);
			g2d.drawRect(locationX, locationY, locationWidth - 1, locationHeight - 1);
		}

		drawMiniatureWithPosition(g2d);

		new TagsRenderHelper(g2d, locationX, locationY + locationHeight - 1, fontService, pictureData, tagPreferences,
				commonTagsHelper).render();

		if (edgeCalculationInProgress) {
			BorderedStringRenderer.drawString(g2d, "Edge calculation in progress", locationX + 10, locationY + 30,
					Color.BLACK, Color.WHITE);
		}

		g2d.setClip(previousClip);
	}

	private void drawMiniatureWithPosition(Graphics2D g2d) {
		int locationX = location.x;
		int locationWidth = location.width;
		int locationHeight = location.height;
		int pictureWidth = pictureData.getWidth();
		int pictureHeight = pictureData.getHeight();

		if (imageHandlingPreferences.isPositionMiniatureVisible()
				&& (pictureWidth > locationWidth || pictureHeight > locationHeight)) {

			int miniatureWidth = locationWidth / 5;
			int miniatureHeight = miniatureWidth * pictureHeight / pictureWidth;

			g2d.setColor(Color.RED);
			int miniatureX = locationX + locationWidth - 1 - miniatureWidth - locationWidth / 20;
			int miniatureY = locationHeight - 1 - miniatureHeight - locationWidth / 20;
			g2d.drawRect(miniatureX, miniatureY, miniatureWidth, miniatureHeight);

			g2d.setColor(Color.YELLOW);
			int visibleWidth = locationWidth * miniatureWidth / pictureWidth;
			if (visibleWidth > miniatureWidth) {
				visibleWidth = miniatureWidth;
			}
			int visibleHeight = locationHeight * miniatureHeight / pictureHeight;
			if (visibleHeight > miniatureHeight) {
				visibleHeight = miniatureHeight;
			}

			int screenXOffsetScaled = pictureWidth > locationWidth
					? (int) (screenXOffset * miniatureWidth / pictureWidth)
					: 0;
			int screenYOffsetScaled = pictureHeight > locationHeight
					? (int) (screenYOffset * miniatureHeight / pictureHeight)
					: 0;

			g2d.drawRect(miniatureX + screenXOffsetScaled, miniatureY + screenYOffsetScaled, visibleWidth,
					visibleHeight);
		}
	}

	@Override
	public void adjustRotation(boolean autorotation) {
		if (autorotation) {
			pictureData.correctOrientation();
		} else {
			pictureData.undoOrientationCorrection();
		}
	}

	@Override
	public void startEdgesCalculation() {
		pictureData.startEdgesCalculation();
	}

	@Override
	public void releaseEdges() {
		pictureData.releaseEdges();
		edgeCalculationInProgress = false;
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
			handleEdgeCalculationStarted(evt);
		} else if (CommunicationMessages.EDGES_CALCULATION_COMPLETED.equals(evt.getPropertyName())) {
			handleEdgeCalculationCompleted(evt);
		}
	}

	private void handleEdgeCalculationStarted(PropertyChangeEvent evt) {
		edgeCalculationInProgress = true;
		propertyChangeSupport.firePropertyChange(CommunicationMessages.EDGES_CALCULATION_STARTED, null, this);
	}

	private void handleEdgeCalculationCompleted(PropertyChangeEvent evt) {
		edgeCalculationInProgress = false;
		propertyChangeSupport.firePropertyChange(CommunicationMessages.EDGES_CALCULATION_COMPLETED, null, this);
	}

}
