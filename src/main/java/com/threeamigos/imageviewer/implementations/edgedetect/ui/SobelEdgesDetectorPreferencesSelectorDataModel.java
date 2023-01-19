package com.threeamigos.imageviewer.implementations.edgedetect.ui;

import java.awt.Component;

import javax.swing.event.ChangeEvent;

import com.threeamigos.imageviewer.implementations.edgedetect.flavours.SobelEdgesDetectorImpl;
import com.threeamigos.imageviewer.interfaces.datamodel.DataModel;
import com.threeamigos.imageviewer.interfaces.edgedetect.EdgesDetector;
import com.threeamigos.imageviewer.interfaces.edgedetect.EdgesDetectorFlavour;
import com.threeamigos.imageviewer.interfaces.preferences.flavours.EdgesDetectorPreferences;

public class SobelEdgesDetectorPreferencesSelectorDataModel extends AbstractEdgesDetectorPreferencesSelectorDataModel {

	SobelEdgesDetectorPreferencesSelectorDataModel(DataModel dataModel,
			EdgesDetectorPreferences edgesDetectorPreferences, Component component) {
		super(dataModel, edgesDetectorPreferences, component);
	}

	void cancelSelection() {
	}

	void acceptSelection() {
	}

	void reset() {
	}

	void resetToDefault() {
	}

	public void handleStateChanged(ChangeEvent e) {
	}

	@Override
	public EdgesDetectorFlavour getEdgesDetectorFlavour() {
		return EdgesDetectorFlavour.SOBEL_EDGES_DETECTOR;
	}

	@Override
	protected EdgesDetector getEdgesDetectorImplementation() {
		return new SobelEdgesDetectorImpl();
	}

	@Override
	boolean isAnyCalculationParameterModified() {
		return false;
	}

}
