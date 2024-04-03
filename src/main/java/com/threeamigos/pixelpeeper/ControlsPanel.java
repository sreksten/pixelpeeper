package com.threeamigos.pixelpeeper;

import com.threeamigos.pixelpeeper.data.ExifValue;
import com.threeamigos.pixelpeeper.interfaces.datamodel.CommunicationMessages;
import com.threeamigos.pixelpeeper.interfaces.datamodel.DataModel;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavours.DoodlingPreferences;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavours.ImageHandlingPreferences;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * A control panel with zoom and doodling controls
 *
 * @author Stefano Reksten
 */
public class ControlsPanel extends JPanel implements ChangeListener, PropertyChangeListener {

    private static final long serialVersionUID = 1L;

    private final transient ImageHandlingPreferences imageHandlingPreferences;
    private final transient DoodlingPreferences drawingPreferences;
    private final transient DataModel dataModel;

    private JButton previousGroupButton;
    private JLabel groupLabel;
    private JButton nextGroupButton;

    private final List<ColoredButton> coloredButtons = new ArrayList<>();
    private JLabel transparencyLabel;
    private JSlider transparencySlider;
    private JLabel brushLabel;
    private JSlider brushSlider;

    private JLabel zoomLabel;
    private JSlider zoomSlider;

    public ControlsPanel(ImageHandlingPreferences imageHandlingPreferences, DoodlingPreferences drawingPreferences,
                         DataModel dataModel) {
        this.imageHandlingPreferences = imageHandlingPreferences;
        this.drawingPreferences = drawingPreferences;
        this.dataModel = dataModel;

        setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        Box controlsBox = Box.createHorizontalBox();
        controlsBox.add(Box.createGlue());

        createDrawingControls(controlsBox);

        createSeparator(controlsBox);

        createGroupControls(controlsBox);

        createSeparator(controlsBox);

        createZoomControls(controlsBox);

        add(controlsBox);
    }

    private void createSeparator(Box box) {
        box.add(Box.createHorizontalStrut(5));
        JSeparator separator = new JSeparator(SwingConstants.VERTICAL);
        separator.setMaximumSize(new Dimension(5, 20));
        box.add(separator);
        box.add(Box.createHorizontalStrut(5));
    }

    private void createDrawingControls(Box controlsBox) {

        controlsBox.add(createColoredButton(Color.RED));

        controlsBox.add(Box.createHorizontalStrut(5));

        controlsBox.add(createColoredButton(Color.YELLOW));

        controlsBox.add(Box.createHorizontalStrut(5));

        controlsBox.add(createColoredButton(Color.GREEN));

        controlsBox.add(Box.createHorizontalStrut(5));

        controlsBox.add(createColoredButton(Color.ORANGE));

        controlsBox.add(Box.createHorizontalStrut(5));

        brushLabel = new JLabel();
        buildBrushLabel();
        controlsBox.add(brushLabel);

        brushSlider = new JSlider(SwingConstants.HORIZONTAL, DoodlingPreferences.BRUSH_SIZE_MIN,
                DoodlingPreferences.BRUSH_SIZE_MAX, drawingPreferences.getBrushSize());
        brushSlider.setMajorTickSpacing(10);
        brushSlider.setMinorTickSpacing(5);
        brushSlider.setPaintTicks(true);
        brushSlider.addChangeListener(this);
        brushSlider.setMaximumSize(new Dimension(100, 20));
        brushSlider.setPreferredSize(new Dimension(100, 20));
        controlsBox.add(brushSlider);

        transparencyLabel = new JLabel();
        buildTransparencyLabel();
        controlsBox.add(transparencyLabel);

        transparencySlider = new JSlider(SwingConstants.HORIZONTAL, DoodlingPreferences.TRANSPARENCY_MIN,
                DoodlingPreferences.TRANSPARENCY_MAX, drawingPreferences.getTransparency());
        transparencySlider.setMajorTickSpacing(10);
        transparencySlider.setMinorTickSpacing(5);
        transparencySlider.setPaintTicks(true);
        transparencySlider.addChangeListener(this);
        transparencySlider.setMaximumSize(new Dimension(100, 20));
        transparencySlider.setPreferredSize(new Dimension(100, 20));
        controlsBox.add(transparencySlider);

        controlsBox.add(Box.createHorizontalStrut(5));

    }

