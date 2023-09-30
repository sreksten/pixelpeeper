package com.threeamigos.pixelpeeper.implementations.persister;

import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.InstanceCreator;
import com.threeamigos.common.util.implementations.GsonColorAdapter;
import com.threeamigos.common.util.interfaces.filesystem.RootPathProvider;
import com.threeamigos.common.util.interfaces.messagehandler.ExceptionHandler;

public class JsonFilePersister<T> extends FilePersister<T> {

	private final String filename;
	private final String entityDescription;

	public JsonFilePersister(String filename, String entityDescription, RootPathProvider rootPathProvider,
			ExceptionHandler exceptionHandler) {
		super(rootPathProvider, exceptionHandler);
		this.filename = filename;
		this.entityDescription = entityDescription;
	}

	@Override
	protected void load(InputStream inputStream, T entity) throws IOException, IllegalArgumentException {
		InstanceCreator<T> creator = new InstanceCreator<T>() {
			public T createInstance(Type type) {
				return entity;
			}
		};
		GsonBuilder builder = new GsonBuilder();
		builder.registerTypeAdapter(Color.class, new GsonColorAdapter());
		builder.registerTypeAdapter(entity.getClass(), creator).create();
		Gson gson = builder.create();
		gson.fromJson(new InputStreamReader(inputStream, StandardCharsets.UTF_8), entity.getClass());
	}

	@Override
	protected void save(OutputStream outputStream, T entity) throws IOException {
		try (PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8))) {
			GsonBuilder builder = new GsonBuilder();
			builder.registerTypeAdapter(Color.class, new GsonColorAdapter());
			Gson gson = builder.create();
			printWriter.println(gson.toJson(entity));
		}
	}

	@Override
	protected String getEntityDescription() {
		return entityDescription;
	}

	@Override
	protected String getNamePart() {
		// TODO Auto-generated method stub
		return filename + ".json";
	}

}
