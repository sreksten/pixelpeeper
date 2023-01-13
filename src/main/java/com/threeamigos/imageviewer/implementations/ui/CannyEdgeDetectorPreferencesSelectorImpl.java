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
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.threeamigos.imageviewer.interfaces.preferences.CannyEdgeDetectorPreferences;
import com.threeamigos.imageviewer.interfaces.preferences.WindowPreferences;
import com.threeamigos.imageviewer.interfaces.ui.CannyEdgeDetectorPreferencesSelector;

public class CannyEdgeDetectorPreferencesSelectorImpl implements CannyEdgeDetectorPreferencesSelector, ChangeListener {

	private static final float NORMALIZATION_VALUE = 10.0f;

	private static final int MIN_TRANSPARENCY = 0;
	private static final int MAX_TRANSPARENCY = 100;

	private static final String LOW_THRESHOLD = "Low threshold";
	private static final String HIGH_THRESHOLD = "High threshold";
	private static final String GAUSSIAN_KERNEL_RADIUS = "Gaussian kernel radius";
	private static final String GAUSSIAN_KERNEL_WIDTH = "Gaussian kernel width";
	private static final String CONTRAST_NORMALIZED = "Contrast normalized";
	private static final String TRANSPARENCY = "Transparency";

	private final WindowPreferences windowPreferences;
	private final CannyEdgeDetectorPreferences cannyEdgeDetectorPreferences;

	private int lowThreshold;
	private int highThreshold;
	private int gaussianKernelRadius;
	private int gaussianKernelWidth;
	private boolean contrastNormalized;
	private int transparency;

	private JLabel lowThresholdText;
	private JLabel highThresholdText;
	private JLabel gaussianKernelRadiusText;
	private JLabel gaussianKernelWidthText;
	private JLabel transparencyText;

	private JSlider lowThresholdSlider;
	private JSlider highThresholdSlider;
	private JSlider gaussianKernelRadiusSlider;
	private JSlider gaussianKernelWidthSlider;
	private JCheckBox contrastNormalizedCheckbox;
	private JSlider transparencySlider;

	private Component parentComponent;

	public CannyEdgeDetectorPreferencesSelectorImpl(WindowPreferences windowPreferences,
			CannyEdgeDetectorPreferences cannyEdgeDetectorPreferences) {
		this.windowPreferences = windowPreferences;
		this.cannyEdgeDetectorPreferences = cannyEdgeDetectorPreferences;
	}

