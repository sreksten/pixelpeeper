package com.threeamigos.pixelpeeper.implementations.ui;

import com.threeamigos.pixelpeeper.data.ExifTag;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavors.NamePatternPreferences;
import com.threeamigos.pixelpeeper.interfaces.ui.NamePatternSelector;

import javax.swing.*;
import java.awt.*;

public class NamePatternSelectorImpl implements NamePatternSelector {

    protected static final int SPACING = 5;

    private static final String OK_OPTION = "OK";
    private static final String CANCEL_OPTION = "Cancel";

    private final NamePatternPreferences namePatternPreferences;

    private JTextField textField;

    public NamePatternSelectorImpl(NamePatternPreferences namePatternPreferences) {
        this.namePatternPreferences = namePatternPreferences;
    }

    @Override
    public void selectNamePattern(Component parentComponent) {

        String[] options = {OK_OPTION, CANCEL_OPTION};

        JOptionPane optionPane = new JOptionPane(createPreferencesPanel(), JOptionPane.PLAIN_MESSAGE,
                JOptionPane.OK_CANCEL_OPTION, null, options, options[1]);

        JDialog dialog = optionPane.createDialog(parentComponent, "Select name pattern");

        dialog.pack();
        dialog.setVisible(true);

        if (OK_OPTION.equals(optionPane.getValue())) {
            namePatternPreferences.setNamePattern(textField.getText());
        }

        dialog.dispose();
    }

    private JPanel createPreferencesPanel() {

        textField = new JTextField();
        textField.setText(namePatternPreferences.getNamePattern());
        ExifTag[] comboBoxValues = new ExifTag[ExifTag.values().length + 1];
        comboBoxValues[0] = null;
        ExifTag[] exifTagValues = ExifTag.values();
        System.arraycopy(exifTagValues, 0, comboBoxValues, 1, exifTagValues.length);
        JComboBox<ExifTag> comboBox = new JComboBox<>(comboBoxValues);
        comboBox.addActionListener(e -> {
            ExifTag selectedItem = (ExifTag) comboBox.getSelectedItem();
            if (selectedItem != null) {
                textField.setText(textField.getText() + "{" + selectedItem.name() + "}");
            }
        });

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));

        mainPanel.add(comboBox);

        mainPanel.add(Box.createVerticalStrut(SPACING));

        mainPanel.add(textField);

        return mainPanel;
    }

}
