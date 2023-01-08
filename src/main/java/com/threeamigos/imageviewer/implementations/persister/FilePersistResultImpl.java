package com.threeamigos.imageviewer.implementations.persister;

import com.threeamigos.imageviewer.interfaces.persister.FilePersistResult;

class FilePersistResultImpl implements FilePersistResult {

	public static final FilePersistResult notFound(String fileDescription) {
		FilePersistResultImpl persistResult = new FilePersistResultImpl(
				"No " + fileDescription + " preferences file found");
		persistResult.notFound = true;
		return persistResult;
	}

	public static final FilePersistResult cannotBeRead(String fileDescription) {
		return new FilePersistResultImpl(fileDescription + " preferences file cannot be read");
	}

	public static final FilePersistResult preferencesPathNotAccessible() {
		return new FilePersistResultImpl("Preferences directory file cannot be accessed");
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
