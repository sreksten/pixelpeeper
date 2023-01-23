package com.threeamigos.imageviewer.implementations.preferences.flavours;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import com.threeamigos.imageviewer.interfaces.datamodel.CommunicationMessages;
import com.threeamigos.imageviewer.interfaces.preferences.flavours.BigPointerPreferences;
import com.threeamigos.imageviewer.interfaces.preferences.flavours.PropertyChangeAwareBigPointerPreferences;

public class BigPointerPreferencesImpl implements PropertyChangeAwareBigPointerPreferences {

	private boolean bigPointerVisible;
	private int bigPointerSize;
	private Rotation bigPointerRotation;

	// transient to make Gson serializer ignore this
	private final transient PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

	@Override
	public void setBigPointerVisible(boolean bigPointerVisible) {
		this.bigPointerVisible = bigPointerVisible;
		propertyChangeSupport.firePropertyChange(CommunicationMessages.BIG_POINTER_PREFERENCES_CHANGED, null, null);
	}

	@Override
	public boolean isBigPointerVisible() {
		return bigPointerVisible;
	}

	@Override
	public void setBigPointerSize(int bigPointerSize) {
		this.bigPointerSize = bigPointerSize;
		propertyChangeSupport.firePropertyChange(CommunicationMessages.BIG_POINTER_PREFERENCES_CHANGED, null, null);
	}

	@Override
	public int getBigPointerSize() {
		return bigPointerSize;
	}

	@Override
	public void setBigPointerRotation(Rotation rotation) {
		this.bigPointerRotation = rotation;
		propertyChangeSupport.firePropertyChange(CommunicationMessages.BIG_POINTER_PREFERENCES_CHANGED, null, null);
	}

	@Override
	public Rotation getBigPointerRotation() {
		return bigPointerRotation;
	}

	@Override
	public void loadDefaultValues() {
		bigPointerVisible = BigPointerPreferences.BIG_POINTER_VISIBLE_DEFAULT;
		bigPointerSize = BigPointerPreferences.BIG_POINTER_SIZE_DEFAULT;
		bigPointerRotation = BigPointerPreferences.POINTER_ROTATION_DEFAULT;
	}

	@Override
	public void validate() {
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
