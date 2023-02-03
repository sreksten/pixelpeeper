package com.threeamigos.imageviewer.implementations.ui.plugins;

import java.beans.PropertyChangeEvent;
import java.util.EnumMap;
import java.util.Map;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import com.threeamigos.imageviewer.interfaces.datamodel.CommunicationMessages;
import com.threeamigos.imageviewer.interfaces.preferences.ExifReaderFlavour;
import com.threeamigos.imageviewer.interfaces.preferences.ImageReaderFlavour;
import com.threeamigos.imageviewer.interfaces.preferences.flavours.ImageHandlingPreferences;
import com.threeamigos.imageviewer.interfaces.preferences.flavours.ImageHandlingPreferences.Disposition;

public class ImageHandlingPlugin extends AbstractMainWindowPlugin {

	private final ImageHandlingPreferences imageHandlingPreferences;

	private JCheckBoxMenuItem autorotationMenuItem;

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
			JMenuItem imageReaderItem = addCheckboxMenuItem(imageReaderMenu, flavour.getDescription(), -1,
					flavour == imageHandlingPreferences.getImageReaderFlavour(), event -> {
						imageHandlingPreferences.setImageReaderFlavour(flavour);
						updateImageReaderMenu(flavour);
					});
			imageReadersByFlavour.put(flavour, imageReaderItem);
		}
		JMenu exifReaderMenu = new JMenu("Exif reader library");
		imageHandlingMenu.add(exifReaderMenu);
		for (ExifReaderFlavour flavour : ExifReaderFlavour.values()) {
			JMenuItem exifReaderItem = addCheckboxMenuItem(exifReaderMenu, flavour.getDescription(), -1,
					flavour == imageHandlingPreferences.getExifReaderFlavour(), event -> {
						imageHandlingPreferences.setExifReaderFlavour(flavour);
						updateExifReaderMenu(flavour);
					});
			exifReadersByFlavour.put(flavour, exifReaderItem);
		}
		autorotationMenuItem = addCheckboxMenuItem(imageHandlingMenu, "Auto rotation", AUTOROTATION_KEY,
				imageHandlingPreferences.isAutorotation(), event -> {
					imageHandlingPreferences.setAutorotation(!imageHandlingPreferences.isAutorotation());
				});
		JMenu dispositionMenu = new JMenu("Disposition");
		imageHandlingMenu.add(dispositionMenu);
		for (Disposition disposition : Disposition.values()) {
			JMenuItem dispositionMenuItem = addCheckboxMenuItem(dispositionMenu, disposition.getDescription(), -1,
					disposition == imageHandlingPreferences.getDisposition(), event -> {
						imageHandlingPreferences.setDisposition(disposition);
						updateDispositionMenu(disposition);
					});
			dispositions.put(disposition, dispositionMenuItem);

		}
		addCheckboxMenuItem(imageHandlingMenu, "Movement in percentage", MOVEMENT_IN_PERCENTAGE_KEY,
				imageHandlingPreferences.isRelativeMovement(), event -> {
					imageHandlingPreferences.setRelativeMovement(!imageHandlingPreferences.isRelativeMovement());
				});
		addCheckboxMenuItem(imageHandlingMenu, "Move all images", MOVE_ALL_IMAGES_KEY,
				imageHandlingPreferences.isMovementAppliedToAllImages(), event -> {
					imageHandlingPreferences
							.setMovementAppliedToAllImages(!imageHandlingPreferences.isMovementAppliedToAllImages());
				});
		addCheckboxMenuItem(imageHandlingMenu, "Show position", SHOW_POSITION_MINIATURE_KEY,
				imageHandlingPreferences.isPositionMiniatureVisible(), event -> {
					imageHandlingPreferences
							.setPositionMiniatureVisible(!imageHandlingPreferences.isPositionMiniatureVisible());
				});
		addCheckboxMenuItem(imageHandlingMenu, "Normalize for crop factor", NORMALIZE_FOR_CROP_FACTOR_KEY,
				imageHandlingPreferences.isNormalizedForCrop(), event -> {
					imageHandlingPreferences.setNormalizedForCrop(!imageHandlingPreferences.isNormalizedForCrop());
				});
		addCheckboxMenuItem(imageHandlingMenu, "Normalize for focal length", NORMALIZE_FOR_FOCAL_LENGTH_KEY,
				imageHandlingPreferences.isNormalizedForFocalLength(), event -> {
					imageHandlingPreferences
							.setNormalizedForFocalLength(!imageHandlingPreferences.isNormalizedForFocalLength());
				});

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

	private void updateDispositionMenu(final Disposition disposition) {
		for (Map.Entry<Disposition, JMenuItem> entry : dispositions.entrySet()) {
			entry.getValue().setSelected(entry.getKey() == disposition);
		}
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals(CommunicationMessages.AUTOROTATION_CHANGED)) {
			autorotationMenuItem.setSelected(imageHandlingPreferences.isAutorotation());
		}
	}
}
