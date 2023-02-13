package com.threeamigos.pixelpeeper.implementations.persister;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

import com.threeamigos.common.util.interfaces.ExceptionHandler;
import com.threeamigos.common.util.preferences.filebased.interfaces.RootPathProvider;

public abstract class TextFilePersister<T> extends FilePersister<T> {

	protected TextFilePersister(RootPathProvider rootPathProvider, ExceptionHandler exceptionHandler) {
		super(rootPathProvider, exceptionHandler);
	}

	@Override
	protected void load(InputStream inputStream, T entity) throws IOException, IllegalArgumentException {
		loadFromText(new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8)), entity);
	}

	protected abstract void loadFromText(BufferedReader reader, T entity) throws IOException, IllegalArgumentException;

	@Override
	protected void save(OutputStream outputStream, T entity) throws IOException {
		try (PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8))) {
			save(printWriter, entity);
		}
	}

	protected abstract void save(PrintWriter printWriter, T entity) throws IOException, IllegalArgumentException;

}
