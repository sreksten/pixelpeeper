package com.threeamigos.pixelpeeper.implementations.filters.ui;

import com.threeamigos.common.util.interfaces.messagehandler.ExceptionHandler;
import com.threeamigos.common.util.ui.draganddrop.DragAndDropSupportHelper;
import com.threeamigos.pixelpeeper.data.PictureData;
import com.threeamigos.pixelpeeper.implementations.helpers.ImageDrawHelper;
import com.threeamigos.pixelpeeper.interfaces.datamodel.DataModel;
import com.threeamigos.pixelpeeper.interfaces.datamodel.ExifImageReader;
import com.threeamigos.pixelpeeper.interfaces.filters.ui.FilterPreferencesSelector;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavors.FilterPreferences;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Consumer;

abstract class FilterPreferencesSelectorImpl implements FilterPreferencesSelector {

    protected static final int SPACING = 5;

    private static final int SOURCE_IMAGE_CANVAS_SIZE_DEFAULT = 256;

    private static final String OK_OPTION = "OK";
    private static final String CANCEL_OPTION = "Cancel";

    protected static final String TRANSPARENCY = "Transparency";

    final SourceImageCanvas testImageCanvas;

    JDialog dialog;

    protected final FilterPreferences filterPreferences;
    protected BufferedImage testImage;
    private final DataModel dataModel;
    private final ExifImageReader exifImageReader;
    private final ExceptionHandler exceptionHandler;

    private final boolean isShowResultsAtStart;

    protected FilterPreferencesSelectorImpl(FilterPreferences filterPreferences,
                                            DataModel dataModel, ExifImageReader exifImageReader,
                                            ExceptionHandler exceptionHandler) {
        this.filterPreferences = filterPreferences;
        this.dataModel = dataModel;
        this.exifImageReader = exifImageReader;
        this.exceptionHandler = exceptionHandler;

        int width = SOURCE_IMAGE_CANVAS_SIZE_DEFAULT;
        int height = SOURCE_IMAGE_CANVAS_SIZE_DEFAULT;

        try {
            InputStream inputStream = getClass().getResourceAsStream("/testImage.jpg");
            assert inputStream != null;
            testImage = ImageIO.read(inputStream);
            width = testImage.getWidth();
            height = testImage.getHeight();
        } catch (IOException e) {
            exceptionHandler.handleException(e);
        }
        testImageCanvas = new SourceImageCanvas();
        testImageCanvas.setSize(width, height);

        isShowResultsAtStart = filterPreferences.isShowResults();
    }

    protected abstract FilterPreferencesSelectorDataModel getFilterPreferencesSelectorDataModel();

