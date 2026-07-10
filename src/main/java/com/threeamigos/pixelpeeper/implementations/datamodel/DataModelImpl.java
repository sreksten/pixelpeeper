package com.threeamigos.pixelpeeper.implementations.datamodel;

import com.threeamigos.common.util.implementations.ui.StringHint;
import com.threeamigos.common.util.interfaces.messagehandler.MessageHandler;
import com.threeamigos.common.util.interfaces.ui.Hint;
import com.threeamigos.common.util.interfaces.ui.InputConsumer;
import com.threeamigos.pixelpeeper.data.ExifTag;
import com.threeamigos.pixelpeeper.data.ExifValue;
import com.threeamigos.pixelpeeper.data.PictureData;
import com.threeamigos.pixelpeeper.implementations.eventbus.EventBus;
import com.threeamigos.pixelpeeper.implementations.eventbus.events.*;
import com.threeamigos.pixelpeeper.implementations.ui.InputAdapter;
import com.threeamigos.pixelpeeper.interfaces.datamodel.*;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavors.ImageHandlingPreferences;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavors.SessionPreferences;
import com.threeamigos.pixelpeeper.interfaces.ui.ExifTagsFilter;
import com.threeamigos.pixelpeeper.interfaces.ui.KeyRegistry;
import jakarta.annotation.Nonnull;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

public class DataModelImpl implements DataModel {

    private final MessageHandler messageHandler;
    private final SessionPreferences sessionPreferences;
    private final ImageSlices imageSlices;
    private final ImageHandlingPreferences imageHandlingPreferences;
    private final GroupedFilesByExifTag groupedFiles;
    private final ExifImageReader imageReader;
    private final ExifTagsClassifier tagsClassifier;
    private final ExifTagsFilter exifTagsFilter;
    private final DoodlesPersistenceService doodlesPersistenceService;

    private boolean isDrawing;
    private boolean isMovementAppliedToAllImagesTemporarilyInverted;

    DataModelImpl(DataModelBuilder builder) {
        messageHandler = builder.getMessageHandler();
        sessionPreferences = builder.getSessionPreferences();
        imageSlices = builder.getImageSlices();
        imageHandlingPreferences = builder.getImageHandlingPreferences();
        groupedFiles = new GroupedFilesByExifTag(builder.getExifCache());
        imageReader = builder.getExifImageReader();
        tagsClassifier = builder.getExifTagsClassifier();
        exifTagsFilter = builder.getExifTagsFilter();
        doodlesPersistenceService = builder.getDoodlesPersistenceService();

        EventBus eventBus = EventBus.get();
        eventBus.subscribe(AutorotationChangedEvent.class, e -> toggleAutorotation());
        eventBus.subscribe(DispositionChangedEvent.class, e -> eventBus.publish(new DataModelChangedEvent()));
        eventBus.subscribe(FilterVisibilityChangedEvent.class, e -> requestRepaint());
        eventBus.subscribe(RequestFilterCalculationEvent.class, e -> startFilterCalculation());
        eventBus.subscribe(ZoomLevelChangedEvent.class, e -> changeZoomLevel());
        eventBus.subscribe(NormalizedForCropChangedEvent.class, e -> changeZoomLevel());
        eventBus.subscribe(NormalizedForFocalLengthChangedEvent.class, e -> changeZoomLevel());
        eventBus.subscribe(MousePressedEvent.class, this::handleMousePressed);
        eventBus.subscribe(MouseReleasedEvent.class, e -> handleMouseReleased());
        eventBus.subscribe(MouseDraggedEvent.class, this::handleMouseDragged);
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
                messageHandler.error("Selected file is not a directory.");
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
                imageSlices.persistDoodles(doodlesPersistenceService);

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
                        imageSlices.loadDoodlesForLastSlice(doodlesPersistenceService);
                    }
                }
                imageSlices.sort();
                imageSlices.resetMovement();
                imageSlices.updateZoomLevel();
                tagsClassifier.classifyTags(
                        loadedPictures.values().stream().map(PictureData::getExifMap).collect(Collectors.toList()));

                EventBus.get().publish(new DataModelChangedEvent());
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
    public void toggleAutorotation() {
        imageSlices.toggleAutorotation();
        EventBus.get().publish(new RepaintRequestEvent());
    }

    @Override
    public boolean hasLoadedImages() {
        return imageSlices.isNotEmpty();
    }

    public Collection<PictureData> getLoadedImages() {
        return imageSlices.getLoadedImages();
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
        EventBus.get().publish(new RepaintRequestEvent());
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
    public void startFilterCalculation() {
        imageSlices.startFilterCalculation();
    }

    @Override
    public void requestRepaint() {
        EventBus.get().publish(new RepaintRequestEvent());
    }

    private void handleMousePressed(MousePressedEvent e) {
        if (hasLoadedImages()) {
            setActiveSlice(e.mouseEvent.getX(), e.mouseEvent.getY());
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

    private void handleMouseDragged(MouseDraggedEvent e) {
        if (hasLoadedImages()) {
            if (isDrawing) {
                imageSlices.addVertex(e.newEvent.getX(), e.newEvent.getY());
            } else {
                int deltaX = e.oldEvent.getX() - e.newEvent.getX();
                int deltaY = e.oldEvent.getY() - e.newEvent.getY();
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
                } else if (e.getKeyCode() == KeyRegistry.MOVEMENT_APPLIED_TO_ALL_IMAGES_TEMPORARILY_INVERTED.getKeyCode()) {
                    isMovementAppliedToAllImagesTemporarilyInverted = true;
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyRegistry.ANNOTATE_KEY.getKeyCode()) {
                    isDrawing = false;
                } else if (e.getKeyCode() == KeyRegistry.MOVEMENT_APPLIED_TO_ALL_IMAGES_TEMPORARILY_INVERTED.getKeyCode()) {
                    isMovementAppliedToAllImagesTemporarilyInverted = false;
                }
            }
        };
    }

    @Override
    public @Nonnull Collection<Hint<String>> getHints() {
        Collection<Hint<String>> hints = new ArrayList<>();
        hints.add(new StringHint("Keep shift pressed and drag to draw annotations on the images."));
        hints.add(new StringHint("Press U to undo the last annotation stroke."));
        hints.add(new StringHint("Press Delete to delete all annotations on the active image."));
        hints.add(new StringHint("Press I to toggle between moving all images together or moving them independently."));
        hints.add(new StringHint("Press Ctrl and drag to temporarily invert the 'move all vs. single image' behaviour."));
        hints.add(new StringHint("Press M to toggle between percentage-based and pixel-based movement."));
        return hints;
    }
}
