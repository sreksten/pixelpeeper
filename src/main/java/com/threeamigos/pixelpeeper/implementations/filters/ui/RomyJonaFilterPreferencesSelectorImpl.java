package com.threeamigos.pixelpeeper.implementations.filters.ui;

import com.threeamigos.common.util.interfaces.messagehandler.ExceptionHandler;
import com.threeamigos.pixelpeeper.interfaces.datamodel.DataModel;
import com.threeamigos.pixelpeeper.interfaces.datamodel.ExifImageReader;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavors.FilterPreferences;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavors.RomyJonaFilterPreferences;

import javax.swing.*;
import java.awt.*;
import java.util.Properties;

public class RomyJonaFilterPreferencesSelectorImpl extends AbstractFilterPreferencesSelectorImpl {

    private static final String PUPPAMENTO = "Puppamento";
    private static final String A_NASTRO = "A nastro";

    private Dimension flavorDimension;

    public RomyJonaFilterPreferencesSelectorImpl(FilterPreferences filterPreferences,
                                                 RomyJonaFilterPreferences romyJonaFilterPreferences, DataModel dataModel,
                                                 ExifImageReader exifImageReader, ExceptionHandler exceptionHandler) {
        super(filterPreferences, dataModel, exifImageReader, exceptionHandler);

        preferencesSelectorDataModel = new RomyJonaFilterPreferencesSelectorDataModel(dataModel,
                filterPreferences, romyJonaFilterPreferences, testImageCanvas);
        preferencesSelectorDataModel.setSourceImage(testImage);
        preferencesSelectorDataModel.startFilterCalculation();
    }

    String getPreferencesDescription() {
        return "Romy Jona Edge Detector Preferences";
    }

    JPanel createFlavorPanel(Component component) {

        RomyJonaFilterPreferencesSelectorDataModel downcastDatamodel = (RomyJonaFilterPreferencesSelectorDataModel) preferencesSelectorDataModel;

        Properties puppamentoSliderLabelTable = new Properties();
        puppamentoSliderLabelTable.put(1, new JLabel("1"));
        puppamentoSliderLabelTable.put(2, new JLabel("2"));
        puppamentoSliderLabelTable.put(3, new JLabel("3"));

        flavorDimension = getMaxDimension(component.getGraphics(), PUPPAMENTO, A_NASTRO, TRANSPARENCY);

        JPanel flavorPanel = new JPanel();
        flavorPanel.setLayout(new BoxLayout(flavorPanel, BoxLayout.PAGE_AXIS));

        createSliderPanel(flavorPanel, flavorDimension, PUPPAMENTO, downcastDatamodel.puppamentoSlider,
                puppamentoSliderLabelTable, downcastDatamodel.puppamentoText);

        flavorPanel.add(Box.createVerticalStrut(SPACING));

        createCheckboxPanel(flavorPanel, A_NASTRO, downcastDatamodel.aNastroCheckbox);

        return flavorPanel;
    }

    Dimension getFlavorDimension() {
        return flavorDimension;
    }

}
