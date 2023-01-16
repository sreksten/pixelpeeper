package com.threeamigos.imageviewer.interfaces.preferences;

import com.threeamigos.imageviewer.interfaces.persister.Persistable;

public interface EdgesDetectorPreferences extends Persistable {

	public static final boolean SHOW_EDGES_DEFAULT = false;
	public static final int EDGES_TRANSPARENCY_DEFAULT = 30;
	public static final EdgesDetectorFlavour EDGES_DETECTOR_FLAVOUR_DEFAULT = EdgesDetectorFlavour.CANNY_EDGES_DETECTOR;

	public void setShowEdges(boolean showEdges);

	public boolean isShowEdges();

	public void setEdgesTransparency(int edgesTransparency);

	public int getEdgesTransparency();

	public void setEdgesDetectorFlavour(EdgesDetectorFlavour flavour);

	public EdgesDetectorFlavour getEdgesDetectorFlavour();

}
