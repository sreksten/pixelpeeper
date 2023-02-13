package com.threeamigos.pixelpeeper.interfaces.preferences.flavours;

import com.threeamigos.pixelpeeper.interfaces.preferences.ExifReaderFlavour;
import com.threeamigos.pixelpeeper.interfaces.preferences.ImageReaderFlavour;
import com.threeamigos.pixelpeeper.interfaces.preferences.Preferences;

public interface ImageHandlingPreferences extends Preferences {

	public static final boolean AUTOROTATION_DEFAULT = true;
	public static final Disposition DISPOSITION_DEFAULT = Disposition.VERTICAL;
	public static final float MIN_ZOOM_LEVEL = 10.0f;
	public static final float ZOOM_LEVEL_DEFAULT = 100.0f;
	public static final float MAX_ZOOM_LEVEL = 100.0f;
	public static final float ZOOM_LEVEL_STEP = 10.0f;
	public static final boolean NORMALIZED_FOR_CROP_DEFAULT = false;
	public static final boolean NORMALIZED_FOR_FOCAL_LENGTH_DEFAULT = false;
	public static final boolean MOVEMENT_IN_PERCENTAGE_DEFAULT = true;
	public static final boolean MOVEMENT_APPLIED_TO_ALL_IMAGES_DEFAULT = true;
	public static final boolean POSITION_MINIATURE_VISIBLE_DEFAULT = false;
	public static final ImageReaderFlavour IMAGE_READER_FLAVOUR_DEFAULT = ImageReaderFlavour.APACHE_COMMONS_IMAGING;
	public static final ExifReaderFlavour METADATA_READER_FLAVOUR_DEFAULT = ExifReaderFlavour.DREW_NOAKES;

	default String getDescription() {
		return "Image handling preferences";
	}

	public void setAutorotation(boolean autorotation);

	public boolean isAutorotation();

	public void setDisposition(Disposition disposition);

	public Disposition getDisposition();

	public void setZoomLevel(int zoomLevel);

	public int getZoomLevel();

	public void setNormalizedForCrop(boolean adaptToCrop);

	public boolean isNormalizedForCrop();

	public void setNormalizedForFocalLength(boolean adaptToFocalLength);

	public boolean isNormalizedForFocalLength();

	public void setMovementAppliedToAllImages(boolean movementAppliesToAllImages);

	public boolean isMovementAppliedToAllImages();

	public void setRelativeMovement(boolean movementInPercentage);

	public boolean isRelativeMovement();

	public void setPositionMiniatureVisible(boolean positionMiniatureVisible);

	public boolean isPositionMiniatureVisible();

	public void setImageReaderFlavour(ImageReaderFlavour imageReaderFlavour);

	public ImageReaderFlavour getImageReaderFlavour();

	public void setExifReaderFlavour(ExifReaderFlavour exifReaderFlavour);

	public ExifReaderFlavour getExifReaderFlavour();

	public enum Disposition {

		VERTICAL("Vertical"), HORIZONTAL("Horizontal"), GRID("Grid");

		private String description;

		private Disposition(String description) {
			this.description = description;
		}

		public String getDescription() {
			return description;
		}
	}

}
