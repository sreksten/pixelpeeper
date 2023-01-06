package com.threeamigos.imageviewer.implementations.ui;

import com.threeamigos.imageviewer.data.PictureData;
import com.threeamigos.imageviewer.interfaces.ui.FontService;
import com.threeamigos.imageviewer.interfaces.ui.ScreenOffsetTracker;
import com.threeamigos.imageviewer.interfaces.ui.ImageSlice;
import com.threeamigos.imageviewer.interfaces.ui.ImageSliceFactory;
import com.threeamigos.imageviewer.interfaces.ui.ExifTagPreferences;

public class ImageSliceFactoryImpl implements ImageSliceFactory {

	private final ScreenOffsetTracker offsetTracker;
	private final ExifTagPreferences tagPreferences;
	private final FontService fontService;

	public ImageSliceFactoryImpl(ScreenOffsetTracker offsetTracker, ExifTagPreferences tagPreferences, FontService fontService) {
		this.offsetTracker = offsetTracker;
		this.tagPreferences = tagPreferences;
		this.fontService = fontService;
	}

	@Override
	public ImageSlice createImageSlice(PictureData pictureData) {
		return new ImageSliceImpl(pictureData, offsetTracker, tagPreferences, fontService);
	}

}
