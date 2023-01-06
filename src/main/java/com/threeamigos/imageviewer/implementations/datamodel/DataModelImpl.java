package com.threeamigos.imageviewer.implementations.datamodel;

import java.awt.Graphics2D;
import java.io.File;
import java.lang.reflect.Field;
import java.util.List;

import javax.swing.JOptionPane;

import com.threeamigos.imageviewer.data.ExifAndImageReader;
import com.threeamigos.imageviewer.data.ExifTag;
import com.threeamigos.imageviewer.data.PictureData;
import com.threeamigos.imageviewer.interfaces.datamodel.DataModel;
import com.threeamigos.imageviewer.interfaces.persister.Persistable;
import com.threeamigos.imageviewer.interfaces.ui.ExifTagPreferences;
import com.threeamigos.imageviewer.interfaces.ui.ImageSlice;
import com.threeamigos.imageviewer.interfaces.ui.ImageSlicesManager;
import com.threeamigos.imageviewer.interfaces.ui.PathPreferences;
import com.threeamigos.imageviewer.interfaces.ui.WindowPreferences;

public class DataModelImpl implements DataModel {

	private final ImageSlicesManager slicesManager;
	private final ExifTagPreferences tagPreferences;
	private final WindowPreferences windowPreferences;
	private final PathPreferences pathPreferences;

	private boolean isMovementAppliedToAllImagesTemporarilyInverted;

	public DataModelImpl(ImageSlicesManager slicesManager, ExifTagPreferences tagPreferences,
			WindowPreferences windowPreferences, PathPreferences pathPreferences) {
		this.slicesManager = slicesManager;
		this.tagPreferences = tagPreferences;
		this.windowPreferences = windowPreferences;
		this.pathPreferences = pathPreferences;
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
		try {
			for (Field field : getClass().getFields()) {
				Object fieldValue = field.get(this);
				if (fieldValue instanceof Persistable) {
					((Persistable) fieldValue).persist();
				}
			}
		} catch (IllegalArgumentException | IllegalAccessException e) {
			JOptionPane.showMessageDialog(null, "Error while saving preferences: " + e.getMessage());
		}
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
}
