package com.threeamigos.imageviewer.implementations.persister;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import com.threeamigos.common.util.preferences.filebased.interfaces.PreferencesRootPathProvider;
import com.threeamigos.imageviewer.interfaces.persister.PersistResult;
import com.threeamigos.imageviewer.interfaces.persister.Persister;

public abstract class FileBasedAbstractPreferencesPersister<T> implements Persister<T> {

	private final String preferencesPath;
	private final boolean preferencesPathAccessible;

	protected FileBasedAbstractPreferencesPersister(PreferencesRootPathProvider rootPathProvider) {
		preferencesPath = rootPathProvider.getRootPath();
		preferencesPathAccessible = rootPathProvider.isRootPathAccessible();
	}

	@Override
	public PersistResult load(T entity) {
		if (preferencesPathAccessible) {
			String filename = getFilenameWithPath();
			File file = new File(filename);
			if (!file.exists()) {
				return PreferencesFilePersistResult.notFound(getEntityDescription());
			}
			if (!file.canRead()) {
				return PreferencesFilePersistResult.cannotBeRead(getEntityDescription());
			}

			try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
				loadImpl(reader, entity);
			} catch (Exception e) {
				return new PreferencesFilePersistResult(e.getMessage());
			}

			PreferencesFilePersistResult result = new PreferencesFilePersistResult();
			result.setFilename(filename);
			return result;
		} else {
			return PreferencesFilePersistResult.preferencesPathNotAccessible();
		}
	}

	@Override
	public PersistResult save(T entity) {
		if (preferencesPathAccessible) {
			String filename = getFilenameWithPath();
			File file = new File(filename);
			if (file.exists() && !file.canWrite()) {
				return new PreferencesFilePersistResult(getEntityDescription() + " preferences file cannot be written.");
			}
			try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
				saveImpl(writer, entity);
			} catch (Exception e) {
				return new PreferencesFilePersistResult(e.getMessage());
			}
			PreferencesFilePersistResult result = new PreferencesFilePersistResult();
			result.setFilename(filename);
			return result;
		} else {
			return PreferencesFilePersistResult.preferencesPathNotAccessible();
		}
	}

	public String getFilenameWithPath() {
		return new StringBuilder(preferencesPath).append(File.separatorChar).append(getNamePart()).toString();
	}

	protected abstract String getEntityDescription();

	protected abstract String getNamePart();

	protected abstract void loadImpl(BufferedReader reader, T entity) throws IOException, IllegalArgumentException;

	protected abstract void saveImpl(PrintWriter writer, T entity) throws IOException;

}
