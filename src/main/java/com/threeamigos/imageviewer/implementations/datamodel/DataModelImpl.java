package com.threeamigos.imageviewer.implementations.datamodel;

import java.awt.Graphics2D;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import com.threeamigos.imageviewer.data.ExifMap;
import com.threeamigos.imageviewer.data.ExifTag;
import com.threeamigos.imageviewer.data.PictureData;
import com.threeamigos.imageviewer.interfaces.datamodel.CannyEdgeDetectorFactory;
import com.threeamigos.imageviewer.interfaces.datamodel.CommonTagsHelper;
import com.threeamigos.imageviewer.interfaces.datamodel.DataModel;
import com.threeamigos.imageviewer.interfaces.datamodel.ExifImageReader;
import com.threeamigos.imageviewer.interfaces.datamodel.ImageSlice;
import com.threeamigos.imageviewer.interfaces.datamodel.ImageSlicesManager;
import com.threeamigos.imageviewer.interfaces.preferences.PathPreferences;
import com.threeamigos.imageviewer.interfaces.preferences.WindowPreferences;
import com.threeamigos.imageviewer.interfaces.ui.ExifTagsFilter;

public class DataModelImpl implements DataModel {

	private final ExifTagsFilter exifTagsFilter;
	private final CommonTagsHelper commonTagsHelper;
	private final ImageSlicesManager slicesManager;
	private final WindowPreferences windowPreferences;
	private final PathPreferences pathPreferences;
	private final CannyEdgeDetectorFactory cannyEdgeDetectorFactory;
	private final ExifImageReader imageReader;

	private boolean isMovementAppliedToAllImagesTemporarilyInverted;

	public DataModelImpl(ExifTagsFilter exifTagsFilter, CommonTagsHelper commonTagsHelper,
			ImageSlicesManager slicesManager, WindowPreferences windowPreferences, PathPreferences pathPreferences,
			CannyEdgeDetectorFactory cannyEdgeDetectorFactory, ExifImageReader imageReader) {
		this.exifTagsFilter = exifTagsFilter;
		this.commonTagsHelper = commonTagsHelper;
		this.slicesManager = slicesManager;
		this.windowPreferences = windowPreferences;
		this.pathPreferences = pathPreferences;
		this.cannyEdgeDetectorFactory = cannyEdgeDetectorFactory;
		this.imageReader = imageReader;

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
			files.parallelStream().forEach(file -> {
				PictureData pictureData = imageReader.readImage(file);
				if (pictureData != null) {
					slicesManager.createImageSlice(pictureData);
				}
			});
			slicesManager.resetMovement();
			commonTagsHelper.updateCommonTags(slicesManager.getImageSlices().stream()
					.map(slice -> slice.getPictureData().getExifMap()).collect(Collectors.toList()));
			pathPreferences.setLastFilenames(files.stream().map(File::getName).collect(Collectors.toList()));
		}
	}

	@Override
	public void browseDirectory(File directory) {
		if (directory.isDirectory()) {

			Map<File, ExifMap> fileToTags = new HashMap<>();

			for (File file : directory.listFiles()) {
				if (file.isFile()) {
					ExifMap exifMap = imageReader.readMetadata(file);

					if (exifMap != null) {
						fileToTags.put(file, exifMap);
					}
				}
			}

			for (Map.Entry<File, ExifMap> entry : fileToTags.entrySet()) {
				File file = entry.getKey();
				System.out.println("File: " + file.getName());
			}

			CommonTagsHelper localCommonTagsHelper = new CommonTagsHelperImpl();
			localCommonTagsHelper.updateCommonTags(fileToTags.values());

			for (ExifTag commonTag : localCommonTagsHelper.getCommonTags()) {
				System.out.println("Common tag: " + commonTag);
			}
			for (Map.Entry<ExifTag, Collection<String>> entry : localCommonTagsHelper.getUncommonTagsToValues()
					.entrySet()) {
				ExifTag uncommonTag = entry.getKey();
				System.out.println("Uncommon tag: " + uncommonTag + " = "
						+ entry.getValue().stream().collect(Collectors.joining(", ")));
			}

			List<ExifTag> tagsToCheck = new ArrayList<>();
			tagsToCheck.add(ExifTag.CAMERA_MODEL);
			tagsToCheck.add(ExifTag.LENS_MODEL);
			tagsToCheck.add(ExifTag.APERTURE);
			tagsToCheck.add(ExifTag.ISO);
			tagsToCheck.add(ExifTag.EXPOSURE_TIME);

			Map<ExifTag, Collection<String>> tagsToFilter = new EnumMap<>(ExifTag.class);

			for (ExifTag exifTag : tagsToCheck) {
				if (!localCommonTagsHelper.isCommonTag(exifTag)) {
					tagsToFilter.put(exifTag, localCommonTagsHelper.getUncommonTagsToValues().get(exifTag));
				}
			}

			Map<ExifTag, Collection<String>> selectionMap = Collections.emptyMap();

			if (!tagsToFilter.isEmpty()) {
				selectionMap = exifTagsFilter.filterTags(tagsToFilter);
				if (selectionMap == null) {
					return;
				}
			}

			List<File> filesToLoad = new ArrayList<>();

			for (Entry<File, ExifMap> entry : fileToTags.entrySet()) {
				File file = entry.getKey();
				ExifMap exifMap = entry.getValue();
				if (exifMapMatchesSelection(exifMap, selectionMap)) {
					filesToLoad.add(file);
				}
			}

			if (!filesToLoad.isEmpty()) {
				pathPreferences.setLastPath(directory.getPath());
				loadFiles(filesToLoad);
			}
		}
	}

	private boolean exifMapMatchesSelection(ExifMap exifMap, Map<ExifTag, Collection<String>> selectionMap) {
		for (Map.Entry<ExifTag, Collection<String>> selectionEntry : selectionMap.entrySet()) {
			ExifTag selectedTag = selectionEntry.getKey();
			Collection<String> selectedValues = selectionEntry.getValue();
			String value = exifMap.getTagDescriptive(selectedTag);
			if (!selectedValues.contains(value)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public void reframe(int width, int height) {
		slicesManager.reframeImageSlices(width, height);
	}

	@Override
	public void repaint(Graphics2D graphics) {
		slicesManager.getImageSlices().parallelStream().forEach(slice -> slice.paint(graphics));
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
	public boolean hasLoadedImages() {
		return slicesManager.hasLoadedImages();
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
	public boolean isShowEdgeImages() {
		return windowPreferences.isShowEdgeImages();
	}

	@Override
	public void toggleShowingEdgeImages() {
		windowPreferences.setShowEdgeImages(!windowPreferences.isShowEdgeImages());
	}

}
