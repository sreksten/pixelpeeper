package com.threeamigos.imageviewer;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.threeamigos.imageviewer.interfaces.datamodel.CommunicationMessages;
import com.threeamigos.imageviewer.interfaces.preferences.flavours.ImageHandlingPreferences;

public class ControlsPanel extends JPanel implements ChangeListener, PropertyChangeListener {

	private final ImageHandlingPreferences imageHandlingPreferences;

	private final JLabel zoomLabel;
	private final JSlider zoomSlider;

	public ControlsPanel(ImageHandlingPreferences imageHandlingPreferences) {
		this.imageHandlingPreferences = imageHandlingPreferences;

		setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
		setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		Box controlsBox = Box.createHorizontalBox();
		controlsBox.add(Box.createGlue());

		controlsBox.add(new JSeparator(JSeparator.VERTICAL));

		zoomLabel = new JLabel();
		buildZoomLabel();
		controlsBox.add(zoomLabel);

		zoomSlider = new JSlider(JSlider.HORIZONTAL, imageHandlingPreferences.MIN_ZOOM_LEVEL,
				imageHandlingPreferences.MAX_ZOOM_LEVEL, imageHandlingPreferences.getZoomLevel());
		zoomSlider.setMajorTickSpacing(10);
		zoomSlider.setMinorTickSpacing(5);
		zoomSlider.setPaintTicks(true);
		zoomSlider.addChangeListener(this);
		controlsBox.add(zoomSlider);

		add(controlsBox);
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
		}
	}

}
