package com.threeamigos.pixelpeeper.interfaces.edgedetect;

public enum EdgesDetectorFlavour {

	// Implements the Canny edge detection algorithm
	CANNY_EDGES_DETECTOR("Canny Edges Detector"),
	// A test edge detector just to check the code
	ROMY_JONA_EDGES_DETECTOR("Romy Jona Edges Detector"),
	// Sobel
	SOBEL_EDGES_DETECTOR("Sobel Edges Detector");

	private final String description;

	private EdgesDetectorFlavour(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}
}
