package com.threeamigos.pixelpeeper.implementations.filters.ui;

import com.threeamigos.pixelpeeper.implementations.filters.flavors.RomyJonaFilterImpl;
import com.threeamigos.pixelpeeper.interfaces.datamodel.DataModel;
import com.threeamigos.pixelpeeper.interfaces.filters.Filter;
import com.threeamigos.pixelpeeper.interfaces.filters.FilterFlavor;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavors.FilterPreferences;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavors.RomyJonaFilterPreferences;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import java.awt.*;

public class RomyJonaFilterPreferencesSelectorDataModel extends AbstractFilterPreferencesSelectorDataModel
        implements RomyJonaFilterPreferences {

    private final RomyJonaFilterPreferences romyJonaFilterPreferences;

    private final int puppamentoBackup;
    private final boolean aNastroBackup;

    JLabel puppamentoText;

    JSlider puppamentoSlider;
    JCheckBox aNastroCheckbox;

    RomyJonaFilterPreferencesSelectorDataModel(DataModel dataModel,
                                               FilterPreferences filterPreferences,
                                               RomyJonaFilterPreferences romyJonaFilterPreferences, Component component) {
        super(dataModel, filterPreferences, component);
        this.romyJonaFilterPreferences = romyJonaFilterPreferences;

        puppamentoBackup = romyJonaFilterPreferences.getPuppamento();
        aNastroBackup = romyJonaFilterPreferences.isANastro();

        puppamentoText = new JLabel(String.valueOf(romyJonaFilterPreferences.getPuppamento()));

        puppamentoSlider = createSlider(1, 3, puppamentoBackup);
        aNastroCheckbox = createCheckbox(aNastroBackup);
    }

    void cancelSelection() {
        romyJonaFilterPreferences.setPuppamento(puppamentoBackup);
        romyJonaFilterPreferences.setANastro(aNastroBackup);
    }

    void acceptSelection() {
        romyJonaFilterPreferences.setPuppamento(puppamentoSlider.getValue());
        romyJonaFilterPreferences.setANastro(aNastroCheckbox.isSelected());
    }

    void reset() {
        puppamentoSlider.setValue(puppamentoBackup);
        aNastroCheckbox.setSelected(aNastroBackup);
    }

    void resetToDefault() {
        puppamentoSlider.setValue(RomyJonaFilterPreferences.PUPPAMENTO_PREFERENCES_DEFAULT);
        aNastroCheckbox.setSelected(RomyJonaFilterPreferences.A_NASTRO_PREFERENCES_DEFAULT);
    }

    public void handleStateChanged(ChangeEvent e) {
        Object object = e.getSource();

        if (object == puppamentoSlider) {
            puppamentoText.setText(String.valueOf(puppamentoSlider.getValue()));
        }
    }

    @Override
    public FilterFlavor getFilterFlavor() {
        return FilterFlavor.ROMY_JONA;
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
    protected Filter getFilterImplementation() {
        return new RomyJonaFilterImpl(this);
    }

    @Override
    boolean isAnyCalculationParameterModified() {
        return romyJonaFilterPreferences.getPuppamento() != puppamentoSlider.getValue()
                || romyJonaFilterPreferences.isANastro() != aNastroCheckbox.isSelected();
    }

    @Override
    public String getDescription() {
        return RomyJonaFilterPreferences.super.getDescription();
    }
}
