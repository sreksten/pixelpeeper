package com.threeamigos.pixelpeeper.implementations.filters.ui;

import com.threeamigos.common.util.interfaces.messagehandler.ExceptionHandler;
import com.threeamigos.pixelpeeper.interfaces.datamodel.DataModel;
import com.threeamigos.pixelpeeper.interfaces.datamodel.ExifImageReader;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavors.FilterPreferences;

import javax.swing.*;
import java.awt.*;

public class SobelFilterPreferencesSelectorImpl extends FilterPreferencesSelectorImpl {

    private Dimension flavorDimension;

    private final SobelEdgesDetectorPreferencesSelectorDataModel filterPreferencesSelectorDataModel;

    public SobelFilterPreferencesSelectorImpl(FilterPreferences filterPreferences,
                                              DataModel dataModel, ExifImageReader exifImageReader, ExceptionHandler exceptionHandler) {
        super(filterPreferences, dataModel, exifImageReader, exceptionHandler);

        filterPreferencesSelectorDataModel = new SobelEdgesDetectorPreferencesSelectorDataModel(dataModel,
                filterPreferences, testImageCanvas);
        filterPreferencesSelectorDataModel.setSourceImage(testImage);
        filterPreferencesSelectorDataModel.startFilterCalculation();
    }

    @Override
    public SobelEdgesDetectorPreferencesSelectorDataModel getFilterPreferencesSelectorDataModel() {
        return filterPreferencesSelectorDataModel;
    }

    String getPreferencesDescription() {
        return "Sobel Edge Detector Preferences";
    }

    JPanel createFlavorPanel(Component component) {

        JPanel flavorPanel = new JPanel();
        flavorPanel.setLayout(new BoxLayout(flavorPanel, BoxLayout.PAGE_AXIS));

        flavorDimension = new Dimension(300, 30);

        return flavorPanel;
    }

    Dimension getFlavorDimension() {
        return flavorDimension;
    }

}
