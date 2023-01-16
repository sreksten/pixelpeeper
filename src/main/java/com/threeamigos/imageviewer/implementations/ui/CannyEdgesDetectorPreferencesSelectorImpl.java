package com.threeamigos.imageviewer.implementations.ui;

import java.awt.Component;
import java.awt.Dimension;
import java.util.Hashtable;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.threeamigos.common.util.interfaces.ExceptionHandler;
import com.threeamigos.imageviewer.interfaces.datamodel.DataModel;
import com.threeamigos.imageviewer.interfaces.datamodel.ExifImageReader;
import com.threeamigos.imageviewer.interfaces.preferences.CannyEdgesDetectorPreferences;
import com.threeamigos.imageviewer.interfaces.preferences.EdgesDetectorPreferences;

public class CannyEdgesDetectorPreferencesSelectorImpl extends AbstractEdgesDetectorPreferencesSelectorImpl {

	private static final String LOW_THRESHOLD = "Low threshold";
	private static final String HIGH_THRESHOLD = "High threshold";
	private static final String GAUSSIAN_KERNEL_RADIUS = "Gaussian kernel radius";
	private static final String GAUSSIAN_KERNEL_WIDTH = "Gaussian kernel width";
	private static final String CONTRAST_NORMALIZED = "Contrast normalized";

	private Dimension flavourDimension;

	public CannyEdgesDetectorPreferencesSelectorImpl(EdgesDetectorPreferences edgesDetectorPreferences,
			CannyEdgesDetectorPreferences cannyEdgesDetectorPreferences, DataModel dataModel,
			ExifImageReader exifImageReader, Component parentComponent, ExceptionHandler exceptionHandler) {
		super(edgesDetectorPreferences, dataModel, exifImageReader, parentComponent, exceptionHandler);

		preferencesSelectorDataModel = new CannyEdgesDetectorPreferencesSelectorDataModel(edgesDetectorPreferences,
				cannyEdgesDetectorPreferences, testImageCanvas);
		preferencesSelectorDataModel.setSourceImage(testImage);
		preferencesSelectorDataModel.startEdgesCalculation();
	}

	String getPreferencesDescription() {
		return "Canny Edge Detector Preferences";
	}

	JPanel createFlavourPanel(Component component) {

		CannyEdgesDetectorPreferencesSelectorDataModel downcastDatamodel = (CannyEdgesDetectorPreferencesSelectorDataModel) preferencesSelectorDataModel;

		Hashtable<Integer, JLabel> thresholdSliderLabelTable = new Hashtable<>();
		thresholdSliderLabelTable.put(Integer.valueOf(1), new JLabel("0.1"));
		thresholdSliderLabelTable.put(Integer.valueOf(50), new JLabel("5"));
		thresholdSliderLabelTable.put(Integer.valueOf(100), new JLabel("10"));

		Hashtable<Integer, JLabel> gaussianKernelRadiusSliderLabelTable = new Hashtable<>();
		gaussianKernelRadiusSliderLabelTable.put(Integer.valueOf(1), new JLabel("0.1"));
		gaussianKernelRadiusSliderLabelTable.put(Integer.valueOf(50), new JLabel("5"));
		gaussianKernelRadiusSliderLabelTable.put(Integer.valueOf(100), new JLabel("10"));

		Hashtable<Integer, JLabel> gaussianKernelWidthSliderLabelTable = new Hashtable<>();
		gaussianKernelWidthSliderLabelTable.put(Integer.valueOf(2), new JLabel("2"));
		gaussianKernelWidthSliderLabelTable.put(Integer.valueOf(16), new JLabel("16"));
		gaussianKernelWidthSliderLabelTable.put(Integer.valueOf(32), new JLabel("32"));

		flavourDimension = getMaxDimension(component.getGraphics(), LOW_THRESHOLD, HIGH_THRESHOLD,
				GAUSSIAN_KERNEL_RADIUS, GAUSSIAN_KERNEL_WIDTH, CONTRAST_NORMALIZED, TRANSPARENCY);

		JPanel flavourPanel = new JPanel();
		flavourPanel.setLayout(new BoxLayout(flavourPanel, BoxLayout.PAGE_AXIS));

		createSliderPanel(flavourPanel, flavourDimension, LOW_THRESHOLD, downcastDatamodel.lowThresholdSlider,
				thresholdSliderLabelTable, downcastDatamodel.lowThresholdText);

		flavourPanel.add(Box.createVerticalStrut(SPACING));

		createSliderPanel(flavourPanel, flavourDimension, HIGH_THRESHOLD, downcastDatamodel.highThresholdSlider,
				thresholdSliderLabelTable, downcastDatamodel.highThresholdText);

		flavourPanel.add(Box.createVerticalStrut(SPACING));

		createSliderPanel(flavourPanel, flavourDimension, GAUSSIAN_KERNEL_RADIUS,
				downcastDatamodel.gaussianKernelRadiusSlider, gaussianKernelRadiusSliderLabelTable,
				downcastDatamodel.gaussianKernelRadiusText);

		flavourPanel.add(Box.createVerticalStrut(SPACING));

		createSliderPanel(flavourPanel, flavourDimension, GAUSSIAN_KERNEL_WIDTH,
				downcastDatamodel.gaussianKernelWidthSlider, gaussianKernelWidthSliderLabelTable,
				downcastDatamodel.gaussianKernelWidthText);

		flavourPanel.add(Box.createVerticalStrut(SPACING));

		createCheckboxPanel(flavourPanel, flavourDimension, CONTRAST_NORMALIZED,
				downcastDatamodel.contrastNormalizedCheckbox);

		return flavourPanel;
	}

	Dimension getFlavourDimension() {
		return flavourDimension;
	}

}