	@Override
	public boolean selectParameters(Component parentComponent) {

		this.parentComponent = parentComponent;

		lowThresholdText = new JLabel(String.valueOf(cannyEdgeDetectorPreferences.getLowThreshold()));
		highThresholdText = new JLabel(String.valueOf(cannyEdgeDetectorPreferences.getHighThreshold()));
		gaussianKernelRadiusText = new JLabel(String.valueOf(cannyEdgeDetectorPreferences.getGaussianKernelRadius()));
		gaussianKernelWidthText = new JLabel(String.valueOf(cannyEdgeDetectorPreferences.getGaussianKernelWidth()));
		transparencyText = new JLabel(String.valueOf(windowPreferences.getEdgeImagesTransparency()));

		lowThreshold = normalize(cannyEdgeDetectorPreferences.getLowThreshold());
		highThreshold = normalize(cannyEdgeDetectorPreferences.getHighThreshold());
		gaussianKernelRadius = normalize(cannyEdgeDetectorPreferences.getGaussianKernelRadius());
		gaussianKernelWidth = cannyEdgeDetectorPreferences.getGaussianKernelWidth();
		contrastNormalized = cannyEdgeDetectorPreferences.isContrastNormalized();
		transparency = normalizeTransparency(windowPreferences.getEdgeImagesTransparency());

		boolean selectionSuccessful = false;

		String[] options = { "OK", "Cancel" };
		JOptionPane jop = new JOptionPane(createPreferencesPanel(parentComponent), JOptionPane.QUESTION_MESSAGE,
				JOptionPane.OK_CANCEL_OPTION, null, options, options[1]);
		JDialog dialog = jop.createDialog(parentComponent, "Canny Edge Detector Preferences");
		dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		dialog.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				windowPreferences.setEdgeImagesTransparency(normalizeTransparency(transparency));
				dialog.setVisible(false);
			}
		});
		// In real code, you should invoke this from AWT-EventQueue using
		// invokeAndWait() or something
		dialog.setVisible(true);
		if ("Cancel".equals(jop.getValue())) {
			windowPreferences.setEdgeImagesTransparency(normalizeTransparency(transparency));
		} else if ("OK".equals(jop.getValue())) {
			cannyEdgeDetectorPreferences.setLowThreshold(denormalize(lowThresholdSlider.getValue()));
			cannyEdgeDetectorPreferences.setHighThreshold(denormalize(highThresholdSlider.getValue()));
			cannyEdgeDetectorPreferences.setGaussianKernelRadius(denormalize(gaussianKernelRadiusSlider.getValue()));
			cannyEdgeDetectorPreferences.setGaussianKernelWidth(gaussianKernelWidthSlider.getValue());
			cannyEdgeDetectorPreferences.setContrastNormalized(contrastNormalizedCheckbox.isSelected());
			windowPreferences.setEdgeImagesTransparency(normalizeTransparency(transparencySlider.getValue()));
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
		transparencySliderLabelTable.put(Integer.valueOf(MIN_TRANSPARENCY),
				new JLabel(String.valueOf(MIN_TRANSPARENCY)));
		transparencySliderLabelTable.put(Integer.valueOf(50), new JLabel("50"));
		transparencySliderLabelTable.put(Integer.valueOf(MAX_TRANSPARENCY),
				new JLabel(String.valueOf(MAX_TRANSPARENCY)));

		Dimension labelDimension = getMaxDimension(component.getGraphics(), LOW_THRESHOLD, HIGH_THRESHOLD,
				GAUSSIAN_KERNEL_RADIUS, GAUSSIAN_KERNEL_WIDTH, CONTRAST_NORMALIZED, TRANSPARENCY);

		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));

		lowThresholdSlider = createSliderPanel(panel, labelDimension, LOW_THRESHOLD, 0, 100, lowThreshold,
				thresholdSliderLabelTable, lowThresholdText);

		panel.add(new JSeparator());

		highThresholdSlider = createSliderPanel(panel, labelDimension, HIGH_THRESHOLD, 0, 100, highThreshold,
				thresholdSliderLabelTable, highThresholdText);

		panel.add(new JSeparator());

		gaussianKernelRadiusSlider = createSliderPanel(panel, labelDimension, GAUSSIAN_KERNEL_RADIUS, 1, 100,
				gaussianKernelRadius, gaussianKernelRadiusSliderLabelTable, gaussianKernelRadiusText);

		panel.add(new JSeparator());

		gaussianKernelWidthSlider = createSliderPanel(panel, labelDimension, GAUSSIAN_KERNEL_WIDTH, 2, 32,
				gaussianKernelWidth, gaussianKernelWidthSliderLabelTable, gaussianKernelWidthText);

		panel.add(new JSeparator());

		contrastNormalizedCheckbox = createCheckboxPanel(panel, labelDimension, CONTRAST_NORMALIZED,
				contrastNormalized);

		panel.add(new JSeparator());

		createActionsPanel(panel);

		panel.add(new JSeparator());

		transparencySlider = createSliderPanel(panel, labelDimension, TRANSPARENCY, MIN_TRANSPARENCY, MAX_TRANSPARENCY,
				transparency, transparencySliderLabelTable, transparencyText);

		return panel;
	}

	private JSlider createSliderPanel(JPanel parent, Dimension labelDimension, String labelString, int minValue,
			int maxValue, int currentValue, Hashtable<Integer, JLabel> labelTable, JLabel valueLabel) {

		JPanel panel = new JPanel();
		panel.setMinimumSize(new Dimension(400, 40));
		panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));

		JPanel labelPanel = new JPanel();
		JLabel label = new JLabel(labelString);
		label.setMinimumSize(labelDimension);
		label.setSize(labelDimension);
		label.setVerticalAlignment(SwingConstants.TOP);
		labelPanel.add(label);
		labelPanel.add(Box.createVerticalGlue());

		panel.add(labelPanel);

		JSlider slider = new JSlider(JSlider.HORIZONTAL, minValue, maxValue, currentValue);
		slider.addChangeListener(this);
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

	private JCheckBox createCheckboxPanel(JPanel parent, Dimension labelDimension, String labelString,
			boolean currentValue) {

		JPanel panel = new JPanel();
		panel.setMinimumSize(new Dimension(400, 40));
		panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));

		JLabel label = new JLabel(labelString);
		label.setMinimumSize(labelDimension);
		label.setSize(labelDimension);
		label.setVerticalAlignment(SwingConstants.TOP);
		panel.add(label);

		JCheckBox checkbox = new JCheckBox();
		checkbox.setSelected(currentValue);
		checkbox.addChangeListener(this);
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
				lowThresholdSlider.setValue(lowThreshold);
				highThresholdSlider.setValue(highThreshold);
				gaussianKernelRadiusSlider.setValue(gaussianKernelRadius);
				gaussianKernelWidthSlider.setValue(gaussianKernelWidth);
				contrastNormalizedCheckbox.setSelected(contrastNormalized);
			}
		});

		panel.add(resetToPrevious);

		panel.add(Box.createHorizontalStrut(10));

		JButton resetToDefault = new JButton("Reset to default");
		resetToDefault.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				lowThresholdSlider.setValue(normalize(CannyEdgeDetectorPreferences.LOW_THRESHOLD_PREFERENCES_DEFAULT));
				highThresholdSlider
						.setValue(normalize(CannyEdgeDetectorPreferences.HIGH_THRESHOLD_PREFERENCES_DEFAULT));
				gaussianKernelRadiusSlider
						.setValue(normalize(CannyEdgeDetectorPreferences.GAUSSIAN_KERNEL_RADIUS_DEFAULT));
				gaussianKernelWidthSlider.setValue(CannyEdgeDetectorPreferences.GAUSSIAN_KERNEL_WIDTH_DEFAULT);
				contrastNormalizedCheckbox.setSelected(CannyEdgeDetectorPreferences.CONTRAST_NORMALIZED_DEFAULT);
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

	@Override
	public void stateChanged(ChangeEvent e) {
		Object object = e.getSource();
		if (object == lowThresholdSlider) {
			lowThresholdText.setText(String.valueOf(denormalize(lowThresholdSlider.getValue())));
			if (lowThresholdSlider.getValue() > highThresholdSlider.getValue()) {
				highThresholdSlider.setValue(lowThresholdSlider.getValue());
				highThresholdText.setText(String.valueOf(denormalize(highThresholdSlider.getValue())));
			}
		} else if (object == highThresholdSlider) {
			highThresholdText.setText(String.valueOf(denormalize(highThresholdSlider.getValue())));
			if (highThresholdSlider.getValue() < lowThresholdSlider.getValue()) {
				lowThresholdSlider.setValue(highThresholdSlider.getValue());
				lowThresholdText.setText(String.valueOf(denormalize(lowThresholdSlider.getValue())));
			}
		} else if (object == gaussianKernelRadiusSlider) {
			gaussianKernelRadiusText.setText(String.valueOf(denormalize(gaussianKernelRadiusSlider.getValue())));
		} else if (object == gaussianKernelWidthSlider) {
			gaussianKernelWidthText.setText(String.valueOf(gaussianKernelWidthSlider.getValue()));
		} else if (object == transparencySlider) {
			transparencyText.setText(String.valueOf(normalizeTransparency(transparencySlider.getValue())));
			windowPreferences.setEdgeImagesTransparency(normalizeTransparency(transparencySlider.getValue()));
			if (parentComponent != null) {
				parentComponent.repaint();
			}
		}
	}

	private int normalize(float value) {
		return (int) (value * NORMALIZATION_VALUE);
	}

	private float denormalize(int value) {
		return value / NORMALIZATION_VALUE;
	}

	private int normalizeTransparency(int transparency) {
		return MAX_TRANSPARENCY - transparency;
	}

}
