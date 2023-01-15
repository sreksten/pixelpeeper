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
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;

import com.threeamigos.common.util.interfaces.ExceptionHandler;
import com.threeamigos.common.util.ui.draganddrop.DragAndDropSupportHelper;
import com.threeamigos.imageviewer.data.PictureData;
import com.threeamigos.imageviewer.implementations.helpers.ImageDrawHelper;
import com.threeamigos.imageviewer.interfaces.datamodel.DataModel;
import com.threeamigos.imageviewer.interfaces.datamodel.ExifImageReader;
import com.threeamigos.imageviewer.interfaces.preferences.CannyEdgeDetectorPreferences;
import com.threeamigos.imageviewer.interfaces.ui.CannyEdgeDetectorPreferencesSelector;

public class CannyEdgeDetectorPreferencesSelectorImpl implements CannyEdgeDetectorPreferencesSelector {

	private static final int SPACING = 5;

	private static final int SOURCE_IMAGE_CANVAS_SIZE_DEFAULT = 256;

	private static final String OK_OPTION = "OK";
	private static final String CANCEL_OPTION = "Cancel";

	private static final String LOW_THRESHOLD = "Low threshold";
	private static final String HIGH_THRESHOLD = "High threshold";
	private static final String GAUSSIAN_KERNEL_RADIUS = "Gaussian kernel radius";
	private static final String GAUSSIAN_KERNEL_WIDTH = "Gaussian kernel width";
	private static final String CONTRAST_NORMALIZED = "Contrast normalized";
	private static final String TRANSPARENCY = "Transparency";

	SourceImageCanvas testImageCanvas;

	private final DataModel dataModel;
	private final ExifImageReader exifImageReader;
	private final ExceptionHandler exceptionHandler;
	private final CannyEdgeDetectorPreferencesSelectorDataModel preferencesSelectorDataModel;

	public CannyEdgeDetectorPreferencesSelectorImpl(CannyEdgeDetectorPreferences cannyEdgeDetectorPreferences,
			DataModel dataModel, ExifImageReader exifImageReader, Component parentComponent,
			ExceptionHandler exceptionHandler) {
		this.dataModel = dataModel;
		this.exifImageReader = exifImageReader;
		this.exceptionHandler = exceptionHandler;

		BufferedImage testImage = null;
		int width = SOURCE_IMAGE_CANVAS_SIZE_DEFAULT;
		int height = SOURCE_IMAGE_CANVAS_SIZE_DEFAULT;

		try {
			InputStream inputStream = getClass().getResourceAsStream("/testImage.jpg");
			testImage = ImageIO.read(inputStream);
			width = testImage.getWidth();
			height = testImage.getHeight();
		} catch (IOException e) {
			exceptionHandler.handleException(e);
		}

		testImageCanvas = new SourceImageCanvas();
		testImageCanvas.setSize(width, height);

		preferencesSelectorDataModel = new CannyEdgeDetectorPreferencesSelectorDataModel(cannyEdgeDetectorPreferences,
				testImageCanvas);
		preferencesSelectorDataModel.setTestImage(testImage);
		preferencesSelectorDataModel.startEdgesCalculation();
	}

