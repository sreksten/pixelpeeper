package com.threeamigos.imageviewer.implementations.persister;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.StringTokenizer;

import com.threeamigos.common.util.interfaces.ExceptionHandler;
import com.threeamigos.common.util.preferences.filebased.interfaces.PreferencesRootPathProvider;
import com.threeamigos.imageviewer.interfaces.persister.Persister;
import com.threeamigos.imageviewer.interfaces.preferences.WindowPreferences;

public class FileBasedWindowPreferencesPersister extends FileBasedAbstractPreferencesPersister<WindowPreferences>
		implements Persister<WindowPreferences> {

	private static final String WINDOW_PREFERENCES_FILENAME = "window.preferences";

	private static final String MAIN_WINDOW_WIDTH = "main_window_width";
	private static final String MAIN_WINDOOW_HEIGHT = "main_window_height";
	private static final String MAIN_WINDOW_X = "main_window_x";
	private static final String MAIN_WINDOW_Y = "main_window_y";

	private static final String DRAG_AND_DROP_WINDOW_VISIBLE = "drag_and_drop_window_visible";
	private static final String DRAG_AND_DROP_WINDOW_WIDTH = "drag_and_drop_window_width";
	private static final String DRAG_AND_DROP_WINDOOW_HEIGHT = "drag_and_drop_window_height";
	private static final String DRAG_AND_DROP_WINDOW_X = "drag_and_drop_window_x";
	private static final String DRAG_AND_DROP_WINDOW_Y = "drag_and_drop_window_y";

	private static final String AUTOROTATION = "images_autorotation";
	private static final String MOVEMENT_APPLIED_TO_ALL_IMAGES = "movement_applied_to_all_images";

	public FileBasedWindowPreferencesPersister(PreferencesRootPathProvider rootPathProvider,
			ExceptionHandler exceptionHandler) {
		super(rootPathProvider, exceptionHandler);
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
		int mainWindowWidth = 0;
		int mainWindowHeight = 0;
		int mainWindowX = -1;
		int mainWindowY = -1;

		boolean dragAndDropWindowVisible = false;
		int dragAndDropWindowWidth = 0;
		int dragAndDropWindowHeight = 0;
		int dragAndDropWindowX = -1;
		int dragAndDropWindowY = -1;

		boolean autorotation = WindowPreferences.AUTOROTATION_DEFAULT;
		boolean movementAppliesToAllImages = WindowPreferences.MOVEMENT_APPLIES_TO_ALL_IMAGES_DEFAULT;

		String line;
		while ((line = reader.readLine()) != null) {
			if (!line.trim().isEmpty()) {
				StringTokenizer st = new StringTokenizer(line, "=");
				String key = st.nextToken();
				String value = st.nextToken();
				if (MAIN_WINDOW_WIDTH.equalsIgnoreCase(key)) {
					mainWindowWidth = Integer.parseInt(value);
				} else if (MAIN_WINDOOW_HEIGHT.equalsIgnoreCase(key)) {
					mainWindowHeight = Integer.parseInt(value);
				} else if (MAIN_WINDOW_X.equalsIgnoreCase(key)) {
					mainWindowX = Integer.parseInt(value);
				} else if (MAIN_WINDOW_Y.equalsIgnoreCase(key)) {
					mainWindowY = Integer.parseInt(value);
				} else if (DRAG_AND_DROP_WINDOW_VISIBLE.equalsIgnoreCase(key)) {
					dragAndDropWindowVisible = Boolean.valueOf(value);
				} else if (DRAG_AND_DROP_WINDOW_WIDTH.equalsIgnoreCase(key)) {
					dragAndDropWindowWidth = Integer.parseInt(value);
				} else if (DRAG_AND_DROP_WINDOOW_HEIGHT.equalsIgnoreCase(key)) {
					dragAndDropWindowHeight = Integer.parseInt(value);
				} else if (DRAG_AND_DROP_WINDOW_X.equalsIgnoreCase(key)) {
					dragAndDropWindowX = Integer.parseInt(value);
				} else if (DRAG_AND_DROP_WINDOW_Y.equalsIgnoreCase(key)) {
					dragAndDropWindowY = Integer.parseInt(value);
				} else if (AUTOROTATION.equalsIgnoreCase(key)) {
					autorotation = Boolean.valueOf(value);
				} else if (MOVEMENT_APPLIED_TO_ALL_IMAGES.equals(key)) {
					movementAppliesToAllImages = Boolean.valueOf(value);
				}
			}
		}

		try {
			checkBoundaries("main", mainWindowWidth, mainWindowHeight, mainWindowX, mainWindowY);

			windowPreferences.setMainWindowWidth(mainWindowWidth);
			windowPreferences.setMainWindowHeight(mainWindowHeight);
			windowPreferences.setMainWindowX(mainWindowX);
			windowPreferences.setMainWindowY(mainWindowY);
		} catch (IllegalArgumentException e) {
			windowPreferences.loadMainWindowDefaultValues();
		}

		try {
			checkBoundaries("drag and drop", dragAndDropWindowWidth, dragAndDropWindowHeight, dragAndDropWindowX,
					dragAndDropWindowY);

			windowPreferences.setDragAndDropWindowVisible(dragAndDropWindowVisible);
			windowPreferences.setDragAndDropWindowWidth(dragAndDropWindowWidth);
			windowPreferences.setDragAndDropWindowHeight(dragAndDropWindowHeight);
			windowPreferences.setDragAndDropWindowX(dragAndDropWindowX);
			windowPreferences.setDragAndDropWindowY(dragAndDropWindowY);
		} catch (IllegalArgumentException e) {
			windowPreferences.loadDragAndDropWindowDefaultValues();
		}

		windowPreferences.setAutorotation(autorotation);
		windowPreferences.setMovementAppliedToAllImages(movementAppliesToAllImages);
	}

	private void checkBoundaries(String windowName, int width, int height, int x, int y)
			throws IllegalArgumentException {
		Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
		if (width <= 0) {
			throw new IllegalArgumentException(
					String.format("Invalid %s window preferences: width must be greater than 0", windowName));
		}
		if (height <= 0) {
			throw new IllegalArgumentException(
					String.format("Invalid %s window preferences: height must be greater than 0", windowName));
		}
		if (x < 0) {
			throw new IllegalArgumentException(String
					.format("Invalid %s window preferences: x position must be equal or greater than 0", windowName));
		}
		if (x >= dimension.width) {
			throw new IllegalArgumentException(String.format(
					"Invalid %s window preferences: x position must be less than the screen width", windowName));
		}
		if (y < 0) {
			throw new IllegalArgumentException(String
					.format("Invalid %s window preferences: y position must be equal or greater than 0", windowName));
		}
		if (y >= dimension.height) {
			throw new IllegalArgumentException(String.format(
					"Invalid %s window preferences: y position must be less than the screen height", windowName));
		}
	}

	@Override
	protected void saveImpl(PrintWriter writer, WindowPreferences windowPreferences) throws IOException {
		writer.println(MAIN_WINDOW_WIDTH + "=" + windowPreferences.getMainWindowWidth());
		writer.println(MAIN_WINDOOW_HEIGHT + "=" + windowPreferences.getMainWindowHeight());
		writer.println(MAIN_WINDOW_X + "=" + windowPreferences.getMainWindowX());
		writer.println(MAIN_WINDOW_Y + "=" + windowPreferences.getMainWindowY());

		writer.println(DRAG_AND_DROP_WINDOW_VISIBLE + "=" + windowPreferences.isDragAndDropWindowVisible());
		writer.println(DRAG_AND_DROP_WINDOW_WIDTH + "=" + windowPreferences.getDragAndDropWindowWidth());
		writer.println(DRAG_AND_DROP_WINDOOW_HEIGHT + "=" + windowPreferences.getDragAndDropWindowHeight());
		writer.println(DRAG_AND_DROP_WINDOW_X + "=" + windowPreferences.getDragAndDropWindowX());
		writer.println(DRAG_AND_DROP_WINDOW_Y + "=" + windowPreferences.getDragAndDropWindowY());

		writer.println(AUTOROTATION + "=" + windowPreferences.isAutorotation());
		writer.println(MOVEMENT_APPLIED_TO_ALL_IMAGES + "=" + windowPreferences.isMovementAppliedToAllImages());
	}

}
