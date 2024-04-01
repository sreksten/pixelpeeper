package com.threeamigos.pixelpeeper.implementations.datamodel;

import com.threeamigos.common.util.implementations.ui.StringHint;
import com.threeamigos.common.util.interfaces.messagehandler.MessageHandler;
import com.threeamigos.common.util.interfaces.ui.Hint;
import com.threeamigos.common.util.interfaces.ui.InputConsumer;
import com.threeamigos.pixelpeeper.data.ExifTag;
import com.threeamigos.pixelpeeper.data.ExifValue;
import com.threeamigos.pixelpeeper.data.PictureData;
import com.threeamigos.pixelpeeper.implementations.ui.InputAdapter;
import com.threeamigos.pixelpeeper.interfaces.datamodel.*;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavours.EdgesDetectorPreferences;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavours.ImageHandlingPreferences;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavours.SessionPreferences;
import com.threeamigos.pixelpeeper.interfaces.ui.ExifTagsFilter;
import com.threeamigos.pixelpeeper.interfaces.ui.KeyRegistry;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

public class DataModelImpl implements DataModel {

    private final ExifTagsClassifier tagsClassifier;
    private final ImageSlices imageSlices;
    private final ImageHandlingPreferences imageHandlingPreferences;
    private final SessionPreferences sessionPreferences;
    private final EdgesDetectorPreferences edgesDetectorPreferences;
    private final ExifImageReader imageReader;
    private final ExifTagsFilter exifTagsFilter;
    private final MessageHandler messageHandler;

    private final PropertyChangeSupport propertyChangeSupport;

    private boolean isDrawing;
    private boolean isMovementAppliedToAllImagesTemporarilyInverted;

    private final GroupedFilesByExifTag groupedFiles;

    public DataModelImpl(ExifTagsClassifier commonTagsHelper, ImageSlices imageSlicesManager,
                         ImageHandlingPreferences imageHandlingPreferences, SessionPreferences sessionPreferences,
                         EdgesDetectorPreferences edgesDetectorPreferences, ExifCache exifCache, ExifImageReader imageReader,
                         ExifTagsFilter exifTagsFilter, MessageHandler messageHandler) {
        this.tagsClassifier = commonTagsHelper;
        this.imageSlices = imageSlicesManager;
        imageSlicesManager.addPropertyChangeListener(this);
        this.imageHandlingPreferences = imageHandlingPreferences;
        this.sessionPreferences = sessionPreferences;
        this.edgesDetectorPreferences = edgesDetectorPreferences;
        this.imageReader = imageReader;
        this.exifTagsFilter = exifTagsFilter;
        this.messageHandler = messageHandler;

        propertyChangeSupport = new PropertyChangeSupport(this);

        groupedFiles = new GroupedFilesByExifTag(exifCache);
    }

    @Override
    public void loadLastFiles() {
        if (!sessionPreferences.getLastFilenames().isEmpty()) {
            loadFilesByName();
        }
    }

    private void loadFilesByName() {
        List<File> files = sessionPreferences.getLastFilenames().stream().map(File::new)
                .collect(Collectors.toList());
        loadFiles(files, sessionPreferences.getTagToGroupBy(), sessionPreferences.getTolerance(),
                sessionPreferences.getTagToOrderBy(), sessionPreferences.getGroupIndex());
    }

    @Override
    public void loadFiles(Collection<File> files) {
        loadFiles(files, null, 0, null, 0);
    }

    @Override
    public void loadFiles(Collection<File> files, ExifTag tagToGroupBy, int tolerance, ExifTag tagToOrderBy,
                          int groupIndex) {
        sessionPreferences.setLastFilenames(files.stream().map(File::getAbsolutePath).collect(Collectors.toList()));
        sessionPreferences.setTagToGroupBy(tagToGroupBy);
        sessionPreferences.setTolerance(tolerance);
        groupedFiles.set(files, tagToGroupBy, tolerance, tagToOrderBy, groupIndex);
        loadFilesImpl();
    }

