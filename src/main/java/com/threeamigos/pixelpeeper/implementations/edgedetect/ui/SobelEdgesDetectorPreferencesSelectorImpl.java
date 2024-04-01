package com.threeamigos.pixelpeeper.implementations.edgedetect.ui;

import com.threeamigos.common.util.interfaces.messagehandler.ExceptionHandler;
import com.threeamigos.pixelpeeper.interfaces.datamodel.DataModel;
import com.threeamigos.pixelpeeper.interfaces.datamodel.ExifImageReader;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavours.EdgesDetectorPreferences;

import javax.swing.*;
import java.awt.*;

public class SobelEdgesDetectorPreferencesSelectorImpl extends AbstractEdgesDetectorPreferencesSelectorImpl {

    private Dimension flavourDimension;

    public SobelEdgesDetectorPreferencesSelectorImpl(EdgesDetectorPreferences edgesDetectorPreferences,
                                                     DataModel dataModel, ExifImageReader exifImageReader, ExceptionHandler exceptionHandler) {
        super(edgesDetectorPreferences, dataModel, exifImageReader, exceptionHandler);

        preferencesSelectorDataModel = new SobelEdgesDetectorPreferencesSelectorDataModel(dataModel,
                edgesDetectorPreferences, testImageCanvas);
        preferencesSelectorDataModel.setSourceImage(testImage);
        preferencesSelectorDataModel.startEdgesCalculation();
    }

    String getPreferencesDescription() {
        return "Sobel Edge Detector Preferences";
    }

    JPanel createFlavourPanel(Component component) {

        JPanel flavourPanel = new JPanel();
        flavourPanel.setLayout(new BoxLayout(flavourPanel, BoxLayout.PAGE_AXIS));

        flavourDimension = new Dimension(300, 30);

        return flavourPanel;
    }

    Dimension getFlavourDimension() {
        return flavourDimension;
    }

}
