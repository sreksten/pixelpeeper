package com.threeamigos.imageviewer.implementations.ui;

import java.awt.Component;
import java.awt.image.BufferedImage;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.threeamigos.imageviewer.interfaces.datamodel.EdgesDetector;
import com.threeamigos.imageviewer.interfaces.preferences.EdgesDetectorFlavour;
import com.threeamigos.imageviewer.interfaces.preferences.EdgesDetectorPreferences;

abstract class AbstractEdgesDetectorPreferencesSelectorDataModel implements EdgesDetectorPreferences, ChangeListener {

	static final int MIN_TRANSPARENCY = 0;
	static final int MAX_TRANSPARENCY = 100;

	protected final EdgesDetectorPreferences edgesDetectorPreferences;
	protected final Component component;

	private BufferedImage sourceImage;
	private BufferedImage edgesImage;

	private int transparency;

	final JLabel transparencyText;

	final JSlider transparencySlider;

	AbstractEdgesDetectorPreferencesSelectorDataModel(EdgesDetectorPreferences edgesDetectorPreferences,
			Component component) {
		this.edgesDetectorPreferences = edgesDetectorPreferences;
		this.component = component;

		transparency = normalizeTransparency(edgesDetectorPreferences.getEdgesTransparency());
		transparencyText = new JLabel(String.valueOf(transparency));
		transparencySlider = createSlider(MIN_TRANSPARENCY, MAX_TRANSPARENCY, transparency);
	}

	final void cancelSelection() {
		edgesDetectorPreferences.setEdgesTransparency(normalizeTransparency(transparency));
		cancelSelectionFlavour();
	}

	abstract void cancelSelectionFlavour();

	final void acceptSelection() {
		edgesDetectorPreferences.setEdgesTransparency(normalizeTransparency(transparencySlider.getValue()));
		acceptSelectionFlavour();
	}

	abstract void acceptSelectionFlavour();

	final void reset() {
		edgesDetectorPreferences.setEdgesTransparency(transparency);

		resetFlavour();
		transparencySlider.setValue(transparency);

		component.repaint();
	}

	abstract void resetFlavour();

	final void resetToDefault() {
		edgesDetectorPreferences.setEdgesTransparency(EdgesDetectorPreferences.EDGES_TRANSPARENCY_DEFAULT);
		transparencySlider.setValue(normalizeTransparency(EdgesDetectorPreferences.EDGES_TRANSPARENCY_DEFAULT));
		resetToDefaultFlavour();
		component.repaint();
	}

	abstract void resetToDefaultFlavour();

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

	private final int normalizeTransparency(int transparency) {
		return MAX_TRANSPARENCY - transparency;
	}

	private final int denormalizeTransparency(int transparency) {
		return normalizeTransparency(transparency);
	}

	public final void stateChanged(ChangeEvent e) {
		Object object = e.getSource();

		if (object == transparencySlider) {
			transparencyText.setText(String.valueOf(transparencySlider.getValue()));
			edgesDetectorPreferences.setEdgesTransparency(denormalizeTransparency(transparencySlider.getValue()));
		} else {
			stateChangedFlavour(e);
		}

		startEdgesCalculation();
		component.repaint();
	}

	protected abstract void stateChangedFlavour(ChangeEvent e);

	@Override
	public final void persist() {
	}

	@Override
	public final void setShowEdges(boolean showEdges) {
	}

	@Override
	public final boolean isShowEdges() {
		return true;
	}

	@Override
	public final void setEdgesDetectorFlavour(EdgesDetectorFlavour flavour) {
	}

	@Override
	public abstract EdgesDetectorFlavour getEdgesDetectorFlavour();

	public final int getEdgesTransparency() {
		return denormalizeTransparency(transparencySlider.getValue());
	}

	public final void setEdgesTransparency(int transparency) {
		transparencySlider.setValue(normalizeTransparency(transparency));
	}

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
}
