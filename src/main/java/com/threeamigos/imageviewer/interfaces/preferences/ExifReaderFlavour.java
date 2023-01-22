package com.threeamigos.imageviewer.interfaces.preferences;

public enum ExifReaderFlavour {

	DREW_NOAKES("Drew Noakes' library");

	private String description;

	private ExifReaderFlavour(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}

}
