package com.threeamigos.imageviewer.implementations.persister;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import com.threeamigos.imageviewer.interfaces.persister.Persister;
import com.threeamigos.imageviewer.interfaces.ui.PathPreferences;

public class PathPreferencesPersisterImpl extends AbstractPreferencesPersisterImpl<PathPreferences>
		implements Persister<PathPreferences> {

	private static final String PATH_FILENAME = "path.preferences";

	@Override
	public String getNamePart() {
		return PATH_FILENAME;
	}

	@Override
	protected String getEntityDescription() {
		return "last path";
	}

	@Override
	protected void loadImpl(BufferedReader reader, PathPreferences pathPreferences)
			throws IOException, IllegalArgumentException {
		String line;
		String path = null;
		while ((line = reader.readLine()) != null) {
			if (!line.isEmpty() && !line.isBlank()) {
				path = line;
				break;
			}
		}
		if (path == null) {
			throw new IllegalArgumentException("No path found in " + getEntityDescription() + " preferences file");
		}
		File file = new File(path);
		if (!file.exists()) {
			throw new IllegalArgumentException("Path " + path + " for " + getEntityDescription() + " does not exist");
		}
		if (!file.isDirectory()) {
			throw new IllegalArgumentException(
					"Path " + path + " for " + getEntityDescription() + " is not a directory");
		}
		if (!file.canRead()) {
			throw new IllegalArgumentException("Path " + path + " for " + getEntityDescription() + " cannot be read");
		}

		pathPreferences.setLastPath(path);
	}

	@Override
	protected void saveImpl(PrintWriter writer, PathPreferences pathPreferences) throws IOException {
		writer.write(pathPreferences.getLastPath());
	}

}
