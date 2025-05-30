package com.threeamigos.pixelpeeper.implementations.ui.plugins;

import com.threeamigos.common.util.interfaces.ui.InputConsumer;
import com.threeamigos.pixelpeeper.implementations.ui.InputAdapter;
import com.threeamigos.pixelpeeper.interfaces.datamodel.CommunicationMessages;
import com.threeamigos.pixelpeeper.interfaces.preferences.ExifReaderFlavor;
import com.threeamigos.pixelpeeper.interfaces.preferences.ImageReaderFlavor;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavors.ImageHandlingPreferences;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavors.ImageHandlingPreferences.Disposition;
import com.threeamigos.pixelpeeper.interfaces.ui.KeyRegistry;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseWheelEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.EnumMap;
import java.util.Map;

public class ImageHandlingPlugin extends AbstractMainWindowPlugin implements PropertyChangeListener {

    private final ImageHandlingPreferences imageHandlingPreferences;

    private JCheckBoxMenuItem autorotationMenuItem;
    private JCheckBoxMenuItem relativeMovementMenuItem;
    private JCheckBoxMenuItem moveAllImagesMenuItem;
    private JCheckBoxMenuItem showPositionMiniatureMenuItem;
    private JCheckBoxMenuItem normalizeForCropFactorMenuItem;
    private JCheckBoxMenuItem normalizeForFocalLengthMenuItem;

    private final Map<ImageReaderFlavor, JMenuItem> imageReadersByFlavor = new EnumMap<>(ImageReaderFlavor.class);
    private final Map<ExifReaderFlavor, JMenuItem> exifReadersByFlavor = new EnumMap<>(ExifReaderFlavor.class);
    private final Map<Disposition, JMenuItem> dispositions = new EnumMap<>(Disposition.class);

    public ImageHandlingPlugin(ImageHandlingPreferences imageHandlingPreferences) {
        super();
        this.imageHandlingPreferences = imageHandlingPreferences;
    }

    @Override
    public void createMenu() {

        JMenu imageHandlingMenu = mainWindow.getMenu("Image handling");

        JMenu imageReaderMenu = new JMenu("Image reader library");
        imageHandlingMenu.add(imageReaderMenu);
        for (ImageReaderFlavor flavor : ImageReaderFlavor.values()) {
            JMenuItem imageReaderItem = addCheckboxMenuItem(imageReaderMenu, flavor.getDescription(),
                    KeyRegistry.NO_KEY, flavor == imageHandlingPreferences.getImageReaderFlavor(), event -> {
                        imageHandlingPreferences.setImageReaderFlavor(flavor);
                        updateImageReaderMenu(flavor);
                    });
            imageReadersByFlavor.put(flavor, imageReaderItem);
        }
        JMenu exifReaderMenu = new JMenu("Exif reader library");
        imageHandlingMenu.add(exifReaderMenu);
        for (ExifReaderFlavor flavor : ExifReaderFlavor.values()) {
            JMenuItem exifReaderItem = addCheckboxMenuItem(exifReaderMenu, flavor.getDescription(), KeyRegistry.NO_KEY,
                    flavor == imageHandlingPreferences.getExifReaderFlavor(), event -> {
                        imageHandlingPreferences.setExifReaderFlavor(flavor);
                        updateExifReaderMenu(flavor);
                    });
            exifReadersByFlavor.put(flavor, exifReaderItem);
        }
        autorotationMenuItem = addCheckboxMenuItem(imageHandlingMenu, "Auto rotation", KeyRegistry.AUTOROTATION_KEY,
                imageHandlingPreferences.isAutorotation(), event -> toggleAutorotation());

        JMenu dispositionMenu = new JMenu("Disposition");
        imageHandlingMenu.add(dispositionMenu);
        for (Disposition disposition : Disposition.values()) {
            JMenuItem dispositionMenuItem = addCheckboxMenuItem(dispositionMenu, disposition.getDescription(),
                    KeyRegistry.NO_KEY, disposition == imageHandlingPreferences.getDisposition(), event -> {
                        imageHandlingPreferences.setDisposition(disposition);
                        updateDispositionMenu(disposition);
                    });
            dispositions.put(disposition, dispositionMenuItem);

        }
        relativeMovementMenuItem = addCheckboxMenuItem(imageHandlingMenu, "Movement in percentage",
                KeyRegistry.MOVEMENT_IN_PERCENTAGE_KEY, imageHandlingPreferences.isRelativeMovement(),
                event -> toggleRelativeMovement());
        moveAllImagesMenuItem = addCheckboxMenuItem(imageHandlingMenu, "Move all images",
                KeyRegistry.MOVE_ALL_IMAGES_KEY, imageHandlingPreferences.isMovementAppliedToAllImages(),
                event -> toggleMovementAppliedToAllImages());
        showPositionMiniatureMenuItem = addCheckboxMenuItem(imageHandlingMenu, "Show position",
                KeyRegistry.SHOW_POSITION_MINIATURE_KEY, imageHandlingPreferences.isPositionMiniatureVisible(),
                event -> togglePositionMiniatureVisibility());
        normalizeForCropFactorMenuItem = addCheckboxMenuItem(imageHandlingMenu, "Normalize for crop factor",
                KeyRegistry.NORMALIZE_FOR_CROP_FACTOR_KEY, imageHandlingPreferences.isNormalizedForCrop(),
                event -> toggleCropFactorNormalization());
        normalizeForFocalLengthMenuItem = addCheckboxMenuItem(imageHandlingMenu, "Normalize for focal length",
                KeyRegistry.NORMALIZE_FOR_FOCAL_LENGTH_KEY, imageHandlingPreferences.isNormalizedForFocalLength(),
                event -> toggleFocalLengthNormalization());
    }