	@Override
	public boolean selectParameters(Component parentComponent) {

		boolean selectionSuccessful = false;

		String[] options = { OK_OPTION, CANCEL_OPTION };

		JOptionPane optionPane = new JOptionPane(createPreferencesPanel(parentComponent), -1,
				JOptionPane.OK_CANCEL_OPTION, null, options, options[1]);

		JDialog dialog = optionPane.createDialog(parentComponent, "Canny Edge Detector Preferences");

		dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		dialog.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				preferencesSelectorDataModel.cancelSelection();
				dialog.setVisible(false);
			}
		});

		// In real code, you should invoke this from AWT-EventQueue using
		// invokeAndWait() or something
		dialog.pack();
		dialog.setVisible(true);

		if (CANCEL_OPTION.equals(optionPane.getValue())) {
			preferencesSelectorDataModel.cancelSelection();
		} else if (OK_OPTION.equals(optionPane.getValue())) {
			preferencesSelectorDataModel.acceptSelection();
			selectionSuccessful = true;
		}

		dialog.dispose();

		if (parentComponent != null) {
			parentComponent.repaint();
		}

		return selectionSuccessful;

	}

	private JPanel createPreferencesPanel(Component component) {

		JPanel sliderAndPreviewPanel = new JPanel();
		sliderAndPreviewPanel.setLayout(new BoxLayout(sliderAndPreviewPanel, BoxLayout.LINE_AXIS));

		sliderAndPreviewPanel.add(createSlidersPanel(component));

		sliderAndPreviewPanel.add(Box.createHorizontalStrut(SPACING));

		sliderAndPreviewPanel.add(createPreviewPanel());

		return sliderAndPreviewPanel;
	}

	private JPanel createSlidersPanel(Component component) {

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

		JPanel slidersPanel = new JPanel();
		slidersPanel.setLayout(new BoxLayout(slidersPanel, BoxLayout.PAGE_AXIS));

		createSliderPanel(slidersPanel, labelDimension, LOW_THRESHOLD, preferencesSelectorDataModel.lowThresholdSlider,
				thresholdSliderLabelTable, preferencesSelectorDataModel.lowThresholdText);

		slidersPanel.add(Box.createVerticalStrut(SPACING));

		createSliderPanel(slidersPanel, labelDimension, HIGH_THRESHOLD,
				preferencesSelectorDataModel.highThresholdSlider, thresholdSliderLabelTable,
				preferencesSelectorDataModel.highThresholdText);

		slidersPanel.add(Box.createVerticalStrut(SPACING));

		createSliderPanel(slidersPanel, labelDimension, GAUSSIAN_KERNEL_RADIUS,
				preferencesSelectorDataModel.gaussianKernelRadiusSlider, gaussianKernelRadiusSliderLabelTable,
				preferencesSelectorDataModel.gaussianKernelRadiusText);

		slidersPanel.add(Box.createVerticalStrut(SPACING));

		createSliderPanel(slidersPanel, labelDimension, GAUSSIAN_KERNEL_WIDTH,
				preferencesSelectorDataModel.gaussianKernelWidthSlider, gaussianKernelWidthSliderLabelTable,
				preferencesSelectorDataModel.gaussianKernelWidthText);

		slidersPanel.add(Box.createVerticalStrut(SPACING));

		createCheckboxPanel(slidersPanel, labelDimension, CONTRAST_NORMALIZED,
				preferencesSelectorDataModel.contrastNormalizedCheckbox);

		slidersPanel.add(Box.createVerticalStrut(SPACING));

		createSliderPanel(slidersPanel, labelDimension, TRANSPARENCY, preferencesSelectorDataModel.transparencySlider,
				transparencySliderLabelTable, preferencesSelectorDataModel.transparencyText);

		slidersPanel.add(Box.createVerticalStrut(SPACING));

		createActionsPanel(slidersPanel);

		return slidersPanel;
	}

	private JPanel createPreviewPanel() {

		JPanel previewPanel = new JPanel();
		previewPanel.setLayout(new BoxLayout(previewPanel, BoxLayout.PAGE_AXIS));

		previewPanel.add(createImagePanel());

		previewPanel.add(Box.createVerticalStrut(SPACING));

		JButton recalculateButton = new JButton("Apply to images");
		recalculateButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dataModel.calculateEdges();
			}
		});

		previewPanel.add(recalculateButton);

		previewPanel.add(Box.createVerticalGlue());

		return previewPanel;
	}

	private JPanel createImagePanel() {

		JPanel imagePanel = new JPanel();
		imagePanel.setBorder(BorderFactory.createTitledBorder("Preview"));
		imagePanel.add(testImageCanvas);

		return imagePanel;
	}

	private void createSliderPanel(JPanel parent, Dimension labelDimension, String sliderLabel, JSlider slider,
			Hashtable<Integer, JLabel> labelTable, JLabel valueLabel) {

		JPanel sliderPanel = new JPanel();
		sliderPanel.setLayout(new BoxLayout(sliderPanel, BoxLayout.LINE_AXIS));
		sliderPanel.setBorder(BorderFactory.createTitledBorder(sliderLabel));

		slider.setMajorTickSpacing(10);
		slider.setMinorTickSpacing(5);
		slider.setPaintTicks(true);
		slider.setLabelTable(labelTable);
		slider.setPaintLabels(true);
		sliderPanel.add(slider);

		sliderPanel.add(Box.createHorizontalStrut(SPACING));

		JPanel valuePanel = new JPanel();
		JLabel label = new JLabel(sliderLabel);
		label.setMinimumSize(labelDimension);
		label.setSize(labelDimension);
		label.setVerticalAlignment(SwingConstants.TOP);
		valuePanel.add(valueLabel);
		valuePanel.add(Box.createVerticalGlue());

		sliderPanel.add(valuePanel);

		parent.add(sliderPanel);
	}

	private void createCheckboxPanel(JPanel parent, Dimension labelDimension, String checkboxLabel,
			JCheckBox checkbox) {

		JPanel checkboxPanel = new JPanel();
		checkboxPanel.setLayout(new BoxLayout(checkboxPanel, BoxLayout.LINE_AXIS));
		checkboxPanel.setBorder(BorderFactory.createTitledBorder(checkboxLabel));

		checkboxPanel.add(checkbox);
		checkboxPanel.add(Box.createHorizontalGlue());

		parent.add(checkboxPanel);
	}

	private void createActionsPanel(JPanel parent) {

		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));

		JButton resetToPrevious = new JButton("Reset to previous");
		resetToPrevious.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				preferencesSelectorDataModel.reset();
			}
		});

		panel.add(resetToPrevious);

		panel.add(Box.createHorizontalStrut(10));

		JButton resetToDefault = new JButton("Reset to default");
		resetToDefault.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				preferencesSelectorDataModel.resetToDefault();
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

	private class SourceImageCanvas extends JPanel implements Consumer<List<File>> {

		private static final long serialVersionUID = 1L;

		SourceImageCanvas() {
			DragAndDropSupportHelper.addJavaFileListSupport(this, exceptionHandler);
		}

		@Override
		public void accept(List<File> files) {
			Optional<BufferedImage> optImage = files.stream().map(file -> loadCropAndResizeImage(file))
					.filter(Objects::nonNull).findFirst();
			if (optImage.isPresent()) {
				preferencesSelectorDataModel.setTestImage(optImage.get());
				preferencesSelectorDataModel.startEdgesCalculation();
				repaint();
			}
		}

		@Override
		public Dimension getPreferredSize() {
			return new Dimension(SOURCE_IMAGE_CANVAS_SIZE_DEFAULT, SOURCE_IMAGE_CANVAS_SIZE_DEFAULT);
		}

		@Override
		public void paint(Graphics g) {
			super.paint(g);
			ImageDrawHelper.drawTransparentImageAtop((Graphics2D) g, preferencesSelectorDataModel.getSourceImage(),
					preferencesSelectorDataModel.getEdgesImage(), 0, 0,
					preferencesSelectorDataModel.getEdgesTransparency());
		}

		private BufferedImage loadCropAndResizeImage(File file) {
			try {
				PictureData pictureData = exifImageReader.readImage(file);
				BufferedImage image = pictureData.getImage();
				int width = image.getWidth();
				int height = image.getHeight();
				if (width > 256 || height > 256) {
					int minDimension = width >= height ? height : width;
					image = image.getSubimage((width - minDimension) / 2, (height - minDimension) / 2, minDimension,
							minDimension);
					BufferedImage scaledImage = new BufferedImage(SOURCE_IMAGE_CANVAS_SIZE_DEFAULT,
							SOURCE_IMAGE_CANVAS_SIZE_DEFAULT, image.getType());
					Graphics2D g2d = scaledImage.createGraphics();
					g2d.drawImage(image, 0, 0, SOURCE_IMAGE_CANVAS_SIZE_DEFAULT, SOURCE_IMAGE_CANVAS_SIZE_DEFAULT,
							null);
					image = scaledImage;
					g2d.dispose();
				}
				return image;
			} catch (Exception e) {
				exceptionHandler.handleException(e);
				return null;
			}
		}

	}

}
