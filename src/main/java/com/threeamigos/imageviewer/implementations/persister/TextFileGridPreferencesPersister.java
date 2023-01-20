package com.threeamigos.imageviewer.implementations.persister;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.StringTokenizer;

import com.threeamigos.common.util.interfaces.ExceptionHandler;
import com.threeamigos.common.util.preferences.filebased.interfaces.RootPathProvider;
import com.threeamigos.imageviewer.interfaces.persister.Persister;
import com.threeamigos.imageviewer.interfaces.preferences.flavours.GridPreferences;

public class TextFileGridPreferencesPersister extends TextFilePersister<GridPreferences>
		implements Persister<GridPreferences> {

	private static final String GRID_PREFERENCES_FILENAME = "grid.preferences";

	private static final String GRID_VISIBLE = "grid_visible";
	private static final String GRID_SPACING = "grid_spacing";

	public TextFileGridPreferencesPersister(RootPathProvider rootPathProvider, ExceptionHandler exceptionHandler) {
		super(rootPathProvider, exceptionHandler);
	}

	@Override
	public String getNamePart() {
		return GRID_PREFERENCES_FILENAME;
	}

	@Override
	protected String getEntityDescription() {
		return "grid preferences";
	}

	@Override
	protected void loadFromText(BufferedReader reader, GridPreferences gridPreferences)
			throws IOException, IllegalArgumentException {
		boolean gridVisible = GridPreferences.GRID_VISIBLE_DEFAULT;
		int gridSpacing = GridPreferences.GRID_SPACING_DEFAULT;

		String line;
		while ((line = reader.readLine()) != null) {
			if (!line.trim().isEmpty()) {
				StringTokenizer st = new StringTokenizer(line, "=");
				String key = st.nextToken();
				String value = st.nextToken();
				if (GRID_VISIBLE.equalsIgnoreCase(key)) {
					gridVisible = Boolean.valueOf(value);
				} else if (GRID_SPACING.equalsIgnoreCase(key)) {
					gridSpacing = Integer.parseInt(value);
				}
			}
		}

		gridPreferences.setGridVisible(gridVisible);
		gridPreferences.setGridSpacing(gridSpacing);
	}

	@Override
	protected void save(PrintWriter writer, GridPreferences gridPreferences) throws IOException {
		writer.println(GRID_VISIBLE + "=" + gridPreferences.isGridVisible());
		writer.println(GRID_SPACING + "=" + gridPreferences.getGridSpacing());
	}

}
