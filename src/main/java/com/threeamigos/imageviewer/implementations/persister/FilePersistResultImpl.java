package com.threeamigos.imageviewer.implementations.persister;

import com.threeamigos.imageviewer.interfaces.persister.FilePersistResult;

class FilePersistResultImpl implements FilePersistResult {

	public static final FilePersistResultImpl notFound(String fileDescription) {
		FilePersistResultImpl persistResult = new FilePersistResultImpl("No " + fileDescription + " file found");
		persistResult.notFound = true;
		return persistResult;
	}

	public static final FilePersistResultImpl cannotBeRead(String fileDescription) {
		return new FilePersistResultImpl(fileDescription + " file cannot be read");
	}

	public static final FilePersistResultImpl pathNotAccessible() {
		return new FilePersistResultImpl("Preferences directory file cannot be accessed");
	}

	public static final FilePersistResultImpl fileNotWriteable() {
		return new FilePersistResultImpl(" cannot be written");
	}

	public static final FilePersistResultImpl notNeeded() {
		return new FilePersistResultImpl();
	}

	private final boolean successful;
	private boolean notFound;

	private String filename;

	private String error;

	FilePersistResultImpl() {
		successful = true;
	}

	FilePersistResultImpl(String error) {
		successful = false;
		this.error = error;
	}

	@Override
	public boolean isSuccessful() {
		return successful;
	}

	@Override
	public boolean isNotFound() {
		return notFound;
	}

	void setFilename(String filename) {
		this.filename = filename;
	}

	@Override
	public String getFilename() {
		return filename;
	}

	@Override
	public String getError() {
		return error;
	}

}
