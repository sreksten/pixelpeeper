package com.threeamigos.imageviewer.implementations.persister;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import com.threeamigos.common.util.preferences.filebased.interfaces.RootPathProvider;
import com.threeamigos.imageviewer.interfaces.persister.Persister;
import com.threeamigos.imageviewer.interfaces.preferences.PathPreferences;

public class TextBasedPathPreferencesPersister extends TextBasedAbstractPreferencesPersister<PathPreferences>
		implements Persister<PathPreferences> {

	private static final String PATH_FILENAME = "path.preferences";
	private static final String PATH_PROPERTY = "path";
	private static final String FILE_PROPERTY = "file";
	private static final String PATH_ = "Path ";
	private static final String _FOR_ = " for ";

	public TextBasedPathPreferencesPersister(RootPathProvider rootPathProvider) {
		super(rootPathProvider);
	}
	
	@Override
	public String getNamePart() {
		return PATH_FILENAME;
	}

	@Override
	protected String getEntityDescription() {
		return "path and files";
	}

	@Override
	protected void loadImpl(BufferedReader reader, PathPreferences pathPreferences)
			throws IOException, IllegalArgumentException {
		String line;
		String path = null;
		List<String> filenames = new ArrayList<>();
		while ((line = reader.readLine()) != null) {
			if (!line.trim().isEmpty()) {
				StringTokenizer st = new StringTokenizer(line, "=");
				String key = st.nextToken();
				if (key.equalsIgnoreCase(PATH_PROPERTY)) {
					path = st.nextToken();
				} else if (key.equalsIgnoreCase(FILE_PROPERTY)) {
					String filename = st.nextToken().trim();
					if (!filename.isEmpty()) {
						filenames.add(filename);
					}
				}
			}
		}
		if (path == null) {
			throw new IllegalArgumentException("No path found in " + getEntityDescription() + " preferences file");
		}
		File file = new File(path);
		if (!file.exists()) {
			throw new IllegalArgumentException(
					PATH_ + path + _FOR_ + getEntityDescription() + " preferences does not exist");
		}
		if (!file.isDirectory()) {
			throw new IllegalArgumentException(
					PATH_ + path + _FOR_ + getEntityDescription() + " preferences is not a directory");
		}
		if (!file.canRead()) {
			throw new IllegalArgumentException(
					PATH_ + path + _FOR_ + getEntityDescription() + " preferences cannot be read");
		}
		pathPreferences.setLastPath(path);

		for (String filename : filenames) {
			file = new File(path + File.separator + filename);
			if (!file.exists()) {
				throw new IllegalArgumentException(
						"File " + filename + _FOR_ + getEntityDescription() + " preferences does not exist");
			}
			if (!file.isFile()) {
				throw new IllegalArgumentException(
						"File " + filename + _FOR_ + getEntityDescription() + " preferences is not a file");
			}
			if (!file.canRead()) {
				throw new IllegalArgumentException(
						"File " + filename + _FOR_ + getEntityDescription() + " preferences cannot be read");
			}
		}
		pathPreferences.setLastFilenames(filenames);
	}

	@Override
	protected void saveImpl(PrintWriter writer, PathPreferences pathPreferences) throws IOException {
		writer.println(PATH_PROPERTY + "=" + pathPreferences.getLastPath());
		for (String filename : pathPreferences.getLastFilenames()) {
			writer.println(FILE_PROPERTY + "=" + filename);
		}
	}
}
