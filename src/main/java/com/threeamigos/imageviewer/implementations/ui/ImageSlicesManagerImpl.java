package com.threeamigos.imageviewer.implementations.ui;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.threeamigos.imageviewer.data.PictureData;
import com.threeamigos.imageviewer.interfaces.ui.ImageSlice;
import com.threeamigos.imageviewer.interfaces.ui.ImageSliceFactory;
import com.threeamigos.imageviewer.interfaces.ui.ImageSlicesManager;

public class ImageSlicesManagerImpl implements ImageSlicesManager {

	private final ImageSliceFactory sliceFactory;

	private List<ImageSlice> slices = new ArrayList<>();

	public ImageSlicesManagerImpl(ImageSliceFactory sliceFactory) {
		this.sliceFactory = sliceFactory;
	}

	@Override
	public void clear() {
		slices.clear();
	}

	@Override
	public boolean isEmpty() {
		return slices.isEmpty();
	}

	@Override
	public void add(ImageSlice slice) {
		slices.add(slice);
	}

	@Override
	public Collection<ImageSlice> getImageSlices() {
		return Collections.unmodifiableCollection(slices);
	}

	@Override
	public ImageSlice findSlice(int x, int y) {
		for (ImageSlice slice : slices) {
			if (slice.contains(x, y)) {
				return slice;
			}
		}
		return null;
	}

	@Override
	public void reframeImageSlices(int panelWidth, int panelHeight) {
		if (!slices.isEmpty()) {
			int sliceWidth = panelWidth / slices.size();
			int sliceHeight = panelHeight;
			int currentScreenOffsetX = 0;
			for (ImageSlice slice : slices) {
				Rectangle sliceRectangle = new Rectangle(currentScreenOffsetX, 0, sliceWidth, sliceHeight);
				slice.setLocation(sliceRectangle);
				currentScreenOffsetX += sliceWidth;
			}
		}
	}

	@Override
	public ImageSlice createImageSlice(PictureData pictureData) {
		return sliceFactory.createImageSlice(pictureData);
	}

}
