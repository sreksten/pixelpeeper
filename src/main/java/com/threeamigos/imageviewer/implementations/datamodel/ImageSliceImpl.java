package com.threeamigos.imageviewer.implementations.datamodel;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import com.threeamigos.imageviewer.data.PictureData;
import com.threeamigos.imageviewer.interfaces.datamodel.ImageSlice;
import com.threeamigos.imageviewer.interfaces.preferences.ExifTagPreferences;
import com.threeamigos.imageviewer.interfaces.ui.FontService;

public class ImageSliceImpl implements ImageSlice {

	private final PictureData pictureData;
	private final ExifTagPreferences tagPreferences;
	private final FontService fontService;

	private Rectangle location;
	private int screenXOffset;
	private int screenYOffset;

	private boolean selected;

	public ImageSliceImpl(PictureData pictureData, ExifTagPreferences tagPreferences, FontService fontService) {
		this.pictureData = pictureData;
		this.tagPreferences = tagPreferences;
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
			screenYOffset = (pictureData.getHeight() - location.height) / 2;
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

		int x = location.x;
		int y = location.y;
		int width = location.width;
		int height = location.height;

		int pictureWidth = pictureData.getWidth();
		int pictureHeight = pictureData.getHeight();

		if (pictureWidth <= width && pictureHeight <= height) {

			if (pictureWidth < width || pictureHeight < height) {
				g2d.setColor(Color.GRAY);
				g2d.drawRect(x, y, width, height);
			}
			g2d.drawImage(pictureData.getImage(), x + (width - pictureWidth) / 2, y + (height - pictureHeight) / 2,
					null);

		} else {

			BufferedImage subImage = pictureData.getImage().getSubimage(screenXOffset, screenYOffset, width, height);
			g2d.drawImage(subImage, x, y, null);

		}

		if (selected) {
			g2d.setColor(Color.RED);
//			System.out.println("Slice " + imageData.getFilename() + " active: drawing rectangle "
//					+ new Rectangle(x, y, x + width - 1, y + height - 1));
			g2d.drawRect(x, y, x + width - 1, y + height - 1);
		}

		new TagsRenderHelper(g2d, x, y + height - 1, fontService, pictureData, tagPreferences).render();

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