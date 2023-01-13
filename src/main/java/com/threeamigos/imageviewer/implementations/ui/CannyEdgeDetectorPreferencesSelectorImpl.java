package com.threeamigos.imageviewer.implementations.ui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.util.Hashtable;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;

import com.threeamigos.imageviewer.interfaces.preferences.CannyEdgeDetectorPreferences;
import com.threeamigos.imageviewer.interfaces.preferences.WindowPreferences;
import com.threeamigos.imageviewer.interfaces.ui.CannyEdgeDetectorPreferencesSelector;

public class CannyEdgeDetectorPreferencesSelectorImpl implements CannyEdgeDetectorPreferencesSelector {

	private static final String OK_OPTION = "OK";
	private static final String CANCEL_OPTION = "Cancel";

	private static final String LOW_THRESHOLD = "Low threshold";
	private static final String HIGH_THRESHOLD = "High threshold";
	private static final String GAUSSIAN_KERNEL_RADIUS = "Gaussian kernel radius";
	private static final String GAUSSIAN_KERNEL_WIDTH = "Gaussian kernel width";
	private static final String CONTRAST_NORMALIZED = "Contrast normalized";
	private static final String TRANSPARENCY = "Transparency";

	private final CannyEdgeDetectorPreferencesSelectorDataModel dataModel;

	public CannyEdgeDetectorPreferencesSelectorImpl(WindowPreferences windowPreferences,
			CannyEdgeDetectorPreferences cannyEdgeDetectorPreferences, Component parentComponent) {
		dataModel = new CannyEdgeDetectorPreferencesSelectorDataModel(windowPreferences, cannyEdgeDetectorPreferences,
				parentComponent);
	}

