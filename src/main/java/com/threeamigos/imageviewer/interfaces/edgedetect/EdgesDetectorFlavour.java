package com.threeamigos.imageviewer.interfaces.edgedetect;

public enum EdgesDetectorFlavour {

	CANNY_EDGES_DETECTOR("Canny Edges Detector"), ROMY_JONA_EDGES_DETECTOR("Romy Jona Detector");

	private final String description;

	private EdgesDetectorFlavour(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}
}
