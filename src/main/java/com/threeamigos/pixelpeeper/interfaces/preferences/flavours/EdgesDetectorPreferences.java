package com.threeamigos.pixelpeeper.interfaces.preferences.flavours;

import com.threeamigos.common.util.interfaces.preferences.Preferences;
import com.threeamigos.pixelpeeper.interfaces.edgedetect.EdgesDetectorFlavour;

public interface EdgesDetectorPreferences extends Preferences {

    boolean SHOW_EDGES_DEFAULT = false;
    int NO_EDGES_TRANSPARENCY = 0;
    int EDGES_TRANSPARENCY_DEFAULT = 30;
    int TOTAL_EDGES_TRANSPARENCY = 100;
    EdgesDetectorFlavour EDGES_DETECTOR_FLAVOUR_DEFAULT = EdgesDetectorFlavour.CANNY_EDGES_DETECTOR;

    default String getDescription() {
        return "Edges Detector preferences";
    }

    void setShowEdges(boolean showEdges);

    boolean isShowEdges();

    void setEdgesTransparency(int edgesTransparency);

    int getEdgesTransparency();

    void setEdgesDetectorFlavour(EdgesDetectorFlavour flavour);

    EdgesDetectorFlavour getEdgesDetectorFlavour();

}
