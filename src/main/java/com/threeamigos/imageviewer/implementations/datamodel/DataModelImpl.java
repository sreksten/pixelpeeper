package com.threeamigos.imageviewer.implementations.datamodel;

import java.awt.Graphics2D;
import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

import com.threeamigos.imageviewer.data.ExifAndImageReader;
import com.threeamigos.imageviewer.data.ExifTag;
import com.threeamigos.imageviewer.data.PictureData;
import com.threeamigos.imageviewer.interfaces.datamodel.CommonTagsHelper;
import com.threeamigos.imageviewer.interfaces.datamodel.DataModel;
import com.threeamigos.imageviewer.interfaces.datamodel.ImageSlice;
import com.threeamigos.imageviewer.interfaces.datamodel.ImageSlicesManager;
import com.threeamigos.imageviewer.interfaces.preferences.ExifTagPreferences;
import com.threeamigos.imageviewer.interfaces.preferences.PathPreferences;
import com.threeamigos.imageviewer.interfaces.preferences.WindowPreferences;

public class DataModelImpl implements DataModel {

	private final CommonTagsHelper commonTagsHelper;
	private final ImageSlicesManager slicesManager;
	private final ExifTagPreferences tagPreferences;
	private final WindowPreferences windowPreferences;
	private final PathPreferences pathPreferences;

	private boolean isMovementAppliedToAllImagesTemporarilyInverted;

	public DataModelImpl(CommonTagsHelper commonTagsHelper, ImageSlicesManager slicesManager,
			ExifTagPreferences tagPreferences, WindowPreferences windowPreferences, PathPreferences pathPreferences) {
		this.commonTagsHelper = commonTagsHelper;
		this.slicesManager = slicesManager;
		this.tagPreferences = tagPreferences;
		this.windowPreferences = windowPreferences;
		this.pathPreferences = pathPreferences;

		List<String> lastFilenames = pathPreferences.getLastFilenames();
		if (!lastFilenames.isEmpty()) {
			loadFilenames(lastFilenames);
		}
	}

	private void loadFilenames(List<String> filenames) {
		String path = pathPreferences.getLastPath() + File.separator;
		loadFiles(filenames.stream().map(name -> new File(path + name)).collect(Collectors.toList()));
	}

	@Override
	public void loadFiles(List<File> files) {
		if (!files.isEmpty()) {
			slicesManager.clear();
			for (File file : files) {
				ExifAndImageReader reader = new ExifAndImageReader(windowPreferences);
				if (reader.readImage(file)) {
					PictureData pictureData = reader.getPictureData();
					slicesManager.createImageSlice(pictureData);
				}
			}
			slicesManager.resetMovement();
			commonTagsHelper.updateCommonTags(slicesManager.getImageSlices().stream().map(ImageSlice::getPictureData)
					.collect(Collectors.toList()));
			pathPreferences.setLastFilenames(files.stream().map(File::getName).collect(Collectors.toList()));
		}
	}

	@Override
	public void reframe(int width, int height) {
		slicesManager.reframeImageSlices(width, height);
	}

	@Override
	public void repaint(Graphics2D graphics) {
		for (ImageSlice slice : slicesManager.getImageSlices()) {
			slice.paint(graphics);
		}
	}

	@Override
	public boolean isAutorotation() {
		return windowPreferences.isAutorotation();
	}

	@Override
	public void toggleAutorotation() {
		boolean autorotation = !windowPreferences.isAutorotation();
		windowPreferences.setAutorotation(autorotation);
		for (ImageSlice slice : slicesManager.getImageSlices()) {
			slice.adjustRotation(autorotation);
		}
	}

	@Override
	public boolean isTagsVisible() {
		return windowPreferences.isTagsVisible();
	}

	@Override
	public void toggleTagsVisibility() {
		tagPreferences.setTagsVisible(!tagPreferences.isTagsVisible());
	}

	@Override
	public boolean isTagVisible(ExifTag exifTag) {
		return tagPreferences.isTagVisible(exifTag);
	}

	@Override
	public void toggleTagVisibility(ExifTag exifTag) {
		tagPreferences.toggle(exifTag);
	}

	@Override
	public int getPreferredWidth() {
		return windowPreferences.getWidth();
	}

	@Override
	public void setPreferredWidth(int width) {
		windowPreferences.setWidth(width);
	}

	@Override
	public int getPreferredHeight() {
		return windowPreferences.getHeight();
	}

	@Override
	public void setPreferredHeight(int height) {
		windowPreferences.setHeight(height);
	}

	@Override
	public void savePreferences() {
		windowPreferences.persist();
		pathPreferences.persist();
		tagPreferences.persist();
	}

	@Override
	public boolean hasLoadedImages() {
		return slicesManager.hasLoadedImages();
	}

	@Override
	public int getPreferredX() {
		return windowPreferences.getX();
	}

	@Override
	public void setPreferredX(int x) {
		windowPreferences.setX(x);
	}

	@Override
	public int getPreferredY() {
		return windowPreferences.getY();
	}

	@Override
	public void setPreferredY(int y) {
		windowPreferences.setY(y);
	}

	@Override
	public String getLastPath() {
		return pathPreferences.getLastPath();
	}

	@Override
	public void setLastPath(String lastPath) {
		pathPreferences.setLastPath(lastPath);
	}

	@Override
	public void move(int deltaX, int deltaY) {
		boolean isMovementAppliedToAllImages = windowPreferences.isMovementAppliedToAllImages();
		if (isMovementAppliedToAllImagesTemporarilyInverted) {
			isMovementAppliedToAllImages = !isMovementAppliedToAllImages;
		}
		slicesManager.move(deltaX, deltaY, isMovementAppliedToAllImages);
	}

	@Override
	public void resetMovement() {
		slicesManager.resetMovement();
	}

	@Override
	public void setActiveSlice(int x, int y) {
		slicesManager.setActiveSlice(x, y);
	}

	@Override
	public void resetActiveSlice() {
		slicesManager.resetActiveSlice();
	}

	@Override
	public boolean isMovementAppliedToAllImages() {
		return windowPreferences.isMovementAppliedToAllImages();
	}

	@Override
	public void setMovementAppliedToAllImages(boolean movementAppliedToAllImages) {
		windowPreferences.setMovementAppliedToAllImages(movementAppliedToAllImages);
	}

	@Override
	public void toggleMovementAppliedToAllImages() {
		windowPreferences.setMovementAppliedToAllImages(!windowPreferences.isMovementAppliedToAllImages());
	}

	@Override
	public boolean isMovementAppliedToAllImagesTemporarilyInverted() {
		return isMovementAppliedToAllImagesTemporarilyInverted;
	}

	@Override
	public void setMovementAppliedToAllImagesTemporarilyInverted(
			boolean isMovementAppliedToAllImagesTemporarilyInverted) {
		this.isMovementAppliedToAllImagesTemporarilyInverted = isMovementAppliedToAllImagesTemporarilyInverted;
	}

	@Override
	public boolean isTagsVisibleOnlyIfDifferent() {
		return windowPreferences.isTagsVisibleOnlyIfDifferent();
	}

	@Override
	public void toggleTagsVisibilityOnlyIfDifferent() {
		windowPreferences.setTagsVisibleOnlyIfDifferent(!windowPreferences.isTagsVisibleOnlyIfDifferent());
	}
}
