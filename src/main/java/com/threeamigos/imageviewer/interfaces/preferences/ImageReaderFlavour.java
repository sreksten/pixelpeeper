package com.threeamigos.imageviewer.interfaces.preferences;

public enum ImageReaderFlavour {

	// Standard java libraries
	JAVA("Java ImageIO"),
	// Extern java libraries
	APACHE_COMMONS_IMAGING("Apache Commons Imaging");

	private String description;

	private ImageReaderFlavour(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}

}
