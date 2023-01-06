package com.threeamigos.imageviewer.implementations.persister;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.StringTokenizer;

import com.threeamigos.imageviewer.interfaces.persister.Persister;
import com.threeamigos.imageviewer.interfaces.ui.WindowPreferences;

public class WindowPreferencesPersisterImpl extends AbstractPreferencesPersisterImpl<WindowPreferences>
		implements Persister<WindowPreferences> {

	private static final String FRAME_DIMENSION_FILENAME = "frame.preferences";

	private static final String WIDTH = "width";
	private static final String HEIGHT = "height";
	private static final String X = "x";
	private static final String Y = "y";
	private static final String AUTOROTATION = "autorotation";
	private static final String TAGS_VISIBLE = "tags";
	private static final String MOVEMENT_APPLIED_TO_ALL_IMAGES = "movement_applied_to_all_images";

	@Override
	public String getNamePart() {
		return FRAME_DIMENSION_FILENAME;
	}

	@Override
	protected String getEntityDescription() {
		return "window";
	}

	@Override
	protected void loadImpl(BufferedReader reader, WindowPreferences windowPreferences)
			throws IOException, IllegalArgumentException {
		int width = 0;
		int height = 0;
		int x = -1;
		int y = -1;
		boolean autorotation = WindowPreferences.AUTOROTATION_DEFAULT;
		boolean tagsVisible = WindowPreferences.TAGS_VISIBLE_DEFAULT;
		boolean movementAppliesToAllImages = WindowPreferences.MOVEMENT_APPLIES_TO_ALL_IMAGES_DEFAULT;

		String line;
		while ((line = reader.readLine()) != null) {
			if (!line.trim().isEmpty()) {
				StringTokenizer st = new StringTokenizer(line, "=");
				String key = st.nextToken();
				String value = st.nextToken();
				if (WIDTH.equalsIgnoreCase(key)) {
					width = Integer.parseInt(value);
				} else if (HEIGHT.equalsIgnoreCase(key)) {
					height = Integer.parseInt(value);
				} else if (X.equalsIgnoreCase(key)) {
					x = Integer.parseInt(value);
				} else if (Y.equalsIgnoreCase(key)) {
					y = Integer.parseInt(value);
				} else if (AUTOROTATION.equalsIgnoreCase(key)) {
					autorotation = Boolean.valueOf(value);
				} else if (TAGS_VISIBLE.equals(key)) {
					tagsVisible = Boolean.valueOf(value);
				} else if (MOVEMENT_APPLIED_TO_ALL_IMAGES.equals(key)) {
					movementAppliesToAllImages = Boolean.valueOf(value);
				}
			}
		}

		checkBoundaries(width, height, x, y);

		windowPreferences.setWidth(width);
		windowPreferences.setHeight(height);
		windowPreferences.setX(x);
		windowPreferences.setY(y);
		windowPreferences.setAutorotation(autorotation);
		windowPreferences.setTagsVisible(tagsVisible);
		windowPreferences.setMovementAppliedToAllImages(movementAppliesToAllImages);
	}

	private void checkBoundaries(int width, int height, int x, int y) throws IllegalArgumentException {
		Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
		if (width <= 0) {
			throw new IllegalArgumentException("Invalid window preferences: width must be greater than 0");
		}
		if (height <= 0) {
			throw new IllegalArgumentException("Invalid window preferences: height must be greater than 0");
		}
		if (x < 0) {
			throw new IllegalArgumentException(
					"Invalid window preferences: x position must be equal or greater than 0");
		}
		if (x >= dimension.width) {
			throw new IllegalArgumentException(
					"Invalid window preferences: x position must be less than the screen width");
		}
		if (y < 0) {
			throw new IllegalArgumentException(
					"Invalid window preferences: y position must be equal or greater than 0");
		}
		if (y >= dimension.height) {
			throw new IllegalArgumentException(
					"Invalid window preferences: y position must be less than the screen height");
		}
	}

	@Override
	protected void saveImpl(PrintWriter writer, WindowPreferences windowPreferences) throws IOException {
		writer.println(WIDTH + "=" + windowPreferences.getWidth());
		writer.println(HEIGHT + "=" + windowPreferences.getHeight());
		writer.println(X + "=" + windowPreferences.getX());
		writer.println(Y + "=" + windowPreferences.getY());
		writer.println(AUTOROTATION + "=" + windowPreferences.isAutorotation());
		writer.println(TAGS_VISIBLE + "=" + windowPreferences.isTagsVisible());
		writer.println(MOVEMENT_APPLIED_TO_ALL_IMAGES + "=" + windowPreferences.isMovementAppliedToAllImages());
	}

}
