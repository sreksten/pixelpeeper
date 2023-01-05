package com.threeamigos.imageviewer.implementations.ui;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import com.threeamigos.imageviewer.data.PictureData;
import com.threeamigos.imageviewer.interfaces.ui.ExifTagPreferences;
import com.threeamigos.imageviewer.interfaces.ui.FontService;
import com.threeamigos.imageviewer.interfaces.ui.ImageSlice;
import com.threeamigos.imageviewer.interfaces.ui.ScreenOffsetTracker;

public class SliceImpl implements ImageSlice {

	private final PictureData pictureData;
	private final ScreenOffsetTracker offsetTracker;
	private final ExifTagPreferences tagPreferences;
	private final FontService fontService;

	private Rectangle location;
	private boolean selected;

	public SliceImpl(PictureData pictureData, ScreenOffsetTracker offsetTracker, ExifTagPreferences tagPreferences,
			FontService fontService) {
		this.pictureData = pictureData;
		this.offsetTracker = offsetTracker;
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

			int startX = (int) (pictureWidth * offsetTracker.getOffsetXPercentage());
			if (startX + width >= pictureWidth) {
				startX = pictureWidth - width - 1;
			}
			int startY = (int) (pictureHeight * offsetTracker.getOffsetYPercentage());
			if (startY + height >= pictureHeight) {
				startY = pictureHeight - height - 1;
			}

			BufferedImage subImage = pictureData.getImage().getSubimage(startX, startY, width, height);
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