    @Override
    public final void selectParameters(Component parentComponent) {

        FilterPreferencesSelectorDataModel filterPreferencesSelectorDataModel = getFilterPreferencesSelectorDataModel();

        String[] options = {OK_OPTION, CANCEL_OPTION};

        JOptionPane optionPane = new JOptionPane(createPreferencesPanel(parentComponent), -1,
                JOptionPane.OK_CANCEL_OPTION, null, options, options[1]);

        dialog = optionPane.createDialog(parentComponent, getPreferencesDescription());

        dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                filterPreferencesSelectorDataModel.abstractCancelSelection();
                dialog.setVisible(false);
            }
        });

        // In real code, you should invoke this from AWT-EventQueue using
        // invokeAndWait() or something
        dialog.pack();
        dialog.setVisible(true);

        if (CANCEL_OPTION.equals(optionPane.getValue())) {
            filterPreferencesSelectorDataModel.abstractCancelSelection();
            if (isShowResultsAtStart) {
                dataModel.startFilterCalculation();
            }
            filterPreferences.setShowResults(isShowResultsAtStart);
            dataModel.requestRepaint();
        } else if (OK_OPTION.equals(optionPane.getValue())) {
            filterPreferencesSelectorDataModel.abstractAcceptSelection();
            if (filterPreferencesSelectorDataModel.isAnyCalculationParameterModified()) {
                dataModel.startFilterCalculation();
            }
            dataModel.requestRepaint();
        }

        dialog.dispose();
    }

    abstract String getPreferencesDescription();

    private JPanel createPreferencesPanel(Component component) {

        JPanel sliderAndPreviewPanel = new JPanel();
        sliderAndPreviewPanel.setLayout(new BoxLayout(sliderAndPreviewPanel, BoxLayout.LINE_AXIS));

        sliderAndPreviewPanel.add(createSlidersPanel(component));

        sliderAndPreviewPanel.add(Box.createHorizontalStrut(SPACING));

        sliderAndPreviewPanel.add(createPreviewPanel());

        return sliderAndPreviewPanel;
    }

    private JPanel createSlidersPanel(Component component) {

        FilterPreferencesSelectorDataModel filterPreferencesSelectorDataModel = getFilterPreferencesSelectorDataModel();

        Properties transparencySliderLabelTable = new Properties();
        transparencySliderLabelTable.put(FilterPreferences.NO_TRANSPARENCY,
                new JLabel(String.valueOf(FilterPreferences.NO_TRANSPARENCY)));
        transparencySliderLabelTable.put(50, new JLabel("50"));
        transparencySliderLabelTable.put(FilterPreferences.TOTAL_TRANSPARENCY,
                new JLabel(String.valueOf(FilterPreferences.TOTAL_TRANSPARENCY)));

        JPanel slidersPanel = new JPanel();
        slidersPanel.setLayout(new BoxLayout(slidersPanel, BoxLayout.PAGE_AXIS));

        slidersPanel.add(createFlavorPanel(component));

        slidersPanel.add(Box.createVerticalStrut(SPACING));

        createSliderPanel(slidersPanel, getFlavorDimension(), TRANSPARENCY,
                filterPreferencesSelectorDataModel.transparencySlider, transparencySliderLabelTable,
                filterPreferencesSelectorDataModel.transparencyText);

        slidersPanel.add(Box.createVerticalStrut(SPACING));

        createActionsPanel(slidersPanel);

        return slidersPanel;
    }

    abstract Dimension getFlavorDimension();

    abstract JPanel createFlavorPanel(Component component);

    private JPanel createPreviewPanel() {

        FilterPreferencesSelectorDataModel filterPreferencesSelectorDataModel = getFilterPreferencesSelectorDataModel();

        JPanel previewPanel = new JPanel();
        previewPanel.setLayout(new BoxLayout(previewPanel, BoxLayout.PAGE_AXIS));

        previewPanel.add(createImagePanel());

        previewPanel.add(Box.createVerticalStrut(SPACING));

        JButton recalculateButton = new JButton("Apply to images");
        recalculateButton.addActionListener(e -> {
            filterPreferences.setShowResults(true);
            if (filterPreferencesSelectorDataModel.isAnyCalculationParameterModified()) {
                filterPreferencesSelectorDataModel.abstractAcceptSelection();
                dataModel.startFilterCalculation();
            } else {
                dataModel.requestRepaint();
            }
        });

        previewPanel.add(recalculateButton);

        previewPanel.add(Box.createVerticalGlue());

        return previewPanel;
    }

    private JPanel createImagePanel() {

        FilterPreferencesSelectorDataModel filterPreferencesSelectorDataModel = getFilterPreferencesSelectorDataModel();

        JPanel imagePanel = new JPanel();
        imagePanel.setLayout(new BoxLayout(imagePanel, BoxLayout.PAGE_AXIS));
        imagePanel.setBorder(BorderFactory.createTitledBorder("Preview"));
        imagePanel.add(testImageCanvas);
        imagePanel.add(Box.createHorizontalStrut(SPACING));

        JButton loadFromFirstImage = new JButton("Load from first image");
        loadFromFirstImage.addActionListener(e -> {
            if (dataModel.hasLoadedImages()) {
                filterPreferencesSelectorDataModel.setSourceImage(scaleImage(dataModel.getLoadedImages().iterator().next().getImage()));
                filterPreferencesSelectorDataModel.startFilterCalculation();
                dialog.repaint();
            }
        });
        imagePanel.add(loadFromFirstImage);

        return imagePanel;
    }

    protected void createSliderPanel(JPanel parent, Dimension labelDimension, String sliderLabel, JSlider slider,
                                     Properties labelTable, JLabel valueLabel) {

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

    protected void createCheckboxPanel(JPanel parent, String checkboxLabel, JCheckBox checkbox) {

        JPanel checkboxPanel = new JPanel();
        checkboxPanel.setLayout(new BoxLayout(checkboxPanel, BoxLayout.LINE_AXIS));
        checkboxPanel.setBorder(BorderFactory.createTitledBorder(checkboxLabel));

        checkboxPanel.add(checkbox);
        checkboxPanel.add(Box.createHorizontalGlue());

        parent.add(checkboxPanel);
    }

    private void createActionsPanel(JPanel parent) {

        FilterPreferencesSelectorDataModel filterPreferencesSelectorDataModel = getFilterPreferencesSelectorDataModel();

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));

        JButton resetToPrevious = new JButton("Reset to previous");
        resetToPrevious.addActionListener(e -> filterPreferencesSelectorDataModel.abstractReset());

        panel.add(resetToPrevious);

        panel.add(Box.createHorizontalStrut(10));

        JButton resetToDefault = new JButton("Reset to default");
        resetToDefault.addActionListener(e -> filterPreferencesSelectorDataModel.abstractResetToDefault());

        panel.add(resetToDefault);

        parent.add(panel);

    }

    private BufferedImage scaleImage(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        if (width > 256 || height > 256) {
            int minDimension = Math.min(width, height);
            image = image.getSubimage((width - minDimension) / 2, (height - minDimension) / 2, minDimension,
                    minDimension);
            BufferedImage scaledImage = new BufferedImage(SOURCE_IMAGE_CANVAS_SIZE_DEFAULT,
                    SOURCE_IMAGE_CANVAS_SIZE_DEFAULT, image.getType());
            Graphics2D g2d = scaledImage.createGraphics();
            g2d.drawImage(image, 0, 0, SOURCE_IMAGE_CANVAS_SIZE_DEFAULT, SOURCE_IMAGE_CANVAS_SIZE_DEFAULT,
                    null);
            g2d.dispose();
            image = scaledImage;
        }
        return image;
    }

    protected Dimension getMaxDimension(Graphics graphics, List<String> strings) {
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

            Optional<BufferedImage> optImage = files.stream().map(this::loadCropAndResizeImage)
                    .filter(Objects::nonNull).findFirst();
            if (optImage.isPresent()) {
                FilterPreferencesSelectorDataModel filterPreferencesSelectorDataModel = getFilterPreferencesSelectorDataModel();
                filterPreferencesSelectorDataModel.setSourceImage(optImage.get());
                filterPreferencesSelectorDataModel.startFilterCalculation();
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
            FilterPreferencesSelectorDataModel filterPreferencesSelectorDataModel = getFilterPreferencesSelectorDataModel();
            ImageDrawHelper.drawTransparentImageAtop((Graphics2D) g, filterPreferencesSelectorDataModel.getSourceImage(),
                    filterPreferencesSelectorDataModel.getFilteredImage(), 0, 0,
                    filterPreferencesSelectorDataModel.getTransparency());
        }

        private BufferedImage loadCropAndResizeImage(File file) {
            try {
                PictureData pictureData = exifImageReader.readImage(file);
                if (pictureData != null) {
                    return scaleImage(pictureData.getImage());
                }
            } catch (Exception e) {
                exceptionHandler.handleException(e);
            }
            return null;
        }

    }

}
