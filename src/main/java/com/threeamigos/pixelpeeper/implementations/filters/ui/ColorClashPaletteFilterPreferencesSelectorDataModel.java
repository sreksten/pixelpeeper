package com.threeamigos.pixelpeeper.implementations.filters.ui;

import com.threeamigos.pixelpeeper.interfaces.datamodel.DataModel;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavors.C64PaletteFilterPreferences;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavors.ColorClashPaletteFilterPreferences;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavors.FilterPreferences;

import javax.swing.*;
import java.awt.*;

abstract class ColorClashPaletteFilterPreferencesSelectorDataModel extends PaletteFilterPreferencesSelectorDataModel
        implements ColorClashPaletteFilterPreferences {

    private final ColorClashPaletteFilterPreferences colorClashPaletteFilterPreferences;

    private final boolean isColorClashEnabledBackup;

    JCheckBox colorClashEnabledCheckbox;

    ColorClashPaletteFilterPreferencesSelectorDataModel(DataModel dataModel,
                                                        FilterPreferences filterPreferences,
                                                        ColorClashPaletteFilterPreferences colorClashPaletteFilterPreferences,
                                                        Component component) {
        super(dataModel, filterPreferences, colorClashPaletteFilterPreferences, component);
        this.colorClashPaletteFilterPreferences = colorClashPaletteFilterPreferences;

        isColorClashEnabledBackup = this.colorClashPaletteFilterPreferences.isColorClashEnabled();

        colorClashEnabledCheckbox = createCheckbox(isColorClashEnabledBackup);
    }

    void cancelSelection() {
        colorClashPaletteFilterPreferences.setColorClashEnabled(isColorClashEnabledBackup);
        super.cancelSelection();
    }

    void acceptSelection() {
        colorClashPaletteFilterPreferences.setColorClashEnabled(colorClashEnabledCheckbox.isSelected());
        super.acceptSelection();
    }

    void reset() {
        colorClashEnabledCheckbox.setSelected(isColorClashEnabledBackup);
        super.reset();
    }

    void resetToDefault() {
        colorClashEnabledCheckbox.setSelected(C64PaletteFilterPreferences.COLOR_CLASH_ENABLED_DEFAULT);
        super.resetToDefault();
    }

    @Override
    public boolean isColorClashEnabled() {
        return colorClashEnabledCheckbox.isSelected();
    }

    @Override
    public void setColorClashEnabled(boolean colorClashEnabled) {
        colorClashEnabledCheckbox.setSelected(colorClashEnabled);
    }

    @Override
    boolean isAnyCalculationParameterModified() {
        return colorClashPaletteFilterPreferences.isColorClashEnabled() != colorClashEnabledCheckbox.isSelected()
                || super.isAnyCalculationParameterModified();
    }
}
