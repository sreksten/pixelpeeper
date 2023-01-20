package com.threeamigos.imageviewer.implementations.edgedetect.ui;

import java.awt.Component;
import java.awt.image.BufferedImage;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.threeamigos.imageviewer.interfaces.datamodel.DataModel;
import com.threeamigos.imageviewer.interfaces.edgedetect.EdgesDetector;
import com.threeamigos.imageviewer.interfaces.edgedetect.EdgesDetectorFlavour;
import com.threeamigos.imageviewer.interfaces.preferences.flavours.EdgesDetectorPreferences;

abstract class AbstractEdgesDetectorPreferencesSelectorDataModel implements EdgesDetectorPreferences, ChangeListener {

	static final int MIN_TRANSPARENCY = 0;
	static final int MAX_TRANSPARENCY = 100;

	protected final DataModel dataModel;
	protected final EdgesDetectorPreferences edgesDetectorPreferences;
	protected final Component component;

	private BufferedImage sourceImage;
	private BufferedImage edgesImage;

	private int transparencyBackup;

	final JLabel transparencyText;

	final JSlider transparencySlider;

	AbstractEdgesDetectorPreferencesSelectorDataModel(DataModel dataModel,
			EdgesDetectorPreferences edgesDetectorPreferences, Component component) {
		this.dataModel = dataModel;
		this.edgesDetectorPreferences = edgesDetectorPreferences;
		this.component = component;

		transparencyBackup = edgesDetectorPreferences.getEdgesTransparency();
		transparencyText = new JLabel(String.valueOf(transparencyBackup));
		transparencySlider = createSlider(MIN_TRANSPARENCY, MAX_TRANSPARENCY, transparencyBackup);
	}

	final void abstractCancelSelection() {
		edgesDetectorPreferences.setEdgesTransparency(transparencyBackup);
		cancelSelection();
	}

	abstract void cancelSelection();

	final void abstractAcceptSelection() {
		edgesDetectorPreferences.setEdgesTransparency(transparencySlider.getValue());
		acceptSelection();
	}

	abstract void acceptSelection();

	abstract boolean isAnyCalculationParameterModified();

	final void abstractReset() {
		edgesDetectorPreferences.setEdgesTransparency(transparencyBackup);

		reset();
		transparencySlider.setValue(transparencyBackup);

		component.repaint();
	}

	abstract void reset();

	final void abstractResetToDefault() {
		edgesDetectorPreferences.setEdgesTransparency(EdgesDetectorPreferences.EDGES_TRANSPARENCY_DEFAULT);
		transparencySlider.setValue(EdgesDetectorPreferences.EDGES_TRANSPARENCY_DEFAULT);
		resetToDefault();
		component.repaint();
	}

	abstract void resetToDefault();

	protected final JSlider createSlider(int minValue, int maxValue, int currentValue) {
		JSlider slider = new JSlider(JSlider.HORIZONTAL, minValue, maxValue, currentValue);
		slider.addChangeListener(this);
		return slider;
	}

	protected final JCheckBox createCheckbox(boolean currentValue) {
		JCheckBox checkbox = new JCheckBox();
		checkbox.setSelected(currentValue);
		checkbox.addChangeListener(this);
		return checkbox;
	}

	public final void stateChanged(ChangeEvent e) {
		Object object = e.getSource();

		if (object == transparencySlider) {
			transparencyText.setText(String.valueOf(transparencySlider.getValue()));
			edgesDetectorPreferences.setEdgesTransparency(transparencySlider.getValue());
		} else {
			handleStateChanged(e);
		}

		startEdgesCalculation();
		component.repaint();
	}

	protected abstract void handleStateChanged(ChangeEvent e);

	public final void startEdgesCalculation() {
		if (sourceImage != null) {
			EdgesDetector edgesDetector = getEdgesDetectorImplementation();
			edgesDetector.setSourceImage(sourceImage);
			edgesDetector.process();
			edgesImage = edgesDetector.getEdgesImage();
		}
	}

	protected abstract EdgesDetector getEdgesDetectorImplementation();

	final void setSourceImage(BufferedImage sourceImage) {
		this.sourceImage = sourceImage;
	}

	final BufferedImage getSourceImage() {
		return sourceImage;
	}

	final BufferedImage getEdgesImage() {
		return edgesImage;
	}

	// EdgesDetectorPreferences

	@Override
	public final void setShowEdges(boolean showEdges) {
	}

	@Override
	public final boolean isShowEdges() {
		return true;
	}

	public final void setEdgesTransparency(int transparency) {
		transparencySlider.setValue(transparency);
	}

	public final int getEdgesTransparency() {
		return transparencySlider.getValue();
	}

	@Override
	public final void setEdgesDetectorFlavour(EdgesDetectorFlavour flavour) {
	}

	@Override
	public abstract EdgesDetectorFlavour getEdgesDetectorFlavour();

	@Override
	public void loadDefaultValues() {
		resetToDefault();
	}

}
