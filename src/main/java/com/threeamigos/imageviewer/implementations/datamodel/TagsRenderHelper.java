package com.threeamigos.imageviewer.implementations.datamodel;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

import com.threeamigos.imageviewer.data.ExifTag;
import com.threeamigos.imageviewer.data.PictureData;
import com.threeamigos.imageviewer.interfaces.datamodel.CommonTagsHelper;
import com.threeamigos.imageviewer.interfaces.preferences.ExifTagPreferences;
import com.threeamigos.imageviewer.interfaces.preferences.WindowPreferences;
import com.threeamigos.imageviewer.interfaces.ui.FontService;

public class TagsRenderHelper {

	private static final int HSPACING = 5;
	private static final int VSPACING = 5;

	private static final int FONT_HEIGHT = 16;

	private final Graphics2D g2d;
	private final int x;
	private final FontService fontService;
	private final PictureData pictureData;
	private final WindowPreferences windowPreferences;
	private final ExifTagPreferences tagPreferences;
	private final CommonTagsHelper commonTagsHelper;

	private int y;

	TagsRenderHelper(Graphics2D g2d, int x, int y, FontService fontService, PictureData pictureData,
			WindowPreferences windowPreferences, ExifTagPreferences tagPreferences, CommonTagsHelper commonTagsHelper) {
		this.g2d = g2d;
		this.x = x + HSPACING;
		this.y = y - VSPACING - FONT_HEIGHT;
		this.fontService = fontService;
		this.pictureData = pictureData;
		this.windowPreferences = windowPreferences;
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
		drawString(g2d, pictureData.getFilename(), x, y, Color.WHITE);
	}

	private void info(ExifTag exifTag) {
		boolean tagVisible = tagPreferences.isTagVisible(exifTag);
		if (tagVisible && windowPreferences.isTagsVisibleOnlyIfDifferent()) {
			tagVisible = !commonTagsHelper.isCommonTag(exifTag);
		}
		if (tagVisible) {
			String tagDescription = exifTag.getDescription();
			String tagValue = pictureData.getTagDescriptive(exifTag);
			drawString(g2d, String.format("%s: %s", tagDescription, tagValue), x, y,
					commonTagsHelper.isCommonTag(exifTag) ? Color.GREEN : Color.YELLOW);
			y -= FONT_HEIGHT + VSPACING;
		}
	}
//
//	protected void drawString(Graphics2D graphics, String s, int x, int y) {
//		drawString(graphics, s, x, y, Color.WHITE);
//	}

	protected void drawString(Graphics2D graphics, String s, int x, int y, Color color) {
		graphics.setColor(Color.BLACK);
		for (int i = x - 1; i <= x + 1; i++) {
			for (int j = y - 1; j <= y + 1; j++) {
				graphics.drawString(s, i, j);
			}
		}
		graphics.setColor(color);
		graphics.drawString(s, x, y);
	}

}
