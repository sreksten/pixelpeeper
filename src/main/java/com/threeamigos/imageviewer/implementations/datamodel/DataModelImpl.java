package com.threeamigos.imageviewer.implementations.datamodel;

import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.threeamigos.common.util.interfaces.MessageHandler;
import com.threeamigos.imageviewer.data.ExifTag;
import com.threeamigos.imageviewer.data.ExifValue;
import com.threeamigos.imageviewer.data.PictureData;
import com.threeamigos.imageviewer.implementations.ui.InputAdapter;
import com.threeamigos.imageviewer.interfaces.datamodel.CommunicationMessages;
import com.threeamigos.imageviewer.interfaces.datamodel.DataModel;
import com.threeamigos.imageviewer.interfaces.datamodel.ExifCache;
import com.threeamigos.imageviewer.interfaces.datamodel.ExifImageReader;
import com.threeamigos.imageviewer.interfaces.datamodel.ImageSlice;
import com.threeamigos.imageviewer.interfaces.datamodel.ImageSlicesManager;
import com.threeamigos.imageviewer.interfaces.datamodel.TagsClassifier;
import com.threeamigos.imageviewer.interfaces.preferences.flavours.EdgesDetectorPreferences;
import com.threeamigos.imageviewer.interfaces.preferences.flavours.ImageHandlingPreferences;
import com.threeamigos.imageviewer.interfaces.preferences.flavours.PathPreferences;
import com.threeamigos.imageviewer.interfaces.ui.ExifTagsFilter;
import com.threeamigos.imageviewer.interfaces.ui.InputConsumer;
import com.threeamigos.imageviewer.interfaces.ui.KeyRegistry;

public class DataModelImpl implements DataModel {

	private final TagsClassifier tagsClassifier;
	private final ImageSlicesManager imageSlicesManager;
	private final ImageHandlingPreferences imageHandlingPreferences;
	private final PathPreferences pathPreferences;
	private final EdgesDetectorPreferences edgesDetectorPreferences;
	private final ExifImageReader imageReader;
	private final ExifTagsFilter exifTagsFilter;
	private final MessageHandler messageHandler;

	private final PropertyChangeSupport propertyChangeSupport;

	private boolean isMovementAppliedToAllImagesTemporarilyInverted;

	private GroupedFilesByExifTag groupedFiles;

	public DataModelImpl(TagsClassifier commonTagsHelper, ImageSlicesManager imageSlicesManager,
			ImageHandlingPreferences imageHandlingPreferences, PathPreferences pathPreferences,
			EdgesDetectorPreferences edgesDetectorPreferences, ExifCache exifCache, ExifImageReader imageReader,
			ExifTagsFilter exifTagsFilter, MessageHandler messageHandler) {
		this.tagsClassifier = commonTagsHelper;
		this.imageSlicesManager = imageSlicesManager;
		imageSlicesManager.addPropertyChangeListener(this);
		this.imageHandlingPreferences = imageHandlingPreferences;
		this.pathPreferences = pathPreferences;
		this.edgesDetectorPreferences = edgesDetectorPreferences;
		this.imageReader = imageReader;
		this.exifTagsFilter = exifTagsFilter;
		this.messageHandler = messageHandler;

		propertyChangeSupport = new PropertyChangeSupport(this);

		groupedFiles = new GroupedFilesByExifTag(exifCache);
	}

	@Override
	public void loadLastFiles() {
		if (!pathPreferences.getLastFilenames().isEmpty()) {
			loadFilesByName();
		}
	}

	private void loadFilesByName() {
		String path = pathPreferences.getLastPath() + File.separator;
		List<File> files = pathPreferences.getLastFilenames().stream().map(name -> new File(path + name))
				.collect(Collectors.toList());
		loadFiles(files, pathPreferences.getTagToGroupBy(), pathPreferences.getLastGroup());
	}

	@Override
	public void loadFiles(Collection<File> files) {
		loadFiles(files, null, 0);
	}

	@Override
	public void loadFiles(Collection<File> files, ExifTag tagToGroupBy, int groupIndex) {
		pathPreferences.setLastFilenames(files.stream().map(File::getName).collect(Collectors.toList()));
		pathPreferences.setTagToGroupBy(tagToGroupBy);
		groupedFiles.set(files, tagToGroupBy, groupIndex);
		loadFilesImpl();
	}