    public void browseDirectory(File directory, Component component) {
        if (directory != null) {
            if (directory.isDirectory()) {

                Collection<File> files = findImageFiles(directory);

                Collection<File> filesToLoad = exifTagsFilter.filterByTags(component, files);

                if (!filesToLoad.isEmpty()) {
                    sessionPreferences.setLastPath(directory.getPath());
                    sessionPreferences.setTagToGroupBy(exifTagsFilter.getTagToGroupBy());
                    sessionPreferences.setTolerance(exifTagsFilter.getTolerance());
                    sessionPreferences.setTagToOrderBy(exifTagsFilter.getTagToOrderBy());
                    loadFiles(filesToLoad, exifTagsFilter.getTagToGroupBy(), exifTagsFilter.getTolerance(),
                            exifTagsFilter.getTagToOrderBy(), 0);
                }
            } else {
                messageHandler.handleErrorMessage("Selected file is not a directory.");
            }
        }
    }

    private Collection<File> findImageFiles(File directory) {
        Collection<File> files = new ArrayList<>();
        for (File file : Objects.requireNonNull(directory.listFiles())) {
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
            sessionPreferences.setGroupIndex(groupedFiles.getCurrentGroup());
            loadFilesImpl();
        }
    }

    @Override
    public void moveToPreviousGroup() {
        if (groupedFiles.getGroupsCount() > 1) {
            groupedFiles.previous();
            sessionPreferences.setGroupIndex(groupedFiles.getCurrentGroup());
            loadFilesImpl();
        }
    }

