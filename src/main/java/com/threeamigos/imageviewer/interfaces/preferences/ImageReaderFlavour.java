package com.threeamigos.imageviewer.interfaces.preferences;

public enum ImageReaderFlavour {

	JAVA("Java ImageIO"), APACHE_COMMONS_IMAGING("Apache Commons Imaging");

	private String description;

	private ImageReaderFlavour(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}

}
