package com.threeamigos.imageviewer.implementations.ui;

import java.awt.event.MouseEvent;

import com.threeamigos.imageviewer.interfaces.ui.MouseTracker;
import com.threeamigos.imageviewer.interfaces.ui.ScreenOffsetTracker;
import com.threeamigos.imageviewer.interfaces.ui.ImageSlice;

public class MouseTrackerImpl implements MouseTracker {

	private final ScreenOffsetTracker offsetTracker;

	private ImageSlice slice;
	private int pointerStartX;
	private int pointerStartY;

	public MouseTrackerImpl(ScreenOffsetTracker offsetTracker) {
		this.offsetTracker = offsetTracker;
	}

	@Override
	public void mousePressed(MouseEvent e, ImageSlice slice) {
		if (slice != null) {
			slice.setSelected(true);
			this.slice = slice;
			pointerStartX = e.getX();
			pointerStartY = e.getY();
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		if (slice != null) {
			slice.setSelected(false);
			slice = null;
		}
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		if (slice != null) {

			int endX = e.getX();
			int currentDeltaX = pointerStartX - endX;

			int pictureWidth = slice.getPictureData().getWidth();
			int pictureStartX = (int) (offsetTracker.getOffsetXPercentage() * pictureWidth) + currentDeltaX;
			if (pictureStartX < 0) {
				pictureStartX = 0;
			} else {
				int sliceWidth = (int) slice.getLocation().getWidth();
				if (pictureStartX >= pictureWidth - sliceWidth) {
					pictureStartX = pictureWidth - sliceWidth - 1;
				}
			}
			offsetTracker.setOffsetXPercentage((double) pictureStartX / (double) pictureWidth);

			int endY = e.getY();
			int currentDeltaY = pointerStartY - endY;

			int pictureHeight = slice.getPictureData().getHeight();
			int pictureStartY = (int) (offsetTracker.getOffsetYPercentage() * pictureHeight) + currentDeltaY;
			if (pictureStartY < 0) {
				pictureStartY = 0;
			} else {
				int sliceHeight = (int) slice.getLocation().getHeight();
				if (pictureStartY >= pictureHeight - sliceHeight) {
					pictureStartY = pictureHeight - sliceHeight - 1;
				}
			}
			offsetTracker.setOffsetYPercentage((double) pictureStartY / (double) pictureHeight);

			pointerStartX = endX;
			pointerStartY = endY;
		}
	}

}