	@Override
	public boolean selectParameters(Component parentComponent) {

		boolean selectionSuccessful = false;

		String[] options = { OK_OPTION, CANCEL_OPTION };

		JOptionPane optionPane = new JOptionPane(createPreferencesPanel(parentComponent), JOptionPane.QUESTION_MESSAGE,
				JOptionPane.OK_CANCEL_OPTION, null, options, options[1]);

		JDialog dialog = optionPane.createDialog(parentComponent, "Canny Edge Detector Preferences");

		dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		dialog.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				dataModel.cancelSelection();
				dialog.setVisible(false);
			}
		});

		// In real code, you should invoke this from AWT-EventQueue using
		// invokeAndWait() or something
		dialog.setVisible(true);

		if (CANCEL_OPTION.equals(optionPane.getValue())) {
			dataModel.cancelSelection();
		} else if (OK_OPTION.equals(optionPane.getValue())) {
			dataModel.acceptSelection();
			selectionSuccessful = true;
		}

		dialog.dispose();

		if (parentComponent != null) {
			parentComponent.repaint();
		}

		return selectionSuccessful;

	}

	private JPanel createPreferencesPanel(Component component) {

		Hashtable<Integer, JLabel> thresholdSliderLabelTable = new Hashtable<>();
		thresholdSliderLabelTable.put(Integer.valueOf(1), new JLabel("0"));
		thresholdSliderLabelTable.put(Integer.valueOf(50), new JLabel("5"));
		thresholdSliderLabelTable.put(Integer.valueOf(100), new JLabel("10"));

		Hashtable<Integer, JLabel> gaussianKernelRadiusSliderLabelTable = new Hashtable<>();
		gaussianKernelRadiusSliderLabelTable.put(Integer.valueOf(1), new JLabel("0.1"));
		gaussianKernelRadiusSliderLabelTable.put(Integer.valueOf(20), new JLabel("2"));
		gaussianKernelRadiusSliderLabelTable.put(Integer.valueOf(50), new JLabel("5"));
		gaussianKernelRadiusSliderLabelTable.put(Integer.valueOf(100), new JLabel("10"));

		Hashtable<Integer, JLabel> gaussianKernelWidthSliderLabelTable = new Hashtable<>();
		gaussianKernelWidthSliderLabelTable.put(Integer.valueOf(2), new JLabel("2"));
		gaussianKernelWidthSliderLabelTable.put(Integer.valueOf(16), new JLabel("16"));
		gaussianKernelWidthSliderLabelTable.put(Integer.valueOf(32), new JLabel("32"));

		Hashtable<Integer, JLabel> transparencySliderLabelTable = new Hashtable<>();
		transparencySliderLabelTable.put(
				Integer.valueOf(CannyEdgeDetectorPreferencesSelectorDataModel.MIN_TRANSPARENCY),
				new JLabel(String.valueOf(CannyEdgeDetectorPreferencesSelectorDataModel.MIN_TRANSPARENCY)));
		transparencySliderLabelTable.put(Integer.valueOf(50), new JLabel("50"));
		transparencySliderLabelTable.put(
				Integer.valueOf(CannyEdgeDetectorPreferencesSelectorDataModel.MAX_TRANSPARENCY),
				new JLabel(String.valueOf(CannyEdgeDetectorPreferencesSelectorDataModel.MAX_TRANSPARENCY)));

		Dimension labelDimension = getMaxDimension(component.getGraphics(), LOW_THRESHOLD, HIGH_THRESHOLD,
				GAUSSIAN_KERNEL_RADIUS, GAUSSIAN_KERNEL_WIDTH, CONTRAST_NORMALIZED, TRANSPARENCY);

		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));

		createSliderPanel(panel, labelDimension, LOW_THRESHOLD, dataModel.lowThresholdSlider, thresholdSliderLabelTable,
				dataModel.lowThresholdText);

		panel.add(new JSeparator());

		createSliderPanel(panel, labelDimension, HIGH_THRESHOLD, dataModel.highThresholdSlider,
				thresholdSliderLabelTable, dataModel.highThresholdText);

		panel.add(new JSeparator());

		createSliderPanel(panel, labelDimension, GAUSSIAN_KERNEL_RADIUS, dataModel.gaussianKernelRadiusSlider,
				gaussianKernelRadiusSliderLabelTable, dataModel.gaussianKernelRadiusText);

		panel.add(new JSeparator());

		createSliderPanel(panel, labelDimension, GAUSSIAN_KERNEL_WIDTH, dataModel.gaussianKernelWidthSlider,
				gaussianKernelWidthSliderLabelTable, dataModel.gaussianKernelWidthText);

		panel.add(new JSeparator());

		createCheckboxPanel(panel, labelDimension, CONTRAST_NORMALIZED, dataModel.contrastNormalizedCheckbox);

		panel.add(new JSeparator());

		createActionsPanel(panel);

		panel.add(new JSeparator());

		createSliderPanel(panel, labelDimension, TRANSPARENCY, dataModel.transparencySlider,
				transparencySliderLabelTable, dataModel.transparencyText);

		return panel;
	}

	private JSlider createSliderPanel(JPanel parent, Dimension labelDimension, String sliderLabel, JSlider slider,
			Hashtable<Integer, JLabel> labelTable, JLabel valueLabel) {

		JPanel panel = new JPanel();
		panel.setMinimumSize(new Dimension(400, 40));
		panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));

		JPanel labelPanel = new JPanel();
		JLabel label = new JLabel(sliderLabel);
		label.setMinimumSize(labelDimension);
		label.setSize(labelDimension);
		label.setVerticalAlignment(SwingConstants.TOP);
		labelPanel.add(label);
		labelPanel.add(Box.createVerticalGlue());

		panel.add(labelPanel);

		slider.setMajorTickSpacing(10);
		slider.setMinorTickSpacing(5);
		slider.setPaintTicks(true);
		slider.setLabelTable(labelTable);
		slider.setPaintLabels(true);
		panel.add(slider);

		panel.add(Box.createHorizontalStrut(10));

		panel.add(valueLabel);

		panel.add(Box.createHorizontalGlue());

		parent.add(panel);

		return slider;
	}

	private JCheckBox createCheckboxPanel(JPanel parent, Dimension labelDimension, String checkboxLabel,
			JCheckBox checkbox) {

		JPanel panel = new JPanel();
		panel.setMinimumSize(new Dimension(400, 40));
		panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));

		JLabel label = new JLabel(checkboxLabel);
		label.setMinimumSize(labelDimension);
		label.setSize(labelDimension);
		label.setVerticalAlignment(SwingConstants.TOP);
		panel.add(label);

		panel.add(checkbox);
		panel.add(Box.createHorizontalGlue());

		parent.add(panel);

		return checkbox;
	}

	private void createActionsPanel(JPanel parent) {

		JPanel panel = new JPanel();
		panel.setMinimumSize(new Dimension(400, 40));
		panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));

		JButton test = new JButton("Test");
		test.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
			}
		});

		panel.add(test);

		panel.add(Box.createHorizontalStrut(10));

		JButton resetToPrevious = new JButton("Reset to previous");
		resetToPrevious.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dataModel.reset();
			}
		});

		panel.add(resetToPrevious);

		panel.add(Box.createHorizontalStrut(10));

		JButton resetToDefault = new JButton("Reset to default");
		resetToDefault.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dataModel.resetToDefault();
			}
		});

		panel.add(resetToDefault);

		parent.add(panel);

	}

	private Dimension getMaxDimension(Graphics graphics, String... strings) {
		Graphics2D g2d = (Graphics2D) graphics;
		Font font = g2d.getFont();
		FontRenderContext context = g2d.getFontRenderContext();
		int maxWidth = 0;
		int maxHeight = 0;
		for (String string : strings) {
			Rectangle2D bounds = font.getStringBounds(string, context);
			int width = (int) bounds.getWidth();
			if (width > maxWidth) {
				maxWidth = width;
			}
			int height = (int) bounds.getHeight();
			if (height > maxHeight) {
				maxHeight = height;
			}
		}
		return new Dimension(maxWidth, maxHeight);
	}

}
