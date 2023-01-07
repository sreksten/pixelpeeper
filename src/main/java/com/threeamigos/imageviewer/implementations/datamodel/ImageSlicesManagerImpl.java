package com.threeamigos.imageviewer.implementations.datamodel;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.threeamigos.imageviewer.data.PictureData;
import com.threeamigos.imageviewer.interfaces.datamodel.CommonTagsHelper;
import com.threeamigos.imageviewer.interfaces.datamodel.ImageSlice;
import com.threeamigos.imageviewer.interfaces.datamodel.ImageSlicesManager;
import com.threeamigos.imageviewer.interfaces.preferences.ExifTagPreferences;
import com.threeamigos.imageviewer.interfaces.preferences.WindowPreferences;
import com.threeamigos.imageviewer.interfaces.ui.FontService;

public class ImageSlicesManagerImpl implements ImageSlicesManager {

	private final WindowPreferences windowPreferences;
	private final CommonTagsHelper commonTagsHelper;
	private final ExifTagPreferences tagPreferences;
	private final FontService fontService;

	private List<ImageSlice> imageSlices = new ArrayList<>();

	private ImageSlice activeSlice;

	public ImageSlicesManagerImpl(WindowPreferences windowPreferences, CommonTagsHelper commonTagsHelper,
			ExifTagPreferences tagPreferences, FontService fontService) {
		this.windowPreferences = windowPreferences;
		this.commonTagsHelper = commonTagsHelper;
		this.tagPreferences = tagPreferences;
		this.fontService = fontService;
	}

	@Override
	public void clear() {
		imageSlices.clear();
	}

	@Override
	public boolean hasLoadedImages() {
		return !imageSlices.isEmpty();
	}

	@Override
	public Collection<ImageSlice> getImageSlices() {
		return Collections.unmodifiableCollection(imageSlices);
	}

	@Override
	public void reframeImageSlices(int panelWidth, int panelHeight) {
		if (!imageSlices.isEmpty()) {
			int sliceWidth = panelWidth / imageSlices.size();
			int sliceHeight = panelHeight;
			int currentScreenOffsetX = 0;
			for (ImageSlice slice : imageSlices) {
				Rectangle sliceRectangle = new Rectangle(currentScreenOffsetX, 0, sliceWidth, sliceHeight);
				slice.setLocation(sliceRectangle);
				currentScreenOffsetX += sliceWidth;
			}
		}
	}

	@Override
	public ImageSlice createImageSlice(PictureData pictureData) {
		ImageSlice imageSlice = new ImageSliceImpl(pictureData, windowPreferences, commonTagsHelper, tagPreferences,
				fontService);
		imageSlices.add(imageSlice);
		return imageSlice;
	}

	@Override
	public void move(int deltaX, int deltaY, boolean movementAppliesToAllImages) {
		if (movementAppliesToAllImages) {
			for (ImageSlice imageSlice : imageSlices) {
				imageSlice.move(deltaX, deltaY);
			}
		} else {
			if (activeSlice != null) {
				activeSlice.move(deltaX, deltaY);
			}
		}
	}

	@Override
	public void resetMovement() {
		for (ImageSlice imageSlice : imageSlices) {
			imageSlice.resetMovement();
		}
	}

	@Override
	public void setActiveSlice(int x, int y) {
		for (ImageSlice currentSlice : imageSlices) {
			if (currentSlice.contains(x, y)) {
				activeSlice = currentSlice;
				activeSlice.setSelected(true);
				break;
			}
		}
	}

	@Override
	public void resetActiveSlice() {
		if (activeSlice != null) {
			activeSlice.setSelected(false);
		}
		activeSlice = null;
	}
}
