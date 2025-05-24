package com.threeamigos.pixelpeeper.implementations.filters.ui;

import com.threeamigos.common.util.implementations.messagehandler.SwingMessageHandler;
import com.threeamigos.pixelpeeper.implementations.filters.flavors.Windows311PaletteFilterImpl;
import com.threeamigos.pixelpeeper.interfaces.datamodel.DataModel;
import com.threeamigos.pixelpeeper.interfaces.filters.Filter;
import com.threeamigos.pixelpeeper.interfaces.filters.FilterFlavor;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavors.FilterPreferences;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavors.Windows311PaletteFilterPreferences;

import java.awt.*;

public class Windows311PaletteFilterPreferencesSelectorDataModel extends PaletteFilterPreferencesSelectorDataModel
        implements Windows311PaletteFilterPreferences {

    Windows311PaletteFilterPreferencesSelectorDataModel(DataModel dataModel,
                                                        FilterPreferences filterPreferences,
                                                        Windows311PaletteFilterPreferences windows311PaletteFilterPreferences,
                                                        Component component) {
        super(dataModel, filterPreferences, windows311PaletteFilterPreferences, component);
    }

    @Override
    public FilterFlavor getFilterFlavor() {
        return FilterFlavor.WINDOWS_3_11_PALETTE;
    }

    @Override
    protected Filter getFilterImplementation() {
        return new Windows311PaletteFilterImpl(this, new SwingMessageHandler());
    }

    @Override
    public String getDescription() {
        return Windows311PaletteFilterPreferences.super.getDescription();
    }
}
