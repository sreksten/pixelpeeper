package com.threeamigos.pixelpeeper.implementations.filters.ui;

import com.threeamigos.common.util.interfaces.messagehandler.ExceptionHandler;
import com.threeamigos.pixelpeeper.interfaces.datamodel.DataModel;
import com.threeamigos.pixelpeeper.interfaces.datamodel.ExifImageReader;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavors.FilterPreferences;

import javax.swing.*;
import java.util.List;

abstract class ColorClashPaletteFilterPreferencesSelectorImpl extends PaletteFilterPreferencesSelectorImpl {

    private static final String COLOR_CLASH_ENABLED = "Color Clash";

    ColorClashPaletteFilterPreferencesSelectorImpl(FilterPreferences filterPreferences,
                                                   DataModel dataModel, ExifImageReader exifImageReader, ExceptionHandler exceptionHandler) {
        super(filterPreferences, dataModel, exifImageReader, exceptionHandler);
    }

    protected abstract ColorClashPaletteFilterPreferencesSelectorDataModel getFilterPreferencesSelectorDataModel();

    @Override
    protected void addPreComponents(JPanel panel) {
        createCheckboxPanel(panel, COLOR_CLASH_ENABLED, getFilterPreferencesSelectorDataModel().colorClashEnabledCheckbox);
        panel.add(Box.createVerticalStrut(SPACING));
    }

    @Override
    protected List<String> getAllLabels() {
        List<String> allLabels = super.getAllLabels();
        allLabels.add(COLOR_CLASH_ENABLED);
        return allLabels;
    }
}
