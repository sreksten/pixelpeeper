package com.threeamigos.imageviewer.implementations.persister;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;

import com.threeamigos.common.util.interfaces.ExceptionHandler;
import com.threeamigos.common.util.preferences.filebased.interfaces.RootPathProvider;

public abstract class TextFilePersister<T> extends FilePersister<T> {

	protected TextFilePersister(RootPathProvider rootPathProvider, ExceptionHandler exceptionHandler) {
		super(rootPathProvider, exceptionHandler);
	}

	@Override
	protected void load(InputStream inputStream, T entity) throws IOException, IllegalArgumentException {
		load(new BufferedReader(new InputStreamReader(inputStream)), entity);
	}

	protected abstract void load(BufferedReader reader, T entity) throws IOException, IllegalArgumentException;

	@Override
	protected void save(OutputStream outputStream, T entity) throws IOException {
		save(new PrintWriter(outputStream), entity);
	}

	protected abstract void save(PrintWriter printWriter, T entity) throws IOException, IllegalArgumentException;

}
