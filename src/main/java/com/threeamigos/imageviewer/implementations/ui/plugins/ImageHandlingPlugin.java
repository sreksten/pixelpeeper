package com.threeamigos.imageviewer.implementations.ui.plugins;

import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.util.EnumMap;
import java.util.Map;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import com.threeamigos.imageviewer.implementations.ui.InputAdapter;
import com.threeamigos.imageviewer.interfaces.datamodel.CommunicationMessages;
import com.threeamigos.imageviewer.interfaces.preferences.ExifReaderFlavour;
import com.threeamigos.imageviewer.interfaces.preferences.ImageReaderFlavour;
import com.threeamigos.imageviewer.interfaces.preferences.flavours.ImageHandlingPreferences;
import com.threeamigos.imageviewer.interfaces.preferences.flavours.ImageHandlingPreferences.Disposition;
import com.threeamigos.imageviewer.interfaces.ui.InputConsumer;
import com.threeamigos.imageviewer.interfaces.ui.KeyRegistry;

public class ImageHandlingPlugin extends AbstractMainWindowPlugin {

	private final ImageHandlingPreferences imageHandlingPreferences;

	private JCheckBoxMenuItem autorotationMenuItem;
	private JCheckBoxMenuItem relativeMovementMenuItem;
	private JCheckBoxMenuItem moveAllImagesMenuItem;
	private JCheckBoxMenuItem showPositionMiniatureMenuItem;
	private JCheckBoxMenuItem normalizeForCropFactorMenuItem;
	private JCheckBoxMenuItem normalizeForFocalLengthMenuItem;

	private Map<ImageReaderFlavour, JMenuItem> imageReadersByFlavour = new EnumMap<>(ImageReaderFlavour.class);
	private Map<ExifReaderFlavour, JMenuItem> exifReadersByFlavour = new EnumMap<>(ExifReaderFlavour.class);
	private Map<Disposition, JMenuItem> dispositions = new EnumMap<>(Disposition.class);

	public ImageHandlingPlugin(ImageHandlingPreferences imageHandlingPreferences) {
		super();
		this.imageHandlingPreferences = imageHandlingPreferences;
	}

	@Override
	public void createMenu() {

		JMenu imageHandlingMenu = mainWindow.getMenu("Image handling");

		JMenu imageReaderMenu = new JMenu("Image reader library");
		imageHandlingMenu.add(imageReaderMenu);
		for (ImageReaderFlavour flavour : ImageReaderFlavour.values()) {
			JMenuItem imageReaderItem = addCheckboxMenuItem(imageReaderMenu, flavour.getDescription(),
					KeyRegistry.NO_KEY, flavour == imageHandlingPreferences.getImageReaderFlavour(), event -> {
						imageHandlingPreferences.setImageReaderFlavour(flavour);
						updateImageReaderMenu(flavour);
					});
			imageReadersByFlavour.put(flavour, imageReaderItem);
		}
		JMenu exifReaderMenu = new JMenu("Exif reader library");
		imageHandlingMenu.add(exifReaderMenu);
		for (ExifReaderFlavour flavour : ExifReaderFlavour.values()) {
			JMenuItem exifReaderItem = addCheckboxMenuItem(exifReaderMenu, flavour.getDescription(), KeyRegistry.NO_KEY,
					flavour == imageHandlingPreferences.getExifReaderFlavour(), event -> {
						imageHandlingPreferences.setExifReaderFlavour(flavour);
						updateExifReaderMenu(flavour);
					});
			exifReadersByFlavour.put(flavour, exifReaderItem);
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

	private void updateImageReaderMenu(final ImageReaderFlavour flavour) {
		for (Map.Entry<ImageReaderFlavour, JMenuItem> entry : imageReadersByFlavour.entrySet()) {
			entry.getValue().setSelected(entry.getKey() == flavour);
		}
	}

	private void updateExifReaderMenu(final ExifReaderFlavour flavour) {
		for (Map.Entry<ExifReaderFlavour, JMenuItem> entry : exifReadersByFlavour.entrySet()) {
			entry.getValue().setSelected(entry.getKey() == flavour);
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
