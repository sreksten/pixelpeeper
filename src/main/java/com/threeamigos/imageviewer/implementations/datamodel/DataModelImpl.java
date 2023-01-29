package com.threeamigos.imageviewer.implementations.datamodel;

import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import com.threeamigos.imageviewer.data.PictureData;
import com.threeamigos.imageviewer.implementations.ui.InputAdapter;
import com.threeamigos.imageviewer.interfaces.datamodel.CommunicationMessages;
import com.threeamigos.imageviewer.interfaces.datamodel.DataModel;
import com.threeamigos.imageviewer.interfaces.datamodel.ExifImageReader;
import com.threeamigos.imageviewer.interfaces.datamodel.ImageSlice;
import com.threeamigos.imageviewer.interfaces.datamodel.ImageSlicesManager;
import com.threeamigos.imageviewer.interfaces.datamodel.TagsClassifier;
import com.threeamigos.imageviewer.interfaces.preferences.flavours.EdgesDetectorPreferences;
import com.threeamigos.imageviewer.interfaces.preferences.flavours.ImageHandlingPreferences;
import com.threeamigos.imageviewer.interfaces.preferences.flavours.PathPreferences;
import com.threeamigos.imageviewer.interfaces.ui.InputConsumer;

public class DataModelImpl implements DataModel {

	private final TagsClassifier tagsClassifier;
	private final ImageSlicesManager imageSlicesManager;
	private final ImageHandlingPreferences imageHandlingPreferences;
	private final PathPreferences pathPreferences;
	private final EdgesDetectorPreferences edgesDetectorPreferences;
	private final ExifImageReader imageReader;

	private final PropertyChangeSupport propertyChangeSupport;

	private boolean isMovementAppliedToAllImagesTemporarilyInverted;

	public DataModelImpl(TagsClassifier commonTagsHelper, ImageSlicesManager imageSlicesManager,
			ImageHandlingPreferences imageHandlingPreferences, PathPreferences pathPreferences,
			EdgesDetectorPreferences edgesDetectorPreferences, ExifImageReader imageReader) {
		this.tagsClassifier = commonTagsHelper;
		this.imageSlicesManager = imageSlicesManager;
		imageSlicesManager.addPropertyChangeListener(this);
		this.imageHandlingPreferences = imageHandlingPreferences;
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
	public void loadFiles(Collection<File> files) {
		if (!files.isEmpty()) {
			imageSlicesManager.clear();
			files.parallelStream().forEach(file -> {
				PictureData pictureData = imageReader.readImage(file);
				if (pictureData != null) {
					imageSlicesManager.createImageSlice(pictureData);
				}
			});
			imageSlicesManager.resetMovement();
			tagsClassifier.classifyTags(imageSlicesManager.getImageSlices().stream()
					.map(slice -> slice.getPictureData().getExifMap()).collect(Collectors.toList()));
			pathPreferences.setLastFilenames(files.stream().map(File::getName).collect(Collectors.toList()));
		}
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
		return imageHandlingPreferences.isAutorotation();
	}

	@Override
	public void toggleAutorotation() {
		boolean autorotation = !imageHandlingPreferences.isAutorotation();
		imageHandlingPreferences.setAutorotation(autorotation);
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
		boolean isMovementAppliedToAllImages = imageHandlingPreferences.isMovementAppliedToAllImages();
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
		if (CommunicationMessages.EDGES_CALCULATION_STARTED.equals(evt.getPropertyName())) {
			handleEdgeCalculationStarted(evt);
		} else if (CommunicationMessages.EDGES_CALCULATION_COMPLETED.equals(evt.getPropertyName())) {
			handleEdgeCalculationCompleted(evt);
		} else if (CommunicationMessages.ZOOM_LEVEL_CHANGED.equals(evt.getPropertyName())) {
			changeZoomLevel();
		}
	}

	private void handleEdgeCalculationStarted(PropertyChangeEvent evt) {
		propertyChangeSupport.firePropertyChange(CommunicationMessages.EDGES_CALCULATION_STARTED, null, null);
	}

	private void handleEdgeCalculationCompleted(PropertyChangeEvent evt) {
		propertyChangeSupport.firePropertyChange(CommunicationMessages.EDGES_CALCULATION_COMPLETED, null, null);
	}

	@Override
	public InputConsumer getInputConsumer() {
		return new InputAdapter() {

			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_CONTROL) {
					isMovementAppliedToAllImagesTemporarilyInverted = true;

				} else if (e.getKeyCode() == KeyEvent.VK_P) {
					imageHandlingPreferences
							.setPositionMiniatureVisible(!imageHandlingPreferences.isPositionMiniatureVisible());
					propertyChangeSupport.firePropertyChange(CommunicationMessages.MINIATURE_VISIBILITY_CHANGE, null,
							null);
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

	@Override
	public Collection<String> getHints() {
		Collection<String> hints = new ArrayList<>();
		hints.add("You can press P to show the position of the visible part of the image.");
		hints.add("You can hold the CTRL button to momentarily invert the current behaviour when dragging an image.");
		return hints;
	}

}
