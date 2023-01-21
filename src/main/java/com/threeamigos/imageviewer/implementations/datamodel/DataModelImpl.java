package com.threeamigos.imageviewer.implementations.datamodel;

import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
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
import com.threeamigos.imageviewer.implementations.ui.PrioritizedInputAdapter;
import com.threeamigos.imageviewer.interfaces.datamodel.CommonTagsHelper;
import com.threeamigos.imageviewer.interfaces.datamodel.CommunicationMessages;
import com.threeamigos.imageviewer.interfaces.datamodel.DataModel;
import com.threeamigos.imageviewer.interfaces.datamodel.ExifImageReader;
import com.threeamigos.imageviewer.interfaces.datamodel.ImageSlice;
import com.threeamigos.imageviewer.interfaces.datamodel.ImageSlicesManager;
import com.threeamigos.imageviewer.interfaces.preferences.flavours.EdgesDetectorPreferences;
import com.threeamigos.imageviewer.interfaces.preferences.flavours.PathPreferences;
import com.threeamigos.imageviewer.interfaces.preferences.flavours.WindowPreferences;
import com.threeamigos.imageviewer.interfaces.ui.ExifTagsFilter;
import com.threeamigos.imageviewer.interfaces.ui.PrioritizedInputConsumer;

public class DataModelImpl implements DataModel {

	private final ExifTagsFilter exifTagsFilter;
	private final CommonTagsHelper commonTagsHelper;
	private final ImageSlicesManager imageSlicesManager;
	private final WindowPreferences windowPreferences;
	private final PathPreferences pathPreferences;
	private final EdgesDetectorPreferences edgesDetectorPreferences;
	private final ExifImageReader imageReader;

	private final PropertyChangeSupport propertyChangeSupport;

	private boolean isMovementAppliedToAllImagesTemporarilyInverted;

	public DataModelImpl(ExifTagsFilter exifTagsFilter, CommonTagsHelper commonTagsHelper,
			ImageSlicesManager imageSlicesManager, WindowPreferences windowPreferences, PathPreferences pathPreferences,
			EdgesDetectorPreferences edgesDetectorPreferences, ExifImageReader imageReader) {
		this.exifTagsFilter = exifTagsFilter;
		this.commonTagsHelper = commonTagsHelper;
		this.imageSlicesManager = imageSlicesManager;
		imageSlicesManager.addPropertyChangeListener(this);
		this.windowPreferences = windowPreferences;
		this.pathPreferences = pathPreferences;
		this.edgesDetectorPreferences = edgesDetectorPreferences;
		this.imageReader = imageReader;

		propertyChangeSupport = new PropertyChangeSupport(this);

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
			imageSlicesManager.clear();
			files.parallelStream().forEach(file -> {
				PictureData pictureData = imageReader.readImage(file);
				if (pictureData != null) {
					imageSlicesManager.createImageSlice(pictureData);
				}
			});
			imageSlicesManager.resetMovement();
			commonTagsHelper.updateCommonTags(imageSlicesManager.getImageSlices().stream()
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
		imageSlicesManager.reframeImageSlices(width, height);
	}

	@Override
	public void repaint(Graphics2D graphics) {
		imageSlicesManager.getImageSlices().stream().forEach(slice -> slice.paint(graphics));
	}

	@Override
	public boolean isAutorotation() {
		return windowPreferences.isAutorotation();
	}

	@Override
	public void toggleAutorotation() {
		boolean autorotation = !windowPreferences.isAutorotation();
		windowPreferences.setAutorotation(autorotation);
		for (ImageSlice slice : imageSlicesManager.getImageSlices()) {
			slice.adjustRotation(autorotation);
		}
	}

	@Override
	public boolean hasLoadedImages() {
		return imageSlicesManager.hasLoadedImages();
	}

	@Override
	public void move(int deltaX, int deltaY) {
		boolean isMovementAppliedToAllImages = windowPreferences.isMovementAppliedToAllImages();
		if (isMovementAppliedToAllImagesTemporarilyInverted) {
			isMovementAppliedToAllImages = !isMovementAppliedToAllImages;
		}
		imageSlicesManager.move(deltaX, deltaY, isMovementAppliedToAllImages);
	}

	@Override
	public void resetMovement() {
		imageSlicesManager.resetMovement();
	}

	@Override
	public void setActiveSlice(int x, int y) {
		imageSlicesManager.setActiveSlice(x, y);
	}

	@Override
	public void resetActiveSlice() {
		imageSlicesManager.resetActiveSlice();
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
	public boolean isShowEdges() {
		return edgesDetectorPreferences.isShowEdges();
	}

	@Override
	public void toggleShowingEdges() {
		boolean isShowEdges = !edgesDetectorPreferences.isShowEdges();
		edgesDetectorPreferences.setShowEdges(isShowEdges);
		if (!isShowEdges) {
			imageSlicesManager.releaseEdges();
		}
	}

	@Override
	public void calculateEdges() {
		imageSlicesManager.calculateEdges();
		propertyChangeSupport.firePropertyChange(CommunicationMessages.EDGES_CALCULATION_STARTED, null, null);
	}

	@Override
	public void requestRepaint() {
		propertyChangeSupport.firePropertyChange(CommunicationMessages.REQUEST_REPAINT, null, null);
	}

	@Override
	public void addPropertyChangeListener(PropertyChangeListener pcl) {
		propertyChangeSupport.addPropertyChangeListener(pcl);
	}

	@Override
	public void removePropertyChangeListener(PropertyChangeListener pcl) {
		propertyChangeSupport.removePropertyChangeListener(pcl);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (CommunicationMessages.EDGES_CALCULATION_STARTED.equals(evt.getPropertyName())) {
			handleEdgeCalculationStarted(evt);
		} else if (CommunicationMessages.EDGES_CALCULATION_COMPLETED.equals(evt.getPropertyName())) {
			handleEdgeCalculationCompleted(evt);
		}
	}

	private void handleEdgeCalculationStarted(PropertyChangeEvent evt) {
		propertyChangeSupport.firePropertyChange(CommunicationMessages.EDGES_CALCULATION_STARTED, null, null);
	}

	private void handleEdgeCalculationCompleted(PropertyChangeEvent evt) {
		propertyChangeSupport.firePropertyChange(CommunicationMessages.EDGES_CALCULATION_COMPLETED, null, null);
	}

	@Override
	public PrioritizedInputConsumer getPrioritizedInputConsumer() {
		return new PrioritizedInputAdapter(5) {

			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_CONTROL) {
					isMovementAppliedToAllImagesTemporarilyInverted = true;
				}
			}

			@Override
			public void keyReleased(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_CONTROL) {
					isMovementAppliedToAllImagesTemporarilyInverted = false;
				}
			}

		};
	}

}