    private void createGroupControls(Box controlsBox) {
        previousGroupButton = new JButton("<");
        previousGroupButton.addActionListener(e -> dataModel.moveToPreviousGroup());
        controlsBox.add(previousGroupButton);

        controlsBox.add(Box.createHorizontalStrut(5));

        groupLabel = new JLabel("");
        controlsBox.add(groupLabel);

        controlsBox.add(Box.createHorizontalStrut(5));

        nextGroupButton = new JButton(">");
        nextGroupButton.addActionListener(e -> dataModel.moveToNextGroup());
        controlsBox.add(nextGroupButton);
    }

    private void createZoomControls(Box controlsBox) {

        zoomLabel = new JLabel();
        buildZoomLabel();
        controlsBox.add(zoomLabel);

        zoomSlider = new JSlider(SwingConstants.HORIZONTAL, (int) ImageHandlingPreferences.MIN_ZOOM_LEVEL,
                (int) ImageHandlingPreferences.MAX_ZOOM_LEVEL, imageHandlingPreferences.getZoomLevel());
        zoomSlider.setMajorTickSpacing(10);
        zoomSlider.setMinorTickSpacing(5);
        zoomSlider.setPaintTicks(true);
        zoomSlider.addChangeListener(this);
        zoomSlider.setMaximumSize(new Dimension(400, 20));
        zoomSlider.setPreferredSize(new Dimension(400, 20));
        controlsBox.add(zoomSlider);
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        if (e.getSource() == zoomSlider) {
            imageHandlingPreferences.setZoomLevel(zoomSlider.getValue());
            buildZoomLabel();
        } else if (e.getSource() == transparencySlider) {
            drawingPreferences.setTransparency(transparencySlider.getValue());
            buildTransparencyLabel();
        } else if (e.getSource() == brushSlider) {
            drawingPreferences.setBrushSize(brushSlider.getValue());
            buildBrushLabel();
        }
    }

    private void buildTransparencyLabel() {
        transparencyLabel.setText(String.format("Transparency %d", drawingPreferences.getTransparency()));
    }

    private void buildBrushLabel() {
        brushLabel.setText(String.format("Brush size %d", drawingPreferences.getBrushSize()));
    }

    private void buildZoomLabel() {
        zoomLabel.setText(String.format("Zoom %d", imageHandlingPreferences.getZoomLevel()));
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (CommunicationMessages.ZOOM_LEVEL_CHANGED.equals(evt.getPropertyName())) {
            zoomSlider.setValue(imageHandlingPreferences.getZoomLevel());
        } else if (CommunicationMessages.DATA_MODEL_CHANGED.equals(evt.getPropertyName())) {
            Optional<ExifValue> exifValueOpt = dataModel.getCurrentExifValue();
            if (exifValueOpt.isPresent()) {
                ExifValue exifValue = exifValueOpt.get();
                previousGroupButton.setEnabled(true);
                groupLabel.setText(
                        "Grouping by " + exifValue.getExifTag().getDescription() + " - " + exifValue.getDescription()
                                + " (" + (dataModel.getCurrentGroup() + 1) + " of " + dataModel.getGroupsCount() + ")");
                nextGroupButton.setEnabled(true);
            } else {
                previousGroupButton.setEnabled(false);
                groupLabel.setText("Grouping not active");
                nextGroupButton.setEnabled(false);
            }
        }
    }

    private ColoredButton createColoredButton(Color color) {
        ColoredButton button = new ColoredButton(color);
        coloredButtons.add(button);
        button.setSelected(color.equals(drawingPreferences.getColor()));
        return button;
    }

    private class ColoredButton extends JToggleButton {

        private static final long serialVersionUID = 1L;

        private final Color color;
        private final int radius;

        ColoredButton(Color color) {
            super(new Icon() {
                @Override
                public void paintIcon(Component c, Graphics g, int x, int y) {
                    // Icon is actually empty
                }

                @Override
                public int getIconWidth() {
                    return 10;
                }

                @Override
                public int getIconHeight() {
                    return 10;
                }
            });
            setContentAreaFilled(false);
            this.color = color;
            this.radius = 2;
            setBackground(color);

            addActionListener(e -> selectColor(color, isSelected()));
        }

        public Color getColor() {
            return color;
        }

        @Override
        public Dimension getSize() {
            return new Dimension(20, 20);
        }

        private void selectColor(Color color, boolean isSelected) {
            coloredButtons.forEach(b -> b.setSelected(color.equals(b.getColor()) && isSelected));
            drawingPreferences.setColor(color);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (isSelected()) {
                g2d.setColor(Color.LIGHT_GRAY);
            } else {
                g2d.setColor(Color.BLACK);
            }
            g2d.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
            g2d.setColor(color);
            g2d.fillRoundRect(2, 2, getWidth() - 4, getHeight() - 4, radius, radius);
        }
    }

}
