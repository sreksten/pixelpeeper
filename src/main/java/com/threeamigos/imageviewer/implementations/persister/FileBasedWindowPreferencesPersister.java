package com.threeamigos.imageviewer.implementations.persister;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.StringTokenizer;

import com.threeamigos.common.util.preferences.filebased.interfaces.PreferencesRootPathProvider;
import com.threeamigos.imageviewer.interfaces.persister.Persister;
import com.threeamigos.imageviewer.interfaces.preferences.WindowPreferences;

public class FileBasedWindowPreferencesPersister extends FileBasedAbstractPreferencesPersister<WindowPreferences>
		implements Persister<WindowPreferences> {

	private static final String WINDOW_PREFERENCES_FILENAME = "window.preferences";

	private static final String WIDTH = "width";
	private static final String HEIGHT = "height";
	private static final String X = "x";
	private static final String Y = "y";
	private static final String AUTOROTATION = "autorotation";
	private static final String MOVEMENT_APPLIED_TO_ALL_IMAGES = "movement_applied_to_all_images";
	private static final String SHOW_EDGE_IMAGES = "show_edge_images";
	private static final String EDGE_IMAGES_TRANSPARENCY = "edge_images_transparency";

	public FileBasedWindowPreferencesPersister(PreferencesRootPathProvider rootPathProvider) {
		super(rootPathProvider);
	}
	
	@Override
	public String getNamePart() {
		return WINDOW_PREFERENCES_FILENAME;
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
		boolean movementAppliesToAllImages = WindowPreferences.MOVEMENT_APPLIES_TO_ALL_IMAGES_DEFAULT;
		boolean showEdgeImages = WindowPreferences.SHOWING_EDGE_IMAGES_DEFAULT;
		int edgeImagesTransparency = WindowPreferences.EDGE_IMAGES_TRANSPARENCY_DEFAULT;

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
				} else if (MOVEMENT_APPLIED_TO_ALL_IMAGES.equals(key)) {
					movementAppliesToAllImages = Boolean.valueOf(value);
				} else if (SHOW_EDGE_IMAGES.equals(key)) {
					showEdgeImages = Boolean.valueOf(value);
				} else if (EDGE_IMAGES_TRANSPARENCY.equals(key)) {
					edgeImagesTransparency = Integer.parseInt(value);
				}
			}
		}

		checkBoundaries(width, height, x, y);

		windowPreferences.setWidth(width);
		windowPreferences.setHeight(height);
		windowPreferences.setX(x);
		windowPreferences.setY(y);
		windowPreferences.setAutorotation(autorotation);
		windowPreferences.setMovementAppliedToAllImages(movementAppliesToAllImages);
		windowPreferences.setShowEdgeImages(showEdgeImages);
		windowPreferences.setEdgeImagesTransparency(edgeImagesTransparency);
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
		writer.println(MOVEMENT_APPLIED_TO_ALL_IMAGES + "=" + windowPreferences.isMovementAppliedToAllImages());
		writer.println(SHOW_EDGE_IMAGES + "=" + windowPreferences.isShowEdgeImages());
		writer.println(EDGE_IMAGES_TRANSPARENCY + "=" + windowPreferences.getEdgeImagesTransparency());
	}

}
