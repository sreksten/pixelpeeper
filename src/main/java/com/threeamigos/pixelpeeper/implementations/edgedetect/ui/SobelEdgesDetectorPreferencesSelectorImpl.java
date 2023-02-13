package com.threeamigos.pixelpeeper.implementations.edgedetect.ui;

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

import com.threeamigos.common.util.interfaces.ExceptionHandler;
import com.threeamigos.pixelpeeper.interfaces.datamodel.DataModel;
import com.threeamigos.pixelpeeper.interfaces.datamodel.ExifImageReader;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavours.EdgesDetectorPreferences;

public class SobelEdgesDetectorPreferencesSelectorImpl extends AbstractEdgesDetectorPreferencesSelectorImpl {

	private static final String PUPPAMENTO = "Puppamento";
	private static final String A_NASTRO = "A nastro";

	private Dimension flavourDimension;

	public SobelEdgesDetectorPreferencesSelectorImpl(EdgesDetectorPreferences edgesDetectorPreferences,
			DataModel dataModel, ExifImageReader exifImageReader, Component parentComponent,
			ExceptionHandler exceptionHandler) {
		super(edgesDetectorPreferences, dataModel, exifImageReader, parentComponent, exceptionHandler);

		preferencesSelectorDataModel = new SobelEdgesDetectorPreferencesSelectorDataModel(dataModel,
				edgesDetectorPreferences, testImageCanvas);
		preferencesSelectorDataModel.setSourceImage(testImage);
		preferencesSelectorDataModel.startEdgesCalculation();
	}

	String getPreferencesDescription() {
		return "Sobel Edge Detector Preferences";
	}

	JPanel createFlavourPanel(Component component) {

		JPanel flavourPanel = new JPanel();
		flavourPanel.setLayout(new BoxLayout(flavourPanel, BoxLayout.PAGE_AXIS));

		flavourDimension = new Dimension(300, 30);

		return flavourPanel;
	}

	Dimension getFlavourDimension() {
		return flavourDimension;
	}

}
