package com.threeamigos.imageviewer.implementations.datamodel;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import com.threeamigos.imageviewer.data.PictureData;
import com.threeamigos.imageviewer.implementations.helpers.ImageDrawHelper;
import com.threeamigos.imageviewer.interfaces.datamodel.CommonTagsHelper;
import com.threeamigos.imageviewer.interfaces.datamodel.ImageSlice;
import com.threeamigos.imageviewer.interfaces.preferences.ExifTagPreferences;
import com.threeamigos.imageviewer.interfaces.preferences.WindowPreferences;
import com.threeamigos.imageviewer.interfaces.ui.FontService;

public class ImageSliceImpl implements ImageSlice {

	private final PictureData pictureData;
	private final CommonTagsHelper commonTagsHelper;
	private final ExifTagPreferences tagPreferences;
	private final WindowPreferences windowPreferences;
	private final FontService fontService;

	private Rectangle location;
	private int screenXOffset;
	private int screenYOffset;

	private boolean selected;

	public ImageSliceImpl(PictureData pictureData, CommonTagsHelper commonTagsHelper, ExifTagPreferences tagPreferences,
			WindowPreferences windowPreferences, FontService fontService) {
		this.pictureData = pictureData;
		this.commonTagsHelper = commonTagsHelper;
		this.tagPreferences = tagPreferences;
		this.windowPreferences = windowPreferences;
		this.fontService = fontService;
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
	public void move(int deltaX, int deltaY) {
		screenXOffset += deltaX;
		screenYOffset += deltaY;
		checkBoundaries();
	}

	private void checkBoundaries() {
		int pictureWidth = pictureData.getWidth();
		if (screenXOffset > pictureWidth - location.width) {
			screenXOffset = pictureWidth - location.width;
		} else if (screenXOffset < 0) {
			screenXOffset = 0;
		}

		int pictureHeight = pictureData.getHeight();
		if (screenYOffset > pictureHeight - location.height) {
			screenYOffset = pictureHeight - location.height;
		} else if (screenYOffset < 0) {
			screenYOffset = 0;
		}
	}

	@Override
	public void resetMovement() {
		if (location != null) {
			screenXOffset = (pictureData.getWidth() - location.width) / 2;
			if (screenXOffset < 0) {
				screenXOffset = 0;
			}
			screenYOffset = (pictureData.getHeight() - location.height) / 2;
			if (screenYOffset < 0) {
				screenYOffset = 0;
			}
		} else {
			screenXOffset = 0;
			screenYOffset = 0;
		}
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
		int imageSliceStartX = screenXOffset;
		if (imageSliceStartX < 0) {
			imageSliceStartX = 0;
		}
		if (imageSliceStartX + imageSliceWidth > pictureWidth) {
			imageSliceStartX = pictureWidth - imageSliceWidth;
		}
		int imageSliceStartY = screenYOffset;
		if (imageSliceStartY < 0) {
			imageSliceStartY = 0;
		}
		if (imageSliceStartY + imageSliceHeight > pictureHeight) {
			imageSliceStartY = pictureHeight - imageSliceHeight;
		}

		BufferedImage subImage = pictureData.getImage().getSubimage(imageSliceStartX, imageSliceStartY, imageSliceWidth,
				imageSliceHeight);
		BufferedImage edgeImage = windowPreferences.isShowEdgeImages()
				? pictureData.getEdgeImage().getSubimage(imageSliceStartX, imageSliceStartY, imageSliceWidth,
						imageSliceHeight)
				: null;

		synchronized (g2d) {

			g2d.setClip(locationX, locationY, locationWidth, locationHeight);

			ImageDrawHelper.drawTransparentImageAtop(g2d, subImage,
					windowPreferences.isShowEdgeImages() ? edgeImage : null, locationX, locationY,
					windowPreferences.getEdgeImagesTransparency());

			if (selected) {
				g2d.setColor(Color.RED);
				g2d.drawRect(locationX, locationY, locationWidth - 1, locationHeight - 1);
			}

			new TagsRenderHelper(g2d, locationX, locationY + locationHeight - 1, fontService, pictureData,
					tagPreferences, commonTagsHelper).render();
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

}
