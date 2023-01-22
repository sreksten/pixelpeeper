package com.threeamigos.imageviewer.interfaces.preferences.flavours;

import com.threeamigos.imageviewer.interfaces.edgedetect.EdgesDetectorFlavour;
import com.threeamigos.imageviewer.interfaces.preferences.Preferences;

public interface EdgesDetectorPreferences extends Preferences {

	public static final boolean SHOW_EDGES_DEFAULT = false;
	public static final int EDGES_TRANSPARENCY_DEFAULT = 30;
	public static final EdgesDetectorFlavour EDGES_DETECTOR_FLAVOUR_DEFAULT = EdgesDetectorFlavour.CANNY_EDGES_DETECTOR;

	default String getDescription() {
		return "Edges Detector preferences";
	}

	public void setShowEdges(boolean showEdges);

	public boolean isShowEdges();

	public void setEdgesTransparency(int edgesTransparency);

	public int getEdgesTransparency();

	public void setEdgesDetectorFlavour(EdgesDetectorFlavour flavour);

	public EdgesDetectorFlavour getEdgesDetectorFlavour();

}
