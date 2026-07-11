package com.threeamigos.pixelpeeper.implementations.filters.ui;

import com.threeamigos.common.util.interfaces.messagehandler.ThrowableHandler;
import com.threeamigos.pixelpeeper.interfaces.datamodel.DataModel;
import com.threeamigos.pixelpeeper.interfaces.datamodel.ExifImageReader;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavors.FilterPreferences;

import javax.swing.*;
import java.awt.*;

public class EquivalentExposureFilterPreferencesSelectorImpl extends FilterPreferencesSelectorImpl {

    private Dimension flavorDimension;

    private final EquivalentExposureFilterPreferencesSelectorDataModel filterPreferencesSelectorDataModel;

    public EquivalentExposureFilterPreferencesSelectorImpl(FilterPreferences filterPreferences,
                                                           DataModel dataModel,
                                                           ExifImageReader exifImageReader,
                                                           ThrowableHandler exceptionHandler) {
        super(filterPreferences, dataModel, exifImageReader, exceptionHandler);

        filterPreferencesSelectorDataModel = new EquivalentExposureFilterPreferencesSelectorDataModel(
                dataModel, filterPreferences, testImageCanvas);
        filterPreferencesSelectorDataModel.setSourceImage(testImage);
        filterPreferencesSelectorDataModel.startFilterCalculation();
    }

    @Override
    public EquivalentExposureFilterPreferencesSelectorDataModel getFilterPreferencesSelectorDataModel() {
        return filterPreferencesSelectorDataModel;
    }

    @Override
    String getPreferencesDescription() {
        return "Equivalent Exposure Preferences";
    }

    @Override
    JPanel createFlavorPanel(Component component) {
        JPanel flavorPanel = new JPanel();
        flavorPanel.setLayout(new BoxLayout(flavorPanel, BoxLayout.PAGE_AXIS));

        flavorDimension = new Dimension(300, 30);

        return flavorPanel;
    }

    @Override
    Dimension getFlavorDimension() {
        return flavorDimension;
    }
}
