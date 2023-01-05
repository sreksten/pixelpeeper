package com.threeamigos.imageviewer.implementations.persister;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import com.threeamigos.imageviewer.interfaces.persister.PersistResult;
import com.threeamigos.imageviewer.interfaces.persister.Persister;

public abstract class AbstractPreferencesPersisterImpl<T> implements Persister<T> {

	@Override
	public PersistResult load(T entity) {
		String filename = getFilenameWithPath();
		File file = new File(filename);
		if (!file.exists()) {
			return FilePersistResultImpl.notFound(getEntityDescription());
		}
		if (!file.canRead()) {
			return FilePersistResultImpl.cannotBeRead(getEntityDescription());
		}

		try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
			loadImpl(reader, entity);
		} catch (Exception e) {
			return new FilePersistResultImpl(e.getMessage());
		}

		FilePersistResultImpl result = new FilePersistResultImpl();
		result.setFilename(filename);
		return result;
	}

	@Override
	public PersistResult save(T entity) {
		String filename = getFilenameWithPath();
		File file = new File(filename);
		if (file.exists() && !file.canWrite()) {
			return new FilePersistResultImpl(getEntityDescription() + " preferences file cannot be written.");
		}
		try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
			saveImpl(writer, entity);
		} catch (Exception e) {
			return new FilePersistResultImpl(e.getMessage());
		}
		FilePersistResultImpl result = new FilePersistResultImpl();
		result.setFilename(filename);
		return result;
	}

	private String getPreferencesPath() {
		String preferencesPath = new StringBuilder(System.getProperty("user.home")).append(File.separatorChar)
				.append(".com.threeamigos.imageviewer").toString();
		new File(preferencesPath).mkdirs();
		return preferencesPath;
	}

	public String getFilenameWithPath() {
		return new StringBuilder(getPreferencesPath()).append(File.separatorChar).append(getNamePart()).toString();
	}

	protected abstract String getEntityDescription();

	protected abstract String getNamePart();

	protected abstract void loadImpl(BufferedReader reader, T entity) throws IOException, IllegalArgumentException;

	protected abstract void saveImpl(PrintWriter writer, T entity) throws IOException;

}
