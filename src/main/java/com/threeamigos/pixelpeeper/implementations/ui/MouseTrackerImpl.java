package com.threeamigos.pixelpeeper.implementations.ui;

import java.awt.event.MouseEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import com.threeamigos.common.util.interfaces.ui.InputConsumer;
import com.threeamigos.common.util.interfaces.ui.MouseTracker;
import com.threeamigos.pixelpeeper.interfaces.datamodel.CommunicationMessages;

public class MouseTrackerImpl implements MouseTracker {

	private final PropertyChangeSupport propertyChangeSupport;

	private MouseEvent oldEvent;

	public MouseTrackerImpl() {
		propertyChangeSupport = new PropertyChangeSupport(this);
	}

	public InputConsumer getInputConsumer() {
		return new InputAdapter() {

			@Override
			public void mousePressed(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON1) {
					propertyChangeSupport.firePropertyChange(CommunicationMessages.MOUSE_PRESSED, null, e);
					oldEvent = e;
				}
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON1) {
					propertyChangeSupport.firePropertyChange(CommunicationMessages.MOUSE_RELEASED, null, null);
					oldEvent = null;
				}
			}

			@Override
			public void mouseDragged(MouseEvent e) {
				if (oldEvent == null) {
					oldEvent = e;
				}
				propertyChangeSupport.firePropertyChange(CommunicationMessages.MOUSE_DRAGGED, oldEvent, e);
				oldEvent = e;
			}
		};
	}

	@Override
	public void addPropertyChangeListener(PropertyChangeListener pcl) {
		propertyChangeSupport.addPropertyChangeListener(pcl);
	}

	@Override
	public void removePropertyChangeListener(PropertyChangeListener pcl) {
		propertyChangeSupport.removePropertyChangeListener(pcl);
	}

}
