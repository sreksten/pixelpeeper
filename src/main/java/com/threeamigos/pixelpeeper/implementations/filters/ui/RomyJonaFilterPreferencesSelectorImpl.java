package com.threeamigos.pixelpeeper.implementations.filters.ui;

import com.threeamigos.common.util.interfaces.messagehandler.ExceptionHandler;
import com.threeamigos.pixelpeeper.interfaces.datamodel.DataModel;
import com.threeamigos.pixelpeeper.interfaces.datamodel.ExifImageReader;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavors.FilterPreferences;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavors.RomyJonaFilterPreferences;

import javax.swing.*;
import java.awt.*;
import java.util.Properties;

public class RomyJonaFilterPreferencesSelectorImpl extends FilterPreferencesSelectorImpl {

    private static final String PUPPAMENTO = "Puppamento";
    private static final String A_NASTRO = "A nastro";

    private Dimension flavorDimension;

    private final RomyJonaFilterPreferencesSelectorDataModel filterPreferencesSelectorDataModel;

    public RomyJonaFilterPreferencesSelectorImpl(FilterPreferences filterPreferences,
                                                 RomyJonaFilterPreferences romyJonaFilterPreferences, DataModel dataModel,
                                                 ExifImageReader exifImageReader, ExceptionHandler exceptionHandler) {
        super(filterPreferences, dataModel, exifImageReader, exceptionHandler);

        filterPreferencesSelectorDataModel = new RomyJonaFilterPreferencesSelectorDataModel(dataModel,
                filterPreferences, romyJonaFilterPreferences, testImageCanvas);
        filterPreferencesSelectorDataModel.setSourceImage(testImage);
        filterPreferencesSelectorDataModel.startFilterCalculation();
    }

    @Override
    public RomyJonaFilterPreferencesSelectorDataModel getFilterPreferencesSelectorDataModel() {
        return filterPreferencesSelectorDataModel;
    }

    String getPreferencesDescription() {
        return "Romy Jona Filter preferences";
    }

    JPanel createFlavorPanel(Component component) {

        Properties puppamentoSliderLabelTable = new Properties();
        puppamentoSliderLabelTable.put(1, new JLabel("1"));
        puppamentoSliderLabelTable.put(2, new JLabel("2"));
        puppamentoSliderLabelTable.put(3, new JLabel("3"));

        flavorDimension = getMaxDimension(component.getGraphics(), java.util.List.of(PUPPAMENTO, A_NASTRO, TRANSPARENCY));

        JPanel flavorPanel = new JPanel();
        flavorPanel.setLayout(new BoxLayout(flavorPanel, BoxLayout.PAGE_AXIS));

        createSliderPanel(flavorPanel, flavorDimension, PUPPAMENTO, filterPreferencesSelectorDataModel.puppamentoSlider,
                puppamentoSliderLabelTable, filterPreferencesSelectorDataModel.puppamentoText);

        flavorPanel.add(Box.createVerticalStrut(SPACING));

        createCheckboxPanel(flavorPanel, A_NASTRO, filterPreferencesSelectorDataModel.aNastroCheckbox);

        return flavorPanel;
    }

    Dimension getFlavorDimension() {
        return flavorDimension;
    }

}
