package com.threeamigos.pixelpeeper;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.JToggleButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.threeamigos.pixelpeeper.data.ExifValue;
import com.threeamigos.pixelpeeper.interfaces.datamodel.CommunicationMessages;
import com.threeamigos.pixelpeeper.interfaces.datamodel.DataModel;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavours.DrawingPreferences;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavours.ImageHandlingPreferences;

public class ControlsPanel extends JPanel implements ChangeListener, PropertyChangeListener {

	private static final long serialVersionUID = 1L;

	private final ImageHandlingPreferences imageHandlingPreferences;
	private final DrawingPreferences drawingPreferences;
	private final DataModel dataModel;

	private JButton previousGroupButton;
	private JLabel groupLabel;
	private JButton nextGroupButton;

	private List<ColoredButton> coloredButtons = new ArrayList<>();
	private JLabel transparencyLabel;
	private JSlider transparencySlider;
	private JLabel brushLabel;
	private JSlider brushSlider;

	private JLabel zoomLabel;
	private JSlider zoomSlider;

	public ControlsPanel(ImageHandlingPreferences imageHandlingPreferences, DrawingPreferences drawingPreferences,
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
		JSeparator separator = new JSeparator(JSeparator.VERTICAL);
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

		brushSlider = new JSlider(JSlider.HORIZONTAL, DrawingPreferences.BRUSH_SIZE_MIN,
				DrawingPreferences.BRUSH_SIZE_MAX, drawingPreferences.getBrushSize());
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

		transparencySlider = new JSlider(JSlider.HORIZONTAL, DrawingPreferences.TRANSPARENCY_MIN,
				DrawingPreferences.TRANSPARENCY_MAX, drawingPreferences.getTransparency());
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
		previousGroupButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dataModel.moveToPreviousGroup();
			}
		});
		controlsBox.add(previousGroupButton);

		controlsBox.add(Box.createHorizontalStrut(5));

		groupLabel = new JLabel("");
		controlsBox.add(groupLabel);

		controlsBox.add(Box.createHorizontalStrut(5));

		nextGroupButton = new JButton(">");
		nextGroupButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dataModel.moveToNextGroup();
			}
		});
		controlsBox.add(nextGroupButton);
	}

	private void createZoomControls(Box controlsBox) {

		zoomLabel = new JLabel();
		buildZoomLabel();
		controlsBox.add(zoomLabel);

		zoomSlider = new JSlider(JSlider.HORIZONTAL, (int) ImageHandlingPreferences.MIN_ZOOM_LEVEL,
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

	private void selectColor(Color color, boolean isSelected) {
		coloredButtons.forEach(b -> b.setSelected(color.equals(b.getColor()) && isSelected));
		drawingPreferences.setColor(color);
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

			addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					selectColor(color, isSelected());
				}
			});
		}

		public Color getColor() {
			return color;
		}

		@Override
		public Dimension getSize() {
			return new Dimension(20, 20);
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
