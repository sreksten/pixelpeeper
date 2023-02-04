package com.threeamigos.imageviewer;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Optional;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.threeamigos.imageviewer.data.ExifValue;
import com.threeamigos.imageviewer.interfaces.datamodel.CommunicationMessages;
import com.threeamigos.imageviewer.interfaces.datamodel.DataModel;
import com.threeamigos.imageviewer.interfaces.preferences.flavours.ImageHandlingPreferences;

public class ControlsPanel extends JPanel implements ChangeListener, PropertyChangeListener {

	private final ImageHandlingPreferences imageHandlingPreferences;
	private final DataModel dataModel;

	private JButton previousGroupButton;
	private JLabel groupLabel;
	private JButton nextGroupButton;

	private JLabel zoomLabel;
	private JSlider zoomSlider;

	public ControlsPanel(ImageHandlingPreferences imageHandlingPreferences, DataModel dataModel) {
		this.imageHandlingPreferences = imageHandlingPreferences;
		this.dataModel = dataModel;

		setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
		setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		Box controlsBox = Box.createHorizontalBox();
		controlsBox.add(Box.createGlue());

		createGroupControls(controlsBox);

		controlsBox.add(Box.createHorizontalStrut(5));

		JSeparator separator = new JSeparator(JSeparator.VERTICAL);
		separator.setMaximumSize(new Dimension(5, 20));

		controlsBox.add(separator);

		controlsBox.add(Box.createHorizontalStrut(5));

		createZoomControls(controlsBox);

		add(controlsBox);
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

		zoomSlider = new JSlider(JSlider.HORIZONTAL, ImageHandlingPreferences.MIN_ZOOM_LEVEL,
				ImageHandlingPreferences.MAX_ZOOM_LEVEL, imageHandlingPreferences.getZoomLevel());
		zoomSlider.setMajorTickSpacing(10);
		zoomSlider.setMinorTickSpacing(5);
		zoomSlider.setPaintTicks(true);
		zoomSlider.addChangeListener(this);
		zoomSlider.setMaximumSize(new Dimension(600, 20));
		zoomSlider.setPreferredSize(new Dimension(600, 20));
		controlsBox.add(zoomSlider);
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		if (e.getSource() == zoomSlider) {
			imageHandlingPreferences.setZoomLevel(zoomSlider.getValue());
			buildZoomLabel();
		}
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

}
