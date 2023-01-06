package com.threeamigos.imageviewer.implementations.datamodel;

import java.awt.Graphics2D;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.threeamigos.imageviewer.data.ExifAndImageReader;
import com.threeamigos.imageviewer.data.ExifTag;
import com.threeamigos.imageviewer.data.PictureData;
import com.threeamigos.imageviewer.implementations.ui.PersistablesHelperImpl;
import com.threeamigos.imageviewer.interfaces.datamodel.DataModel;
import com.threeamigos.imageviewer.interfaces.persister.Persistable;
import com.threeamigos.imageviewer.interfaces.ui.ExifTagPreferences;
import com.threeamigos.imageviewer.interfaces.ui.ImageSlice;
import com.threeamigos.imageviewer.interfaces.ui.ImageSlicesManager;
import com.threeamigos.imageviewer.interfaces.ui.PathPreferences;
import com.threeamigos.imageviewer.interfaces.ui.PersistablesHelper;
import com.threeamigos.imageviewer.interfaces.ui.ScreenOffsetTracker;
import com.threeamigos.imageviewer.interfaces.ui.WindowPreferences;

public class DataModelImpl implements DataModel {

	private final ScreenOffsetTracker screenOffsetTracker;
	private final ImageSlicesManager slicesManager;
	private final ExifTagPreferences tagPreferences;
	private final WindowPreferences windowPreferences;
	private final PathPreferences pathPreferences;
	private final PersistablesHelper persistablesHelper;

	private List<Persistable> persistables = new ArrayList<>();

	public DataModelImpl(ScreenOffsetTracker screenOffsetTracker, ImageSlicesManager slicesManager,
			ExifTagPreferences tagPreferences, WindowPreferences windowPreferences, PathPreferences pathPreferences) {
		this.screenOffsetTracker = screenOffsetTracker;
		this.slicesManager = slicesManager;
		this.persistablesHelper = new PersistablesHelperImpl();

		this.tagPreferences = tagPreferences;
		persistablesHelper.addPersistable(tagPreferences);

		this.windowPreferences = windowPreferences;
		persistablesHelper.addPersistable(windowPreferences);

		this.pathPreferences = pathPreferences;
		persistablesHelper.addPersistable(pathPreferences);
	}

	@Override
	public void loadFiles(List<File> files) {
		if (!files.isEmpty()) {
			slicesManager.clear();
			for (File file : files) {
				ExifAndImageReader reader = new ExifAndImageReader(windowPreferences);
				if (reader.readImage(file)) {
					PictureData imageData = reader.getPictureData();
					ImageSlice slice = slicesManager.createImageSlice(imageData);
					slicesManager.addImageSlice(slice);
				}
			}
			screenOffsetTracker.reset();
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
		persistablesHelper.persist();
	}

	@Override
	public boolean hasLoadedImages() {
		return slicesManager.hasLoadedImages();
	}

	@Override
	public ImageSlice findImageSlice(int mouseX, int mouseY) {
		return slicesManager.findImageSlice(mouseX, mouseY);
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

}
