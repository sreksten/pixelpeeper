package com.threeamigos.pixelpeeper.implementations.edgedetect.ui;

import com.threeamigos.common.util.interfaces.messagehandler.ExceptionHandler;
import com.threeamigos.pixelpeeper.interfaces.datamodel.DataModel;
import com.threeamigos.pixelpeeper.interfaces.datamodel.ExifImageReader;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavours.EdgesDetectorPreferences;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavours.RomyJonaEdgesDetectorPreferences;

import javax.swing.*;
import java.awt.*;
import java.util.Hashtable;

public class RomyJonaEdgesDetectorPreferencesSelectorImpl extends AbstractEdgesDetectorPreferencesSelectorImpl {

    private static final String PUPPAMENTO = "Puppamento";
    private static final String A_NASTRO = "A nastro";

    private Dimension flavourDimension;

    public RomyJonaEdgesDetectorPreferencesSelectorImpl(EdgesDetectorPreferences edgesDetectorPreferences,
                                                        RomyJonaEdgesDetectorPreferences romyJonaEdgesDetectorPreferences, DataModel dataModel,
                                                        ExifImageReader exifImageReader, Component parentComponent, ExceptionHandler exceptionHandler) {
        super(edgesDetectorPreferences, dataModel, exifImageReader, parentComponent, exceptionHandler);

        preferencesSelectorDataModel = new RomyJonaEdgesDetectorPreferencesSelectorDataModel(dataModel,
                edgesDetectorPreferences, romyJonaEdgesDetectorPreferences, testImageCanvas);
        preferencesSelectorDataModel.setSourceImage(testImage);
        preferencesSelectorDataModel.startEdgesCalculation();
    }

    String getPreferencesDescription() {
        return "Romy Jona Edge Detector Preferences";
    }

    JPanel createFlavourPanel(Component component) {

        RomyJonaEdgesDetectorPreferencesSelectorDataModel downcastDatamodel = (RomyJonaEdgesDetectorPreferencesSelectorDataModel) preferencesSelectorDataModel;

        Hashtable<Integer, JLabel> puppamentoSliderLabelTable = new Hashtable<>();
        puppamentoSliderLabelTable.put(1, new JLabel("1"));
        puppamentoSliderLabelTable.put(2, new JLabel("2"));
        puppamentoSliderLabelTable.put(3, new JLabel("3"));

        flavourDimension = getMaxDimension(component.getGraphics(), PUPPAMENTO, A_NASTRO, TRANSPARENCY);

        JPanel flavourPanel = new JPanel();
        flavourPanel.setLayout(new BoxLayout(flavourPanel, BoxLayout.PAGE_AXIS));

        createSliderPanel(flavourPanel, flavourDimension, PUPPAMENTO, downcastDatamodel.puppamentoSlider,
                puppamentoSliderLabelTable, downcastDatamodel.puppamentoText);

        flavourPanel.add(Box.createVerticalStrut(SPACING));

        createCheckboxPanel(flavourPanel, flavourDimension, A_NASTRO, downcastDatamodel.aNastroCheckbox);

        return flavourPanel;
    }

    Dimension getFlavourDimension() {
        return flavourDimension;
    }

}