    private void loadFilesImpl() {
        new Thread(() -> {

            Collection<File> files = groupedFiles.getCurrentFiles();

            if (!files.isEmpty()) {
                imageSlices.clear();
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
                        imageSlices.add(pictureData);
                    }
                }
                imageSlices.sort();
                imageSlices.resetMovement();
                imageSlices.updateZoomLevel();
                tagsClassifier.classifyTags(
                        loadedPictures.values().stream().map(PictureData::getExifMap).collect(Collectors.toList()));

                propertyChangeSupport.firePropertyChange(CommunicationMessages.DATA_MODEL_CHANGED, null, null);
            }
        }).start();
    }

    @Override
    public void reframe(int width, int height) {
        imageSlices.reframe(width, height);
    }

    @Override
    public void repaint(Graphics2D graphics) {
        imageSlices.paint(graphics);
    }

    @Override
    public boolean isAutorotation() {
        return imageHandlingPreferences.isAutorotation();
    }

    @Override
    public void toggleAutorotation() {
        imageSlices.toggleAutorotation();
        propertyChangeSupport.firePropertyChange(CommunicationMessages.REQUEST_REPAINT, null, null);
    }

    @Override
    public boolean hasLoadedImages() {
        return imageSlices.isNotEmpty();
    }

    @Override
    public void move(int deltaX, int deltaY) {
        if (isDrawing) {
            imageSlices.move(deltaX, deltaY, false);
        } else {
            boolean isMovementAppliedToAllImages = imageHandlingPreferences.isMovementAppliedToAllImages();
            if (isMovementAppliedToAllImagesTemporarilyInverted) {
                isMovementAppliedToAllImages = !isMovementAppliedToAllImages;
            }
            imageSlices.move(deltaX, deltaY, isMovementAppliedToAllImages);
        }
        propertyChangeSupport.firePropertyChange(CommunicationMessages.REQUEST_REPAINT, null, null);
    }

    @Override
    public void resetMovement() {
        imageSlices.resetMovement();
    }

    @Override
    public void changeZoomLevel() {
        imageSlices.updateZoomLevel();
    }

    @Override
    public void setActiveSlice(int x, int y) {
        imageSlices.setActiveSlice(x, y);
    }

    @Override
    public void resetActiveSlice() {
        imageSlices.setNoActiveSlice();
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
            imageSlices.releaseEdges();
        }
    }

    @Override
    public void calculateEdges() {
        imageSlices.calculateEdges();
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
            handleEdgeCalculationStarted();
        } else if (CommunicationMessages.EDGES_CALCULATION_COMPLETED.equals(evt.getPropertyName())) {
            handleEdgeCalculationCompleted();
        } else if (CommunicationMessages.ZOOM_LEVEL_CHANGED.equals(evt.getPropertyName())
                || CommunicationMessages.NORMALIZED_FOR_CROP_CHANGED.equals(evt.getPropertyName())
                || CommunicationMessages.NORMALIZE_FOR_FOCAL_LENGTH_CHANGED.equals(evt.getPropertyName())) {
            changeZoomLevel();
        } else if (CommunicationMessages.MOUSE_PRESSED.equals(evt.getPropertyName())) {
            handleMousePressed(evt);
        } else if (CommunicationMessages.MOUSE_RELEASED.equals(evt.getPropertyName())) {
            handleMouseReleased();
        } else if (CommunicationMessages.MOUSE_DRAGGED.equals(evt.getPropertyName())) {
            handleMouseDragged(evt);
        }
    }

    private void handleEdgesVisibilityChanged() {
        requestRepaint();
    }

    private void handleEdgeCalculationStarted() {
        propertyChangeSupport.firePropertyChange(CommunicationMessages.EDGES_CALCULATION_STARTED, null, null);
    }

    private void handleEdgeCalculationCompleted() {
        propertyChangeSupport.firePropertyChange(CommunicationMessages.EDGES_CALCULATION_COMPLETED, null, null);
    }

    private void handleMousePressed(PropertyChangeEvent evt) {
        if (hasLoadedImages()) {
            MouseEvent e = (MouseEvent) evt.getNewValue();
            setActiveSlice(e.getX(), e.getY());
            if (isDrawing) {
                imageSlices.startDoodling();
            }
            requestRepaint();
        }
    }

    private void handleMouseReleased() {
        if (hasLoadedImages()) {
            if (isDrawing) {
                imageSlices.stopDoodling();
            }
            resetActiveSlice();
            requestRepaint();
        }
    }

    private void handleMouseDragged(PropertyChangeEvent evt) {
        if (hasLoadedImages()) {
            if (isDrawing) {
                MouseEvent newEvent = (MouseEvent) evt.getNewValue();
                imageSlices.addVertex(newEvent.getX(), newEvent.getY());
            } else {
                MouseEvent oldEvent = (MouseEvent) evt.getOldValue();
                MouseEvent newEvent = (MouseEvent) evt.getNewValue();
                int deltaX = oldEvent.getX() - newEvent.getX();
                int deltaY = oldEvent.getY() - newEvent.getY();
                move(deltaX, deltaY);
            }
            requestRepaint();
        }
    }

    @Override
    public InputConsumer getInputConsumer() {
        return new InputAdapter() {

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyRegistry.ANNOTATE_KEY.getKeyCode()) {
                    isDrawing = true;
                } else if (e.getKeyCode() == KeyRegistry.UNDO_KEY.getKeyCode()) {
                    imageSlices.undoLastDoodle();
                } else if (e.getKeyCode() == KeyRegistry.DELETE_KEY.getKeyCode()) {
                    imageSlices.clearDoodles();
                } else if (e.getKeyCode() == KeyRegistry.MOVEMENT_APPLIED_TO_ALL_IMAGES_TEMPORARILY_INVERTED
                        .getKeyCode()) {
                    isMovementAppliedToAllImagesTemporarilyInverted = true;
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyRegistry.ANNOTATE_KEY.getKeyCode()) {
                    isDrawing = false;
                } else if (e.getKeyCode() == KeyRegistry.MOVEMENT_APPLIED_TO_ALL_IMAGES_TEMPORARILY_INVERTED
                        .getKeyCode()) {
                    isMovementAppliedToAllImagesTemporarilyInverted = false;
                }
            }
        };
    }

    @Override
    public Collection<Hint<String>> getHints() {
        Collection<Hint<String>> hints = new ArrayList<>();
        hints.add(new StringHint("You can press P to show the position of the visible part of the image."));
        hints.add(new StringHint(
                "You can hold the CTRL button to momentarily invert the current behaviour when dragging an image."));
        return hints;
    }

}
