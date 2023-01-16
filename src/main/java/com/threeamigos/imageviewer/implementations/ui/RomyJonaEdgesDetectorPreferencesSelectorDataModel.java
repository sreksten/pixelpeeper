package com.threeamigos.imageviewer.implementations.ui;

import java.awt.Component;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.threeamigos.imageviewer.implementations.datamodel.RomyJonaEdgesDetectorImpl;
import com.threeamigos.imageviewer.interfaces.datamodel.EdgesDetector;
import com.threeamigos.imageviewer.interfaces.preferences.EdgesDetectorFlavour;
import com.threeamigos.imageviewer.interfaces.preferences.EdgesDetectorPreferences;
import com.threeamigos.imageviewer.interfaces.preferences.RomyJonaEdgesDetectorPreferences;

public class RomyJonaEdgesDetectorPreferencesSelectorDataModel extends AbstractEdgesDetectorPreferencesSelectorDataModel
		implements EdgesDetectorPreferences, RomyJonaEdgesDetectorPreferences, ChangeListener {

	private final RomyJonaEdgesDetectorPreferences romyJonaEdgesDetectorPreferences;

	private int puppamentoBackup;
	private boolean aNastroBackup;

	JLabel puppamentoText;

	JSlider puppamentoSlider;
	JCheckBox aNastroCheckbox;

	RomyJonaEdgesDetectorPreferencesSelectorDataModel(EdgesDetectorPreferences edgesDetectorPreferences,
			RomyJonaEdgesDetectorPreferences romyJonaEdgesDetectorPreferences, Component component) {
		super(edgesDetectorPreferences, component);
		this.romyJonaEdgesDetectorPreferences = romyJonaEdgesDetectorPreferences;

		puppamentoBackup = romyJonaEdgesDetectorPreferences.getPuppamento();
		aNastroBackup = romyJonaEdgesDetectorPreferences.isANastro();

		puppamentoText = new JLabel(String.valueOf(romyJonaEdgesDetectorPreferences.getPuppamento()));

		puppamentoSlider = createSlider(1, 3, puppamentoBackup);
		aNastroCheckbox = createCheckbox(aNastroBackup);
	}

	void cancelSelectionFlavour() {
		romyJonaEdgesDetectorPreferences.setPuppamento(puppamentoBackup);
		romyJonaEdgesDetectorPreferences.setANastro(aNastroBackup);
	}

	void acceptSelectionFlavour() {
		romyJonaEdgesDetectorPreferences.setPuppamento(puppamentoSlider.getValue());
		romyJonaEdgesDetectorPreferences.setANastro(aNastroCheckbox.isSelected());
	}

	void resetFlavour() {
		puppamentoSlider.setValue(puppamentoBackup);
		aNastroCheckbox.setSelected(aNastroBackup);
	}

	void resetToDefaultFlavour() {
		puppamentoSlider.setValue(RomyJonaEdgesDetectorPreferences.PUPPAMENTO_PREFERENCES_DEFAULT);
		aNastroCheckbox.setSelected(RomyJonaEdgesDetectorPreferences.A_NASTRO_PREFERENCES_DEFAULT);
	}

	public void stateChangedFlavour(ChangeEvent e) {
		Object object = e.getSource();

		if (object == puppamentoSlider) {
			puppamentoText.setText(String.valueOf(puppamentoSlider.getValue()));
		}
	}

	@Override
	public EdgesDetectorFlavour getEdgesDetectorFlavour() {
		return EdgesDetectorFlavour.ROMY_JONA_EDGES_DETECTOR;
	}

	@Override
	public int getPuppamento() {
		return puppamentoSlider.getValue();
	}

	@Override
	public void setPuppamento(int puppamento) {
		puppamentoSlider.setValue(puppamento);
	}

	@Override
	public boolean isANastro() {
		return aNastroCheckbox.isSelected();
	}

	@Override
	public void setANastro(boolean aNastro) {
		aNastroCheckbox.setSelected(aNastro);
	}

	@Override
	protected EdgesDetector getEdgesDetectorImplementation() {
		return new RomyJonaEdgesDetectorImpl(this);
	}

}
