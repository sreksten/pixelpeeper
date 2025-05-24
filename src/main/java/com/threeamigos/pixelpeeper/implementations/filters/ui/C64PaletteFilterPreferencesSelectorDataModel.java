package com.threeamigos.pixelpeeper.implementations.filters.ui;

import com.threeamigos.common.util.implementations.messagehandler.SwingMessageHandler;
import com.threeamigos.pixelpeeper.implementations.filters.flavors.C64PaletteFilterImpl;
import com.threeamigos.pixelpeeper.interfaces.datamodel.DataModel;
import com.threeamigos.pixelpeeper.interfaces.filters.Filter;
import com.threeamigos.pixelpeeper.interfaces.filters.FilterFlavor;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavors.C64PaletteFilterPreferences;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavors.FilterPreferences;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import java.awt.*;

public class C64PaletteFilterPreferencesSelectorDataModel extends ColorClashPaletteFilterPreferencesSelectorDataModel
        implements C64PaletteFilterPreferences {

    C64PaletteFilterPreferencesSelectorDataModel(DataModel dataModel,
                                                 FilterPreferences filterPreferences,
                                                 C64PaletteFilterPreferences c64PaletteFilterPreferences,
                                                 Component component) {
        super(dataModel, filterPreferences, c64PaletteFilterPreferences, component);
    }

    @Override
    public FilterFlavor getFilterFlavor() {
        return FilterFlavor.C64_PALETTE;
    }

    @Override
    protected Filter getFilterImplementation() {
        return new C64PaletteFilterImpl(this, new SwingMessageHandler());
    }

    @Override
    public String getDescription() {
        return C64PaletteFilterPreferences.super.getDescription();
    }
}