    private void updateImageReaderMenu(final ImageReaderFlavor flavor) {
        for (Map.Entry<ImageReaderFlavor, JMenuItem> entry : imageReadersByFlavor.entrySet()) {
            entry.getValue().setSelected(entry.getKey() == flavor);
        }
    }

    private void updateExifReaderMenu(final ExifReaderFlavor flavor) {
        for (Map.Entry<ExifReaderFlavor, JMenuItem> entry : exifReadersByFlavor.entrySet()) {
            entry.getValue().setSelected(entry.getKey() == flavor);
        }
    }

    private void toggleAutorotation() {
        imageHandlingPreferences.setAutorotation(!imageHandlingPreferences.isAutorotation());
    }

    private void toggleRelativeMovement() {
        imageHandlingPreferences.setRelativeMovement(!imageHandlingPreferences.isRelativeMovement());
    }

    private void toggleMovementAppliedToAllImages() {
        imageHandlingPreferences
                .setMovementAppliedToAllImages(!imageHandlingPreferences.isMovementAppliedToAllImages());
    }

    private void togglePositionMiniatureVisibility() {
        imageHandlingPreferences.setPositionMiniatureVisible(!imageHandlingPreferences.isPositionMiniatureVisible());
    }

    private void toggleCropFactorNormalization() {
        imageHandlingPreferences.setNormalizedForCrop(!imageHandlingPreferences.isNormalizedForCrop());
    }

    private void toggleFocalLengthNormalization() {
        imageHandlingPreferences.setNormalizedForFocalLength(!imageHandlingPreferences.isNormalizedForFocalLength());
    }

    private void zoomIn() {
        imageHandlingPreferences
                .setZoomLevel(imageHandlingPreferences.getZoomLevel() + (int) ImageHandlingPreferences.ZOOM_LEVEL_STEP);
    }

    private void zoomOut() {
        imageHandlingPreferences
                .setZoomLevel(imageHandlingPreferences.getZoomLevel() - (int) ImageHandlingPreferences.ZOOM_LEVEL_STEP);
    }

    private void updateDispositionMenu(final Disposition disposition) {
        for (Map.Entry<Disposition, JMenuItem> entry : dispositions.entrySet()) {
            entry.getValue().setSelected(entry.getKey() == disposition);
        }
    }

    public InputConsumer getInputConsumer() {

        return new InputAdapter() {

            @Override
            public void keyPressed(KeyEvent e) {
                int key = e.getKeyCode();
                if (key == KeyRegistry.AUTOROTATION_KEY.getKeyCode()) {
                    toggleAutorotation();
                } else if (key == KeyRegistry.MOVEMENT_IN_PERCENTAGE_KEY.getKeyCode()) {
                    toggleRelativeMovement();
                } else if (key == KeyRegistry.MOVE_ALL_IMAGES_KEY.getKeyCode()) {
                    toggleMovementAppliedToAllImages();
                } else if (key == KeyRegistry.SHOW_POSITION_MINIATURE_KEY.getKeyCode()) {
                    togglePositionMiniatureVisibility();
                } else if (key == KeyRegistry.NORMALIZE_FOR_CROP_FACTOR_KEY.getKeyCode()) {
                    toggleCropFactorNormalization();
                } else if (key == KeyRegistry.NORMALIZE_FOR_FOCAL_LENGTH_KEY.getKeyCode()) {
                    toggleFocalLengthNormalization();
                } else if (key == KeyRegistry.ENLARGE_KEY.getKeyCode()) {
                    zoomIn();
                } else if (key == KeyRegistry.REDUCE_KEY.getKeyCode()) {
                    zoomOut();
                }
            }

            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                if (e.getWheelRotation() < 0) {
                    zoomIn();
                } else {
                    zoomOut();
                }
            }
        };
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(CommunicationMessages.AUTOROTATION_CHANGED)) {
            autorotationMenuItem.setSelected(imageHandlingPreferences.isAutorotation());
        } else if (evt.getPropertyName().equals(CommunicationMessages.NORMALIZED_FOR_CROP_CHANGED)) {
            normalizeForCropFactorMenuItem.setSelected(imageHandlingPreferences.isNormalizedForCrop());
        } else if (evt.getPropertyName().equals(CommunicationMessages.NORMALIZE_FOR_FOCAL_LENGTH_CHANGED)) {
            normalizeForFocalLengthMenuItem.setSelected(imageHandlingPreferences.isNormalizedForFocalLength());
        } else if (evt.getPropertyName().equals(CommunicationMessages.RELATIVE_MOVEMENT_CHANGED)) {
            relativeMovementMenuItem.setSelected(imageHandlingPreferences.isRelativeMovement());
        } else if (evt.getPropertyName().equals(CommunicationMessages.MOVEMENT_APPLIED_TO_ALL_IMAGES_CHANGED)) {
            moveAllImagesMenuItem.setSelected(imageHandlingPreferences.isMovementAppliedToAllImages());
        } else if (evt.getPropertyName().equals(CommunicationMessages.POSITION_MINIATURE_VISIBILITY_CHANGED)) {
            showPositionMiniatureMenuItem.setSelected(imageHandlingPreferences.isPositionMiniatureVisible());
        }
    }
}
