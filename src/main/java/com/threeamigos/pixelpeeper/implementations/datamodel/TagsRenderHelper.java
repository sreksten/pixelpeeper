package com.threeamigos.pixelpeeper.implementations.datamodel;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

import com.threeamigos.common.util.ui.draganddrop.BorderedStringRenderer;
import com.threeamigos.pixelpeeper.data.ExifTag;
import com.threeamigos.pixelpeeper.data.ExifTagVisibility;
import com.threeamigos.pixelpeeper.data.PictureData;
import com.threeamigos.pixelpeeper.interfaces.datamodel.TagsClassifier;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavours.ExifTagPreferences;
import com.threeamigos.pixelpeeper.interfaces.ui.FontService;

public class TagsRenderHelper {

	private static final int HSPACING = 5;
	private static final int VSPACING = 5;

	private static final int FONT_HEIGHT = 16;

	private final Graphics2D g2d;
	private final int x;
	private final FontService fontService;
	private final PictureData pictureData;
	private final ExifTagPreferences tagPreferences;
	private final TagsClassifier commonTagsHelper;

	private int y;

	TagsRenderHelper(Graphics2D g2d, int x, int y, FontService fontService, PictureData pictureData,
			ExifTagPreferences tagPreferences, TagsClassifier commonTagsHelper) {
		this.g2d = g2d;
		this.x = x + HSPACING;
		this.y = y - VSPACING - FONT_HEIGHT;
		this.fontService = fontService;
		this.pictureData = pictureData;
		this.tagPreferences = tagPreferences;
		this.commonTagsHelper = commonTagsHelper;
	}

	public void render() {

		if (tagPreferences.isTagsVisible()) {
			Font smallerFont = fontService.getFont("Arial", Font.BOLD, FONT_HEIGHT);
			g2d.setFont(smallerFont);

			info(ExifTag.HDR);
			info(ExifTag.SHARPNESS);
			info(ExifTag.SATURATION);
			info(ExifTag.CONTRAST);
			info(ExifTag.GAIN_CONTROL);
			info(ExifTag.DIGITAL_ZOOM_RATIO);
			info(ExifTag.COLOR_SPACE);
			info(ExifTag.FLASH);
			info(ExifTag.FOCUS_MODE);
			info(ExifTag.COLOR_TEMPERATURE);
			info(ExifTag.WHITE_BALANCE_MODE);
			info(ExifTag.WHITE_BALANCE);
			info(ExifTag.METERING_MODE);
			info(ExifTag.DISTANCE_FROM_SUBJECT);
			info(ExifTag.EXPOSURE_MODE);
			info(ExifTag.EXPOSURE_PROGRAM);
			info(ExifTag.EXPOSURE_TIME);
			info(ExifTag.ISO);
			info(ExifTag.APERTURE);
			info(ExifTag.FOCAL_LENGTH_35MM_EQUIVALENT);
			info(ExifTag.FOCAL_LENGTH);
			info(ExifTag.PICTURE_DATE);
			info(ExifTag.IMAGE_DIMENSIONS);
			info(ExifTag.IMAGE_ORIENTATION);
			info(ExifTag.LENS_FIRMWARE);
			info(ExifTag.LENS_MAXIMUM_APERTURE);
			info(ExifTag.LENS_MODEL);
			info(ExifTag.LENS_MANUFACTURER);
			info(ExifTag.CAMERA_FIRMWARE);
			info(ExifTag.CAMERA_MODEL);
			info(ExifTag.CAMERA_MANUFACTURER);
		}

		y -= FONT_HEIGHT;

		Font font = fontService.getFont("Arial", Font.BOLD, FONT_HEIGHT * 2);
		g2d.setFont(font);
		BorderedStringRenderer.drawString(g2d, pictureData.getFilename(), x, y, Color.BLACK, Color.WHITE);
	}

	private void info(ExifTag exifTag) {
		boolean tagVisible = tagPreferences.isTagsVisible();
		if (tagVisible) {
			if (!tagPreferences.isOverridingTagsVisibility()) {
				ExifTagVisibility visibility = tagPreferences.getTagVisibility(exifTag);
				tagVisible = visibility == ExifTagVisibility.YES || visibility == ExifTagVisibility.ONLY_IF_DIFFERENT
						&& (commonTagsHelper.getTotalMappedPictures() == 1 || !commonTagsHelper.isCommonTag(exifTag));
			}

			if (tagVisible) {
				String tagDescription = exifTag.getDescription();
				String tagValue = pictureData.getTagDescriptive(exifTag);
				BorderedStringRenderer.drawString(g2d, String.format("%s: %s", tagDescription, tagValue), x, y,
						Color.BLACK,
						(commonTagsHelper.getTotalMappedPictures() == 1 || commonTagsHelper.isCommonTag(exifTag))
								? Color.GREEN
								: Color.YELLOW);
				y -= FONT_HEIGHT + VSPACING;
			}
		}
	}

}
