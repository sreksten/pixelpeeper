package com.threeamigos.imageviewer.data;

public enum ExifTagVisibility {

	YES("Yes"),
	ONLY_IF_DIFFERENT("Only if different"),
	NO("No");

	private String description;

	private ExifTagVisibility(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}

}
