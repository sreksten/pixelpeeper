package com.threeamigos.pixelpeeper.implementations.persister;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.threeamigos.common.util.interfaces.filesystem.RootPathProvider;
import com.threeamigos.common.util.interfaces.messagehandler.ExceptionHandler;
import com.threeamigos.pixelpeeper.interfaces.persister.PersistResult;
import com.threeamigos.pixelpeeper.interfaces.persister.Persister;

public abstract class FilePersister<T> implements Persister<T> {

	protected final ExceptionHandler exceptionHandler;
	private final String rootPath;
	private final boolean rootPathAccessible;

	protected FilePersister(RootPathProvider rootPathProvider, ExceptionHandler exceptionHandler) {
		this.exceptionHandler = exceptionHandler;
		rootPath = rootPathProvider.getRootPath();
		rootPathAccessible = rootPathProvider.isRootPathAccessible();
	}

	public String getFilenameWithPath() {
		return new StringBuilder(rootPath).append(File.separatorChar).append(getNamePart()).toString();
	}

	protected abstract String getEntityDescription();

	protected abstract String getNamePart();

	@Override
	public PersistResult load(T entity) {
		if (rootPathAccessible) {
			String filename = getFilenameWithPath();
			File file = new File(filename);
			if (!file.exists()) {
				return FilePersistResultImpl.notFound(getEntityDescription());
			}
			if (!file.canRead()) {
				return FilePersistResultImpl.cannotBeRead(getEntityDescription());
			}

			try (InputStream inputStream = new FileInputStream(filename)) {
				load(inputStream, entity);
			} catch (Exception e) {
				return new FilePersistResultImpl(e.getMessage());
			}

			FilePersistResultImpl result = new FilePersistResultImpl();
			result.setFilename(filename);
			return result;
		} else {
			return FilePersistResultImpl.pathNotAccessible();
		}
	}

	protected abstract void load(InputStream inputStream, T entity) throws IOException, IllegalArgumentException;

	@Override
	public PersistResult save(T entity) {
		if (rootPathAccessible) {
			String filename = getFilenameWithPath();
			File file = new File(filename);
			if (file.exists() && !file.canWrite()) {
				return new FilePersistResultImpl(getEntityDescription() + " file cannot be written.");
			}
			try (OutputStream outputStream = new FileOutputStream(filename)) {
				save(outputStream, entity);
			} catch (Exception e) {
				return new FilePersistResultImpl(e.getMessage());
			}
			FilePersistResultImpl result = new FilePersistResultImpl();
			result.setFilename(filename);
			return result;
		} else {
			return FilePersistResultImpl.pathNotAccessible();
		}
	}

	protected abstract void save(OutputStream outputStream, T entity) throws IOException;

}