	public void browseDirectory(File directory, Component component) {
		if (directory != null) {
			if (directory.isDirectory()) {

				Collection<File> files = findImageFiles(directory);

				Collection<File> filesToLoad = exifTagsFilter.filterByTags(component, files);

				if (!filesToLoad.isEmpty()) {
					pathPreferences.setLastPath(directory.getPath());
					pathPreferences.setTagToGroupBy(exifTagsFilter.getTagToGroupBy());
					loadFiles(filesToLoad, exifTagsFilter.getTagToGroupBy(), 0);
				}
			} else {
				messageHandler.handleErrorMessage("Selected file is not a directory.");
			}
		}
	}

	private Collection<File> findImageFiles(File directory) {
		Collection<File> files = new ArrayList<>();
		for (File file : directory.listFiles()) {
			if (file.isFile()) {
				files.add(file);
			}
		}
		return files;
	}

	@Override
	public int getGroupsCount() {
		return groupedFiles.getGroupsCount();
	}

	@Override
	public int getCurrentGroup() {
		return groupedFiles.getCurrentGroup();
	}

	@Override
	public Optional<ExifValue> getCurrentExifValue() {
		return groupedFiles.getCurrentExifValue();
	}

	@Override
	public void moveToNextGroup() {
		if (groupedFiles.getGroupsCount() > 1) {
			groupedFiles.next();
			pathPreferences.setGroupIndex(groupedFiles.getCurrentGroup());
			loadFilesImpl();
		}
	}

	@Override
	public void moveToPreviousGroup() {
		if (groupedFiles.getGroupsCount() > 1) {
			groupedFiles.previous();
			pathPreferences.setGroupIndex(groupedFiles.getCurrentGroup());
			loadFilesImpl();
		}
	}

	private void loadFilesImpl() {
		new Thread(new Runnable() {
			@Override
			public void run() {

				Collection<File> files = groupedFiles.getCurrentFiles();

				if (!files.isEmpty()) {
					imageSlicesManager.clear();
					Map<File, PictureData> loadedPictures = new HashMap<>();
					files.parallelStream().forEach(file -> {
						PictureData pictureData = imageReader.readImage(file);
						if (pictureData != null) {
							synchronized (loadedPictures) {
								loadedPictures.put(file, pictureData);
							}
						}
					});
					for (File file : files) {
						PictureData pictureData = loadedPictures.get(file);
						if (pictureData != null) {
							imageSlicesManager.createImageSlice(pictureData);
						}
					}
					imageSlicesManager.resetMovement();
					imageSlicesManager.changeZoomLevel();
					tagsClassifier.classifyTags(imageSlicesManager.getImageSlices().stream()
							.map(slice -> slice.getPictureData().getExifMap()).collect(Collectors.toList()));

					propertyChangeSupport.firePropertyChange(CommunicationMessages.DATA_MODEL_CHANGED, null, null);
				}
			}
		}).start();
	}

	@Override
	public void reframe(int width, int height) {
		imageSlicesManager.reframeImageSlices(width, height);
	}

	@Override
	public void repaint(Graphics2D graphics) {
		imageSlicesManager.getImageSlices().forEach(slice -> slice.paint(graphics));
	}

	@Override
	public boolean isAutorotation() {
		return imageHandlingPreferences.isAutorotation();
	}

	@Override
	public void toggleAutorotation() {
		boolean autorotation = imageHandlingPreferences.isAutorotation();
		for (ImageSlice slice : imageSlicesManager.getImageSlices()) {
			slice.adjustRotation(autorotation);
		}
		propertyChangeSupport.firePropertyChange(CommunicationMessages.REQUEST_REPAINT, null, null);
	}

	@Override
	public boolean hasLoadedImages() {
		return imageSlicesManager.hasLoadedImages();
	}

	@Override
	public void move(int deltaX, int deltaY) {
		boolean isMovementAppliedToAllImages = imageHandlingPreferences.isMovementAppliedToAllImages();
		if (isMovementAppliedToAllImagesTemporarilyInverted) {
			isMovementAppliedToAllImages = !isMovementAppliedToAllImages;
		}
		imageSlicesManager.move(deltaX, deltaY, isMovementAppliedToAllImages);
		propertyChangeSupport.firePropertyChange(CommunicationMessages.REQUEST_REPAINT, null, null);
	}

	@Override
	public void resetMovement() {
		imageSlicesManager.resetMovement();
	}

	@Override
	public void changeZoomLevel() {
		imageSlicesManager.changeZoomLevel();
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
		return imageHandlingPreferences.isMovementAppliedToAllImages();
	}

	@Override
	public void setMovementAppliedToAllImages(boolean movementAppliedToAllImages) {
		imageHandlingPreferences.setMovementAppliedToAllImages(movementAppliedToAllImages);
	}

