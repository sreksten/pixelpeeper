package com.threeamigos.pixelpeeper.implementations.filters.ui;

import com.threeamigos.common.util.implementations.messagehandler.SwingMessageHandler;
import com.threeamigos.pixelpeeper.implementations.filters.flavors.ZXSpectrumPaletteFilterImpl;
import com.threeamigos.pixelpeeper.interfaces.datamodel.DataModel;
import com.threeamigos.pixelpeeper.interfaces.filters.Filter;
import com.threeamigos.pixelpeeper.interfaces.filters.FilterFlavor;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavors.FilterPreferences;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavors.ZXSpectrumPaletteFilterPreferences;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import java.awt.*;

public class ZXSpectrumPaletteFilterPreferencesSelectorDataModel extends ColorClashPaletteFilterPreferencesSelectorDataModel
        implements ZXSpectrumPaletteFilterPreferences {

    ZXSpectrumPaletteFilterPreferencesSelectorDataModel(DataModel dataModel,
                                                        FilterPreferences filterPreferences,
                                                        ZXSpectrumPaletteFilterPreferences zxSpectrumPaletteFilterPreferences,
                                                        Component component) {
        super(dataModel, filterPreferences, zxSpectrumPaletteFilterPreferences, component);
    }

    @Override
    public FilterFlavor getFilterFlavor() {
        return FilterFlavor.ZX_SPECTRUM_PALETTE;
    }

    @Override
    protected Filter getFilterImplementation() {
        return new ZXSpectrumPaletteFilterImpl(this, new SwingMessageHandler());
    }

    @Override
    public String getDescription() {
        return ZXSpectrumPaletteFilterPreferences.super.getDescription();
    }
}
