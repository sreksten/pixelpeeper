package com.threeamigos.pixelpeeper.implementations.edgedetect.ui;

import java.awt.Component;

import javax.swing.event.ChangeEvent;

import com.threeamigos.pixelpeeper.implementations.edgedetect.flavours.SobelEdgesDetectorImpl;
import com.threeamigos.pixelpeeper.interfaces.datamodel.DataModel;
import com.threeamigos.pixelpeeper.interfaces.edgedetect.EdgesDetector;
import com.threeamigos.pixelpeeper.interfaces.edgedetect.EdgesDetectorFlavour;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavours.EdgesDetectorPreferences;

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

	@Override
	public void validate() {
		// TODO Auto-generated method stub

	}

}