	@Override
	public void toggleMovementAppliedToAllImages() {
		imageHandlingPreferences
				.setMovementAppliedToAllImages(!imageHandlingPreferences.isMovementAppliedToAllImages());
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
		if (CommunicationMessages.AUTOROTATION_CHANGED.equals(evt.getPropertyName())) {
			toggleAutorotation();
		} else if (CommunicationMessages.DISPOSITION_CHANGED.equals(evt.getPropertyName())) {
			propertyChangeSupport.firePropertyChange(CommunicationMessages.DATA_MODEL_CHANGED, null, null);
		} else if (CommunicationMessages.EDGES_VISIBILITY_CHANGED.equals(evt.getPropertyName())) {
			handleEdgesVisibilityChanged();
		} else if (CommunicationMessages.REQUEST_EDGES_CALCULATION.equals(evt.getPropertyName())) {
			calculateEdges();
		} else if (CommunicationMessages.EDGES_CALCULATION_STARTED.equals(evt.getPropertyName())) {
			handleEdgeCalculationStarted(evt);
		} else if (CommunicationMessages.EDGES_CALCULATION_COMPLETED.equals(evt.getPropertyName())) {
			handleEdgeCalculationCompleted(evt);
		} else if (CommunicationMessages.ZOOM_LEVEL_CHANGED.equals(evt.getPropertyName())
				|| CommunicationMessages.NORMALIZED_FOR_CROP_CHANGED.equals(evt.getPropertyName())
				|| CommunicationMessages.NORMALIZE_FOR_FOCAL_LENGTH_CHANGED.equals(evt.getPropertyName())) {
			changeZoomLevel();
		} else if (CommunicationMessages.MOUSE_PRESSED.equals(evt.getPropertyName())) {
			handleMousePressed(evt);
		} else if (CommunicationMessages.MOUSE_RELEASED.equals(evt.getPropertyName())) {
			handleMouseReleased(evt);
		} else if (CommunicationMessages.MOUSE_DRAGGED.equals(evt.getPropertyName())) {
			handleMouseDragged(evt);
		}
	}

	private void handleEdgesVisibilityChanged() {
		requestRepaint();
	}

	private void handleEdgeCalculationStarted(PropertyChangeEvent evt) {
		propertyChangeSupport.firePropertyChange(CommunicationMessages.EDGES_CALCULATION_STARTED, null, null);
	}

	private void handleEdgeCalculationCompleted(PropertyChangeEvent evt) {
		propertyChangeSupport.firePropertyChange(CommunicationMessages.EDGES_CALCULATION_COMPLETED, null, null);
	}

	private void handleMousePressed(PropertyChangeEvent evt) {
		if (hasLoadedImages()) {
			MouseEvent e = (MouseEvent) evt.getNewValue();
			setActiveSlice(e.getX(), e.getY());
			requestRepaint();
		}
	}

	private void handleMouseReleased(PropertyChangeEvent evt) {
		if (hasLoadedImages()) {
			resetActiveSlice();
			requestRepaint();
		}
	}

	private void handleMouseDragged(PropertyChangeEvent evt) {
		if (hasLoadedImages()) {
			MouseEvent oldEvent = (MouseEvent) evt.getOldValue();
			MouseEvent newEvent = (MouseEvent) evt.getNewValue();
			int deltaX = oldEvent.getX() - newEvent.getX();
			int deltaY = oldEvent.getY() - newEvent.getY();
			move(deltaX, deltaY);
			requestRepaint();
		}
	}

	@Override
	public InputConsumer getInputConsumer() {
		return new InputAdapter() {

			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyRegistry.MOVEMENT_APPLIED_TO_ALL_IMAGES_TEMPORARILY_INVERTED) {
					isMovementAppliedToAllImagesTemporarilyInverted = true;

				} else if (e.getKeyCode() == KeyRegistry.SHOW_POSITION_MINIATURE_KEY) {
					imageHandlingPreferences
							.setPositionMiniatureVisible(!imageHandlingPreferences.isPositionMiniatureVisible());
					propertyChangeSupport.firePropertyChange(
							CommunicationMessages.POSITION_MINIATURE_VISIBILITY_CHANGED, null, null);
				}
			}

			@Override
			public void keyReleased(KeyEvent e) {
				if (e.getKeyCode() == KeyRegistry.MOVEMENT_APPLIED_TO_ALL_IMAGES_TEMPORARILY_INVERTED) {
					isMovementAppliedToAllImagesTemporarilyInverted = false;
				}
			}
		};
	}

	@Override
	public Collection<String> getHints() {
		Collection<String> hints = new ArrayList<>();
		hints.add("You can press P to show the position of the visible part of the image.");
		hints.add("You can hold the CTRL button to momentarily invert the current behaviour when dragging an image.");
		return hints;
	}

}
