package com.threeamigos.pixelpeeper.implementations.edgedetect.ui;

import java.awt.Component;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;

import com.threeamigos.pixelpeeper.implementations.edgedetect.flavours.RomyJonaEdgesDetectorImpl;
import com.threeamigos.pixelpeeper.interfaces.datamodel.DataModel;
import com.threeamigos.pixelpeeper.interfaces.edgedetect.EdgesDetector;
import com.threeamigos.pixelpeeper.interfaces.edgedetect.EdgesDetectorFlavour;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavours.EdgesDetectorPreferences;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavours.RomyJonaEdgesDetectorPreferences;

public class RomyJonaEdgesDetectorPreferencesSelectorDataModel extends AbstractEdgesDetectorPreferencesSelectorDataModel
		implements RomyJonaEdgesDetectorPreferences {

	private final RomyJonaEdgesDetectorPreferences romyJonaEdgesDetectorPreferences;

	private int puppamentoBackup;
	private boolean aNastroBackup;

	JLabel puppamentoText;

	JSlider puppamentoSlider;
	JCheckBox aNastroCheckbox;

	RomyJonaEdgesDetectorPreferencesSelectorDataModel(DataModel dataModel,
			EdgesDetectorPreferences edgesDetectorPreferences,
			RomyJonaEdgesDetectorPreferences romyJonaEdgesDetectorPreferences, Component component) {
		super(dataModel, edgesDetectorPreferences, component);
		this.romyJonaEdgesDetectorPreferences = romyJonaEdgesDetectorPreferences;

		puppamentoBackup = romyJonaEdgesDetectorPreferences.getPuppamento();
		aNastroBackup = romyJonaEdgesDetectorPreferences.isANastro();

		puppamentoText = new JLabel(String.valueOf(romyJonaEdgesDetectorPreferences.getPuppamento()));

		puppamentoSlider = createSlider(1, 3, puppamentoBackup);
		aNastroCheckbox = createCheckbox(aNastroBackup);
	}

	void cancelSelection() {
		romyJonaEdgesDetectorPreferences.setPuppamento(puppamentoBackup);
		romyJonaEdgesDetectorPreferences.setANastro(aNastroBackup);
	}

	void acceptSelection() {
		romyJonaEdgesDetectorPreferences.setPuppamento(puppamentoSlider.getValue());
		romyJonaEdgesDetectorPreferences.setANastro(aNastroCheckbox.isSelected());
	}

	void reset() {
		puppamentoSlider.setValue(puppamentoBackup);
		aNastroCheckbox.setSelected(aNastroBackup);
	}

	void resetToDefault() {
		puppamentoSlider.setValue(RomyJonaEdgesDetectorPreferences.PUPPAMENTO_PREFERENCES_DEFAULT);
		aNastroCheckbox.setSelected(RomyJonaEdgesDetectorPreferences.A_NASTRO_PREFERENCES_DEFAULT);
	}

	public void handleStateChanged(ChangeEvent e) {
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

	@Override
	boolean isAnyCalculationParameterModified() {
		return romyJonaEdgesDetectorPreferences.getPuppamento() != puppamentoSlider.getValue()
				|| romyJonaEdgesDetectorPreferences.isANastro() != aNastroCheckbox.isSelected();
	}

	@Override
	public void validate() {
		// TODO Auto-generated method stub

	}

	@Override
	public String getDescription() {
		return RomyJonaEdgesDetectorPreferences.super.getDescription();
	}

}
